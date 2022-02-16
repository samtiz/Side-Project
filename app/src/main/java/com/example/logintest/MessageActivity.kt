package com.example.logintest

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_message.*
import kotlinx.android.synthetic.main.activity_write_post.*
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlinx.coroutines.*
import java.io.File
import java.util.jar.Manifest

class MessageActivity : AppCompatActivity() {

    private lateinit var mFirebaseAuth : FirebaseAuth
    private lateinit var mDatabaseReference : DatabaseReference


    //private var usersId : ArrayList<String>? = null
    private var uid : String? = null
    private var userName : String? = null

    private var chatRoomUid : String? = null
    private var postId : String? = null
    private var post : Post? = null
    private var postMaster : String? = null
    private val users : HashMap<String, String> = HashMap()


    private var recyclerView : RecyclerView? = null

    private var pickImageFromElbum = 0
    private var uriPhoto : Uri? = null

    // 메세지를 보낸 시간
    val time = System.currentTimeMillis() + 32400000
    private val dateFormat = SimpleDateFormat("MM월dd일 hh:mm")
    private val curTime = dateFormat.format(Date(time)).toString()

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        setSupportActionBar(toolbar_message)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        mFirebaseAuth = FirebaseAuth.getInstance()
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("logintest")

        val imageView_photo = findViewById<ImageView>(R.id.messageActivity_ImageView_photo)
        val imageView = findViewById<ImageView>(R.id.messageActivity_ImageView)
        val editText = findViewById<TextView>(R.id.messageActivity_editText)



        // 클릭으로 넘어온 post Id, chatRoomName
        postId = intent.getStringExtra("postId")
        //chatRoomName = intent.getStringExtra("chatRoomName")
        uid = mFirebaseAuth.currentUser?.uid!!
        recyclerView = findViewById(R.id.messageActivity_recyclerview)

        mDatabaseReference.child("UserAccount").child(uid!!).child("nowChatting").setValue(true)
        //글창에 글 없으면 이미지 추가 버튼, 글 있으면 전송 버튼
        //imageView.visibility = View.INVISIBLE
        imageView.visibility = View.INVISIBLE
        imageView_photo.visibility = View.VISIBLE
        editText.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(editText.text.isEmpty()){
                    imageView.visibility = View.INVISIBLE
                    imageView_photo.visibility = View.VISIBLE
                }
                imageView.visibility = View.VISIBLE
                imageView_photo.visibility = View.INVISIBLE
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

