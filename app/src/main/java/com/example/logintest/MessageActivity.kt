package com.example.logintest

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_message.*
import kotlinx.android.synthetic.main.activity_post_detail.*
import kotlinx.android.synthetic.main.activity_write_post.*
import kotlinx.coroutines.*
import okhttp3.Dispatcher
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import java.io.File
import java.security.AccessController.getContext
import kotlin.coroutines.CoroutineContext
import kotlin.math.max

class MessageActivity : BasicActivity(){

    //private lateinit var mJob : Job
    private lateinit var mFirebaseAuth : FirebaseAuth
    private lateinit var mDatabaseReference : DatabaseReference

//    override val coroutineContext: CoroutineContext
//        get() = mJob + Dispatchers.Main

    //private var usersId : ArrayList<String>? = null
    private var uid : String? = null
    private var userName : String? = null

    private var postId : String? = null
    private var post : Post? = null
    private var postMaster : String? = null
    private var users : HashMap<String, String> = HashMap()
    private var chatusers : HashMap<String, ChatModel.userStat> = HashMap()


    private var recyclerView : RecyclerView? = null

    private var uriPhoto : Uri? = null

    private var isViewing: Boolean? = null

    // 메세지를 보낸 시간
    val time = System.currentTimeMillis()
    private val dateFormat = SimpleDateFormat("MM월dd일 hh:mm")
    private val curTime = dateFormat.format(Date(time)).toString()

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)
//        mJob = Job()

        setSupportActionBar(toolbar_message)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        mFirebaseAuth = FirebaseAuth.getInstance()
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("logintest")


        val imageView_photo = findViewById<ImageView>(R.id.messageActivity_ImageView_photo)
        val imageView = findViewById<ImageView>(R.id.messageActivity_ImageView)
        val editText = findViewById<TextView>(R.id.messageActivity_editText)

        isViewing = true

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

        var maxIndex = 0

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
        mDatabaseReference.child("UserAccount").child(uid!!).child("nickname").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                userName = snapshot.getValue<String>()
            }
        })

            // 채팅방에 참여중인 유저 가져오기
            // 최대 인덱스 확인용
        mDatabaseReference.child("chatrooms").child(postId.toString()).child("users").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                chatusers.clear()
                maxIndex = 0
                for (data in snapshot.children){
                    val key = data.key
                    val item = data.getValue<ChatModel.userStat>()
                    chatusers.put(key.toString(), item!!)
                    if (maxIndex < item.index!!){
                        maxIndex = item.index!!
                    }
                }
            }
        })

        mDatabaseReference.child("chatrooms").child(postId.toString()).addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

                //Toast.makeText(applicationContext, "존재하지 않는 채팅방입니다.", Toast.LENGTH_SHORT).show()
                if (isViewing!!) {
                    val dlg: AlertDialog.Builder = AlertDialog.Builder(this@MessageActivity, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth)
                    dlg.setMessage("존재하지 않는 채팅방입니다. 채팅을 종료합니다.") // 메시지
                    dlg.setPositiveButton("확인") { _, _ ->
                        finish()
                    }
                    dlg.show()
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })


            // 해당 포스트에 참여중인 유저 가져오기
        mDatabaseReference.child("Post").child(postId.toString()).child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                users.clear()
                for (data in snapshot.children) {
                    val key = data.key
                    val item = data.value
                    users.put(key.toString(), item.toString())
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })


        Handler().postDelayed({
            // 처음 들어오는 유저 더해주기
            if (!users.containsKey(uid)){

                Log.d("firstUser", users.toString())

                if((users.isEmpty() && uid == postMaster) || !users.isEmpty()){
                    users.put(uid!!, userName!!)
                    mDatabaseReference.child("Post").child(postId.toString()).child("users").child(uid.toString()).setValue(userName.toString())

                    // 채팅방 users 업데이트
                    val userStatus = ChatModel.userStat(userName, maxIndex + 1)
                    chatusers.put(uid!!, userStatus)
                    mDatabaseReference.child("chatrooms").child(postId.toString()).child("users").child(uid.toString()).setValue(userStatus)


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
            }
            println("자 이제 시작이야")
            recyclerView?.layoutManager = LinearLayoutManager(this@MessageActivity)
            recyclerView?.adapter = RecyclerViewAdapter()
        },1000L)



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
            val time = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("MM월dd일 hh:mm")
            val curTime = dateFormat.format(Date(time)).toString()

            if(!editText.text.isEmpty()){
                val comment = ChatModel.Comment(uid, editText.text.toString(), curTime, false)
                mDatabaseReference.child("chatrooms").child(postId.toString()).child("comments").push().setValue(comment)
                messageActivity_editText.text = null
                Log.d("chatUidNotNull dest", "$postId")
            }
            imageView.visibility = View.INVISIBLE
            imageView_photo.visibility = View.VISIBLE
        }



    }

    override fun onRestart() {
        super.onRestart()
        isViewing = true
        mDatabaseReference.child("UserAccount").child(uid!!).child("nowChatting").setValue(true)
        mDatabaseReference.child("chatrooms").child(postId.toString()).get().addOnSuccessListener {
            if (it.value.toString() == "null") {
                val dlg: AlertDialog.Builder = AlertDialog.Builder(this@MessageActivity, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth)
                dlg.setMessage("존재하지 않는 채팅방입니다. 채팅을 종료합니다.") // 메시지
                dlg.setPositiveButton("확인") { _, _ ->
                    finish()
                }
                dlg.show()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        isViewing = false
        mDatabaseReference.child("UserAccount").child(uid!!).child("nowChatting").setValue(false)
    }

//    override fun onDestroy() {
//        super.onDestroy()
//
//        mJob.cancel()
//    }

    private fun showContextPopupPermission() {
        Toast.makeText(applicationContext, "사진을 불러오기 위해 권한이 필요합니다", Toast.LENGTH_SHORT).show()
    }

    private fun navigatesPhotos() {
        val intent = Intent(this, imagePickerActivity::class.java)
        startActivityForResult(intent, 2000)

    }

    // 그림 전송
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val time = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("MM월dd일 hh:mm")
        val curTime = dateFormat.format(Date(time)).toString()

        if (requestCode == 2000) {
            var path = data?.getStringExtra("image")
            if (data != null){
                // 데이터 가져옴
                uriPhoto = Uri.fromFile(File(path))
                val messageId = mDatabaseReference.child("chatrooms").child(postId.toString()).child("comments").push()
                FirebaseStorage.getInstance().reference.child("ChatImages").child(postId.toString()).child(messageId.getKey().toString()).putFile(uriPhoto!!).addOnSuccessListener {taskSnapshot ->
                    taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener {
                        it ->
                            var imageUrl=it.toString()
                        val photoComments = ChatModel.Comment(uid, imageUrl, curTime, true)
                        messageId.setValue(photoComments)
                    }
                    Toast.makeText(applicationContext, "전송 완료", Toast.LENGTH_SHORT).show()
                    //messageId.setValue(photoComments)
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
            R.id.action_toPost -> {
                val intent = Intent(this@MessageActivity, PostDetailActivity::class.java)
                intent.putExtra("postId", postId)
                intent.putExtra("uid", uid)
                startActivity(intent)
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
        val time = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("MM월dd일 hh:mm")
        val curTime = dateFormat.format(Date(time)).toString()
        mDatabaseReference.child("Post").child(postId.toString()).child("visibility").setValue(false)
        // 모집마감 알림
        val finishAlarm = ChatModel.Comment("Admin", "모집이 마감되었습니다.", curTime, false)
        mDatabaseReference.child("chatrooms").child(postId.toString()).child("comments").push().setValue(finishAlarm)
    }

    private fun exitPost() {
        val time = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("MM월dd일 hh:mm")
        val curTime = dateFormat.format(Date(time)).toString()
        // Post에 접근하여 uid 제거하기
        mDatabaseReference.child("Post").child(postId.toString()).child("users").child(uid.toString()).removeValue()
        // UserAccount에 접근하여 postId 제거하기
        mDatabaseReference.child("UserAccount").child(uid!!).child("postId").removeValue()
        // 메인 화면으로 돌아가기
        finish()
        // 퇴장 알림
        val exitAlarm = ChatModel.Comment("Admin", "${userName}님이 퇴장하셨습니다.", curTime, false)
        mDatabaseReference.child("chatrooms").child(postId.toString()).child("comments").push().setValue(exitAlarm)

    }

    private fun exitMasterPost() {
        val time = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("MM월dd일 hh:mm")
        val curTime = dateFormat.format(Date(time)).toString()
        var nextMaster : String? = null
        val currentUsersId = ArrayList<String>()
        // Post에 접근하여 uid 제거하기
        mDatabaseReference.child("Post").child(postId.toString()).child("users").child(uid.toString()).removeValue()
        // UserAccount에 접근하여 postId 제거하기
        mDatabaseReference.child("UserAccount").child(uid!!).child("postId").removeValue()
        // 메인 화면으로 돌아가기
        finish()


        mDatabaseReference.child("Post").child(postId.toString()).child("users").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                for (data in snapshot.children){
                    val key = data.key
                    currentUsersId.add(key!!)
                }
                if (currentUsersId.isEmpty()){//  다 나갔으면 채팅방 폭파
                    println("유저가 아무도 안남음!!!")
                    //post 제거
                    mDatabaseReference.child("Post").child(postId.toString()).removeValue()
                    //chatroom 제거
                    mDatabaseReference.child("chatrooms").child(postId.toString()).removeValue()
                    // 사진 파일 제거
                    FirebaseStorage.getInstance().reference.child("ChatImages").child(postId.toString()).delete()
                    return
                }
                var maxIndex = 100
                for (cuid in currentUsersId){
                    if(chatusers.get(cuid)?.index!! < maxIndex){
                        maxIndex = chatusers.get(cuid)?.index!!
                        nextMaster = cuid
                    }
                }
                mDatabaseReference.child("Post").child(postId.toString()).child("uid").setValue(nextMaster)
                // 퇴장 알림
                val exitAlarm1 = ChatModel.Comment("Admin", "${userName}님(방장)이 퇴장하셨습니다.", curTime, false)
                val exitAlarm2 = ChatModel.Comment("Admin", "방장이 ${chatusers.get(nextMaster!!)?.nickname}님으로 변경되었습니다.", curTime, false)
                mDatabaseReference.child("chatrooms").child(postId.toString()).child("comments").push().setValue(exitAlarm1)
                mDatabaseReference.child("chatrooms").child(postId.toString()).child("comments").push().setValue(exitAlarm2)
            }
        })

//        mDatabaseReference.child("Post").child(postId.toString()).child("users").addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onCancelled(error: DatabaseError) {
//            }
//
//            override fun onDataChange(snapshot: DataSnapshot) {
//                currentUsers.clear()
//                for (data in snapshot.children) {
//                    val key = data.key
//                    val item = data.value
//
//                    //현재 유저들 정보 받아오기
//                    currentUsers.put(key.toString(), item.toString())
//                }
//
//                if (isChatRoomEmpty) {
//                    //post 제거
//                    mDatabaseReference.child("Post").child(postId.toString()).removeValue()
//                    //chatroom 제거
//                    mDatabaseReference.child("chatrooms").child(postId.toString()).removeValue()
//                    // 사진 파일 제거
//                    FirebaseStorage.getInstance().reference.child("ChatImages").child(postId.toString()).delete()
//                } else {
//                    var minIndex = 100
//                    for (key in currentUsers.keys){
//                        val i = currentUsers.get(key)?.index!!
//                        if(i < minIndex){
//                            minIndex = i
//                            nextMaster = key
//                        }
//                    }
//                    //nextMaster = currentUsers.keys.toTypedArray()[0]
//                    mDatabaseReference.child("Post").child(postId.toString()).child("uid").setValue(nextMaster)
//
//                    // 퇴장 알림
//                    val exitAlarm1 = ChatModel.Comment("Admin", "${userName}님(방장)이 퇴장하셨습니다.", curTime, false)
//                    val exitAlarm2 = ChatModel.Comment("Admin", "방장이 ${
//                        currentUsers.get(nextMaster)?.nickname
//                    }님으로 변경되었습니다.", curTime, false)
//                    mDatabaseReference.child("chatrooms").child(postId.toString()).child("comments").push().setValue(exitAlarm1)
//                    mDatabaseReference.child("chatrooms").child(postId.toString()).child("comments").push().setValue(exitAlarm2)
//                }
//            }
//        })

    }

    inner class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerViewAdapter.MessageViewHolder>() {

        private val comments = ArrayList<ChatModel.Comment>()
        private val keys = ArrayList<String>()
        private var post : Post? = null

        init{
            // 포스트 정보 받아오기
            getMessageList()
        }
        fun getMessageList(){
            mDatabaseReference.child("chatrooms").child(postId.toString()).child("comments").addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    comments.clear()
                    for (data in snapshot.children) {
                        val item = data.getValue<ChatModel.Comment>()
                        comments.add(item!!)
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
            holder.textView_message.textSize = 20F
            val speakingUserId = comments[position].uid
            var speakingUserName = chatusers.get(speakingUserId!!)?.nickname
            holder.textView_time.text = comments[position].time
            holder.imageView_message.visibility = View.GONE


            // 프사 정해주기
            var index = 0
            if (!speakingUserId.equals("Admin")){
                index = chatusers.get(speakingUserId)?.index!!
            }

            when{
                index % 5 == 0 -> {
                    holder.imageView_profile.setBackgroundResource(R.drawable.profile_background1)
                }
                index % 5 == 1 -> {
                    holder.imageView_profile.setBackgroundResource(R.drawable.profile_background2)
                }
                index % 5 == 2 -> {
                    holder.imageView_profile.setBackgroundResource(R.drawable.profile_background3)
                }
                index % 5 == 3 -> {
                    holder.imageView_profile.setBackgroundResource(R.drawable.profile_background4)
                }
                index % 5 == 4 -> {
                    holder.imageView_profile.setBackgroundResource(R.drawable.profile_background5)
                }
            }

            if (isPhoto!!){
                holder.imageView_message.visibility = View.VISIBLE
                holder.textView_message.visibility = View.GONE
                var photo = Uri.parse(comments[position].message)

                // 사진 클릭하면 자세히 보여주기
                holder.imageView_message.setOnClickListener{
                    val intent = Intent(this@MessageActivity, photoDetailActivity::class.java)
                    intent.putExtra("imageUrl", comments[position].message)
                    startActivity(intent)
                }

                Glide.with(holder.itemView.context).load(photo).into(holder.imageView_message)
                when {
                    speakingUserId.equals(uid) -> {// 본인 채팅
                        holder.textView_name.visibility = View.INVISIBLE
                        holder.layout_destination.visibility = View.INVISIBLE
                        holder.layout_main.gravity = Gravity.RIGHT

                        holder.layout_admin.visibility = View.GONE
                        holder.textView_admin_message.visibility = View.GONE

                    }
                    else -> {// 남의 채팅
                        holder.textView_name.text = speakingUserName
                        holder.layout_destination.visibility = View.VISIBLE
                        when{

                        }
                        //holder.imageView_profile.background
                        holder.textView_name.visibility = View.VISIBLE
                        holder.layout_main.gravity = Gravity.LEFT

                        holder.layout_admin.visibility = View.GONE
                        holder.textView_admin_message.visibility = View.GONE
                    }
                }
            }else{
                when {
                    speakingUserId.equals(uid) -> {// 본인 채팅
                        holder.textView_message.text = comments[position].message
                        holder.textView_message.setBackgroundResource(R.drawable.rightbubble)
                        holder.textView_name.visibility = View.INVISIBLE
                        holder.layout_destination.visibility = View.INVISIBLE
                        holder.layout_main.gravity = Gravity.RIGHT

                        holder.layout_admin.visibility = View.GONE
                        holder.textView_admin_message.visibility = View.GONE


                    }
                    speakingUserId.equals("Admin") -> {

                        holder.layout_admin.visibility = View.VISIBLE
                        holder.textView_admin_message.visibility = View.VISIBLE
                        holder.textView_admin_message.text = comments[position].message
                        holder.textView_name.visibility = View.GONE
                        holder.layout_destination.visibility = View.GONE
                        holder.textView_message.visibility = View.GONE
                        holder.textView_time.visibility = View.GONE
                    }
                    else -> {// 남의 채팅
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
            }

        }

        inner class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view){
            val textView_message: TextView = view.findViewById(R.id.messageItem_textView_message)
            val textView_name: TextView = view.findViewById(R.id.messageItem_textview_name)
            val imageView_message: ImageView = view.findViewById(R.id.messageItem_imageview_message)
            val imageView_profile: ImageView = view.findViewById(R.id.messageItem_imageview_profile)
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