        // 채팅방 이름 설정
        // 포스트 정보 받아오기
        mDatabaseReference.child("Post").child(postId.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                post = snapshot.getValue<Post>()
                if (post?.restaurantName == "미정") {
                    message_lists_top_name.text = post?.foodCategories!![0]
                } else {
                    message_lists_top_name.text = post?.restaurantName
                }
                postMaster = post?.uid
            }
        })



        // UserAccount 에서 현재 사용자 이름 받아오기
        mDatabaseReference.child("UserAccount").child(uid!!).child("nickname").get().addOnSuccessListener {
            userName = it.value.toString()
        }.addOnFailureListener {
            Toast.makeText(this@MessageActivity, "get() username failed", Toast.LENGTH_SHORT).show()
        }

        // 해당 포스트의 채팅방에 참여중인 유저 가져오기
        mDatabaseReference.child("Post").child(postId.toString()).child("users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                users.clear()
                for (data in snapshot.children) {
                    val key = data.key
                    val item = data.value
                    users.put(key.toString(), item.toString())
                }

            }
        })

        // 유저 id 추가(처음 들어오는 유저만)
        Handler().postDelayed({
            if (users!!.containsKey(uid)) {
            } else {
                users.put(uid!!, userName!!)
                mDatabaseReference.child("Post").child(postId.toString()).child("users").setValue(users)

                //입장 알람
                if (uid == postMaster){
                    val entranceAlarm = ChatModel.Comment("Admin", "${userName}님(방장)이 입장하셨습니다.", curTime, false)
                    mDatabaseReference.child("chatrooms").child(postId.toString()).child("comments").push().setValue(entranceAlarm)
                }else{
                    val entranceAlarm = ChatModel.Comment("Admin", "${userName}님이 입장하셨습니다.", curTime, false)
                    mDatabaseReference.child("chatrooms").child(postId.toString()).child("comments").push().setValue(entranceAlarm)
                }

                // 입장할 때 공지사항
                val dlg: AlertDialog.Builder = AlertDialog.Builder(this@MessageActivity, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth)
                dlg.setTitle("공지") //제목
                dlg.setMessage("1. 각자 원하시는 메뉴를 말씀해주세요.\n" +
                        "2. 음식을 받을 위치를 결정해주세요.\n" +
                        "3. 시키시는 분(방장)은 계좌번호를 알려주세요.") // 메시지
                dlg.setPositiveButton("확인") { _, _ ->
                }
                dlg.show()
            }
        }, 1000L)

        imageView_photo.setOnClickListener {
            when{
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                )==PackageManager.PERMISSION_GRANTED ->{
                    // 권한이 잘 부여 되었을 때 갤러리에서 사진을 선택하는 기능
                    navigatesPhotos()
                }
                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE)->{
                    // 교육용 팝 확인 후 권한 팝업 띄우는 기능

                    showContextPopupPermission()
                }
                else ->{
                    requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),1000)
                }
            }
        }


        // 글 전송 버튼 누르면
        imageView.setOnClickListener {
            if(!editText.text.isEmpty()){
                val comment = ChatModel.Comment(uid, editText.text.toString(), curTime, false)
                mDatabaseReference.child("chatrooms").child(postId.toString()).child("comments").push().setValue(comment)
                messageActivity_editText.text = null
                Log.d("chatUidNotNull dest", "$postId")
            }
//            if(editText.text.isEmpty()){
//                imageView.visibility = View.INVISIBLE
//                imageView_photo.visibility = View.VISIBLE
//            }
            imageView.visibility = View.INVISIBLE
            imageView_photo.visibility = View.VISIBLE
        }

        recyclerView?.layoutManager = LinearLayoutManager(this@MessageActivity)
        recyclerView?.adapter = RecyclerViewAdapter()
    }

    override fun onRestart() {
        super.onRestart()
        mDatabaseReference.child("UserAccount").child(uid!!).child("nowChatting").setValue(true)
    }

    override fun onStop() {
        super.onStop()
        mDatabaseReference.child("UserAccount").child(uid!!).child("nowChatting").setValue(false)
    }

    private fun showContextPopupPermission() {
//        AlertDialog.Builder(this).setTitle("권한 요청")
//            .setMessage("사진을 불러오기 위해 권한이 필요합니다.")
//            .setPositiveButton("동의") {_,_ ->
//                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1000)
//            }
//            .setNegativeButton("취소"){_,_ ->
//            }
//            .create()
//            .show()
        Toast.makeText(applicationContext, "사진을 불러오기 위해 권한이 필요합니다", Toast.LENGTH_SHORT).show()
    }

    private fun navigatesPhotos() {
        val intent = Intent(this, imagePickerActivity::class.java)
        startActivityForResult(intent, 2000)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2000) {
            var path = data?.getStringExtra("image")
            if (data != null){
                // 데이터 가져옴
                uriPhoto = Uri.fromFile(File(path))
                val photoComments = ChatModel.Comment(uid, uriPhoto.toString(), curTime, true)
                val messageId = mDatabaseReference.child("chatrooms").child(postId.toString()).child("comments").push()
                FirebaseStorage.getInstance().reference.child("ChatImages").child(postId.toString()).child(messageId.getKey().toString()).putFile(uriPhoto!!).addOnSuccessListener {
                    Toast.makeText(applicationContext, "업로드 완료!", Toast.LENGTH_SHORT).show()
                    Handler().postDelayed({
                        messageId.setValue(photoComments)
                    },10000L)

                }

            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        mDatabaseReference.child("Post").child(postId.toString()).child("uid").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                postMaster = snapshot.getValue<String>()
                menu?.clear()
                if (uid == postMaster) {
                    menuInflater.inflate(R.menu.menu_message_master, menu)
                } else {
                    menuInflater.inflate(R.menu.menu_message, menu)
                }
            }
        })

        //menuInflater.inflate(R.menu.menu_message_master, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (isTaskRoot) {
                    val intent = Intent(this@MessageActivity, LoginActivity::class.java)
                    intent.action = Intent.ACTION_MAIN
                    intent.addCategory(Intent.CATEGORY_LAUNCHER)
                    startActivity(intent)
                    finish()
                    overridePendingTransition(R.anim.none, R.anim.none)
                } else {
                    finish()
                    overridePendingTransition(R.anim.none, R.anim.none)
                }
                return true
            }
            R.id.action_exit -> {
                if (uid == postMaster) {// 방장이 나가면
                    MaterialAlertDialogBuilder(this@MessageActivity).setMessage("채팅방을 나가시겠습니까? 방장의 권한은 다른 사람에게 넘어갑니다.")
                            .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                                exitMasterPost()
                            })
                            .setNegativeButton("취소") { _, _ -> }.show()
                } else { // 방장이 아닌사람이 나가면
                    MaterialAlertDialogBuilder(this@MessageActivity).setMessage("채팅방을 나가시겠습니까?")
                            .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                                exitPost()
                            })
                            .setNegativeButton("취소") { _, _ -> }.show()
                }
            }
            R.id.action_finish -> {
                MaterialAlertDialogBuilder(this@MessageActivity).setMessage("모집을 마감하시겠습니까?")
                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                            finishPost()
                        })
                        .setNegativeButton("취소") { _, _ -> }.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }



    private fun finishPost() {
        mDatabaseReference.child("Post").child(postId.toString()).child("visibility").setValue(false)
        // 모집마감 알림
        val finishAlarm = ChatModel.Comment("Admin", "모집이 마감되었습니다.", curTime, false)
        mDatabaseReference.child("chatrooms").child(postId.toString()).child("comments").push().setValue(finishAlarm)
    }

    private fun exitPost() {
        // Post에 접근하여 uid 제거하기
        mDatabaseReference.child("Post").child(postId.toString()).child("users").child(uid.toString()).removeValue()
        // UserAccount에 접근하여 postId 제거하기
        mDatabaseReference.child("UserAccount").child(uid!!).child("postId").removeValue()
        // 메인 화면으로 돌아가기
//        val intent = Intent(this@MessageActivity, MainActivity::class.java)
//        startActivity(intent)
        finish()
        // 퇴장 알림
        val exitAlarm = ChatModel.Comment("Admin", "${userName}님이 퇴장하셨습니다.", curTime, false)
        mDatabaseReference.child("chatrooms").child(postId.toString()).child("comments").push().setValue(exitAlarm)

//        // 마지막 남은 사람인지 확인하고 맞다면 채팅방 폭파
//        mDatabaseReference.child("Post").child(postId.toString()).child("users").get().addOnSuccessListener {
//            if (it.value == null){
//                //post 제거
//                mDatabaseReference.child("Post").child(postId.toString()).removeValue()
//                //chatroom 제거
//                mDatabaseReference.child("chatrooms").child(postId.toString()).removeValue()
//            }
//        }.addOnFailureListener {
//        }
    }

    private fun exitMasterPost() {
        var nextMaster : String? = null
        var currentUsers : HashMap<String, String> = HashMap()
        // Post에 접근하여 uid 제거하기
        mDatabaseReference.child("Post").child(postId.toString()).child("users").child(uid.toString()).removeValue()
        // UserAccount에 접근하여 postId 제거하기
        mDatabaseReference.child("UserAccount").child(uid!!).child("postId").removeValue()
        // 메인 화면으로 돌아가기
//        val intent = Intent(this@MessageActivity, MainActivity::class.java)
//        startActivity(intent)
        finish()


        mDatabaseReference.child("Post").child(postId.toString()).child("users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                currentUsers.clear()
                for (data in snapshot.children) {
                    val key = data.key
                    val item = data.value

                    currentUsers.put(key.toString(), item.toString())
                }
                if (currentUsers.isEmpty()) {
                    //post 제거
                    mDatabaseReference.child("Post").child(postId.toString()).removeValue()
                    //chatroom 제거
                    mDatabaseReference.child("chatrooms").child(postId.toString()).removeValue()
                } else {
                    nextMaster = currentUsers.keys.toTypedArray()[0]
                    mDatabaseReference.child("Post").child(postId.toString()).child("uid").setValue(nextMaster)

                    // 퇴장 알림
                    val exitAlarm1 = ChatModel.Comment("Admin", "${userName}님(방장)이 퇴장하셨습니다.", curTime, false)
                    val exitAlarm2 = ChatModel.Comment("Admin", "방장이 ${currentUsers.get(nextMaster)}님으로 변경되었습니다.", curTime, false)
                    mDatabaseReference.child("chatrooms").child(postId.toString()).child("comments").push().setValue(exitAlarm1)
                    mDatabaseReference.child("chatrooms").child(postId.toString()).child("comments").push().setValue(exitAlarm2)
                }

//                if (snapshot.children == null){ // 채팅방에서 모두 나갔는지 확인
//                    //post 제거
//                    mDatabaseReference.child("Post").child(postId.toString()).removeValue()
//                    //chatroom 제거
//                    mDatabaseReference.child("chatrooms").child(postId.toString()).removeValue()
//                }
//                else{ // 방장 넘기기
//                    for (data in snapshot.children){
//                        val key = data.key
//                        val item = data.value
//
//                        currentUsers.put(key.toString(), item.toString())
//                    }
//                    nextMaster = currentUsers.keys.toTypedArray()[0]
//                    mDatabaseReference.child("Post").child(postId.toString()).child("uid").setValue(nextMaster)
//
//                    // 퇴장 알림
//                    val exitAlarm1 = ChatModel.Comment("Admin", "${userName}님(방장)이 퇴장하셨습니다.", "exit")
//                    val exitAlarm2 = ChatModel.Comment("Admin", "방장이 ${currentUsers.get(nextMaster)}님으로 변경되었습니다.", "exit")
//                    mDatabaseReference.child("chatrooms").child(postId.toString()).child("comments").push().setValue(exitAlarm1)
//                    mDatabaseReference.child("chatrooms").child(postId.toString()).child("comments").push().setValue(exitAlarm2)
//                }
            }
        })

//        val nextMaster = users.keys.toTypedArray()[0]
//        mDatabaseReference.child("Post").child(postId.toString()).child("uid").setValue(nextMaster)




        // 마지막 남은 사람인지 확인하고 맞다면 채팅방 폭파
//        mDatabaseReference.child("Post").child(postId.toString()).child("users").get().addOnSuccessListener {
//            println("남은사람")
//            println(it.value)
//            if (it.value == null){
//                //post 제거
//                mDatabaseReference.child("Post").child(postId.toString()).removeValue()
//                //chatroom 제거
//                mDatabaseReference.child("chatrooms").child(postId.toString()).removeValue()
//            }
//        }.addOnFailureListener {
//        }
    }

    inner class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerViewAdapter.MessageViewHolder>() {

        private val comments = ArrayList<ChatModel.Comment>()
        private val keys = ArrayList<String>()
        private var post : Post? = null
        init{
            // 포스트 정보 받아오기
            mDatabaseReference.child("Post").child(postId.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
//                    post = snapshot.getValue<Post>()
//                    if (post?.restaurantName == "미정"){
//                        message_lists_top_name.text = post?.foodCategories!![0]
//                    }else{
//                        message_lists_top_name.text = post?.restaurantName
//                    }
                    getMessageList()
                }
            })
        }
        fun getMessageList(){
            mDatabaseReference.child("chatrooms").child(postId.toString()).child("comments").addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    comments.clear()
                    for (data in snapshot.children) {
                        val item = data.getValue<ChatModel.Comment>()
                        val key = data.key
                        comments.add(item!!)
                        keys.add(key!!)
                    }
                    notifyDataSetChanged()
                    recyclerView?.scrollToPosition(comments.size - 1)
                }
            })
        }

        override fun getItemViewType(position: Int): Int {
            return position
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
            val view : View = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)

            return MessageViewHolder(view)
        }

        @SuppressLint("RtlHardcoded")
        override fun onBindViewHolder(
                holder: MessageViewHolder,
                position: Int
        ) {
            // 사진이면 사진 받아오기
            var isPhoto = comments[position].isPhoto
            if (isPhoto!!){
                FirebaseStorage.getInstance().reference.child("ChatImages").child(postId.toString()).child(keys[position]).downloadUrl
                    .addOnSuccessListener {
                        Glide.with(holder.itemView.context)
                            .load(it).into(holder.imageView_message)
                    }.addOnFailureListener{
                        Toast.makeText(applicationContext, "실패!", Toast.LENGTH_SHORT).show()
                    }
            }

            holder.textView_message.textSize = 20F

            val speakingUserId = comments[position].uid
            var speakingUserName = users?.get(speakingUserId!!)
            holder.textView_time.text = comments[position].time
            holder.imageView_message.visibility = View.INVISIBLE

            if (speakingUserId.equals(uid)){// 본인 채팅
//                if (isPhoto!!){
//                    holder.imageView_message.visibility = View.VISIBLE
//
//                }
//                else{
//                    holder.textView_message.text = comments[position].message
//                    holder.textView_message.setBackgroundResource(R.drawable.rightbubble)
//                    holder.textView_name.visibility = View.INVISIBLE
//                    holder.layout_destination.visibility = View.INVISIBLE
//                    holder.layout_main.gravity = Gravity.RIGHT
//
//                    holder.layout_admin.visibility = View.GONE
//                    holder.textView_admin_message.visibility = View.GONE
//                }
                holder.imageView_message.visibility = View.VISIBLE
                holder.textView_message.text = comments[position].message
                holder.textView_message.setBackgroundResource(R.drawable.rightbubble)
                holder.textView_name.visibility = View.INVISIBLE
                holder.layout_destination.visibility = View.INVISIBLE
                holder.layout_main.gravity = Gravity.RIGHT

                holder.layout_admin.visibility = View.GONE
                holder.textView_admin_message.visibility = View.GONE


            }
            else if(speakingUserId.equals("Admin")){

                holder.layout_admin.visibility = View.VISIBLE
                holder.textView_admin_message.visibility = View.VISIBLE
                holder.textView_admin_message.text = comments[position].message
                holder.textView_name.visibility = View.GONE
                holder.layout_destination.visibility = View.GONE
                holder.textView_message.visibility = View.GONE
                holder.textView_time.visibility = View.GONE
            }
            else{// 남의 채팅
                holder.textView_message.text = comments[position].message
                holder.textView_name.text = speakingUserName
                holder.layout_destination.visibility = View.VISIBLE
                holder.textView_name.visibility = View.VISIBLE
                holder.textView_message.setBackgroundResource(R.drawable.leftbubble)
                holder.layout_main.gravity = Gravity.LEFT

                holder.layout_admin.visibility = View.GONE
                holder.textView_admin_message.visibility = View.GONE
            }
        }

        inner class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view){
            val textView_message: TextView = view.findViewById(R.id.messageItem_textView_message)
            val textView_name: TextView = view.findViewById(R.id.messageItem_textview_name)
            val imageView_message: ImageView = view.findViewById(R.id.messageItem_imageview_message)
            //val imageView_profile: ImageView = view.findViewById(R.id.messageItem_imageview_profile)
            val layout_destination: LinearLayout = view.findViewById(R.id.messageItem_layout_destination)
            val layout_main: LinearLayout = view.findViewById(R.id.messageItem_linearlayout_main)
            val textView_time : TextView = view.findViewById(R.id.messageItem_textView_time)

            // 관리자 메세지
            val textView_admin_message: TextView = view.findViewById(R.id.messageItem_textView_admin_message)
            val layout_admin: LinearLayout = view.findViewById(R.id.messageItem_linearlayout_admin)
        }

        override fun getItemCount(): Int {
            return comments.size
        }
    }

}