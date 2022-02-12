package com.example.logintest

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import kotlinx.android.synthetic.main.activity_message.*
import kotlinx.android.synthetic.main.activity_write_post.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlinx.coroutines.*

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

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        setSupportActionBar(toolbar_message)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        mFirebaseAuth = FirebaseAuth.getInstance()
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("logintest")

        val imageView = findViewById<ImageView>(R.id.messageActivity_ImageView)
        val editText = findViewById<TextView>(R.id.messageActivity_editText)

        // 메세지를 보낸 시간
        val time = System.currentTimeMillis() + 32400000
        val dateFormat = SimpleDateFormat("MM월dd일 hh:mm")
        val curTime = dateFormat.format(Date(time)).toString()

        // 클릭으로 넘어온 post Id, chatRoomName
        postId = intent.getStringExtra("postId")
        //chatRoomName = intent.getStringExtra("chatRoomName")
        uid = mFirebaseAuth.currentUser?.uid!!
        recyclerView = findViewById(R.id.messageActivity_recyclerview)


        // 채팅방 이름 설정
        // 포스트 정보 받아오기
        mDatabaseReference.child("Post").child(postId.toString()).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                    post = snapshot.getValue<Post>()
                    if (post?.restaurantName == "미정"){
                        message_lists_top_name.text = post?.foodCategories!![0]
                    }else{
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
        mDatabaseReference.child("Post").child(postId.toString()).child("users").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                for (data in snapshot.children){
                    val key = data.key
                    val item = data.value
                    users.put(key.toString(), item.toString())
                }
            }
        })

        // 유저 id 추가(처음 들어오는 유저만)
        Handler().postDelayed({
            if (users!!.containsKey(uid)){
            }else{
                users.put(uid!!, userName!!)
                mDatabaseReference.child("Post").child(postId.toString()).child("users").setValue(users)

                //입장 알람
                if (uid == postMaster){
                    val entranceAlarm = ChatModel.Comment("Admin", "${userName}님(방장)이 입장하셨습니다.", curTime)
                    mDatabaseReference.child("chatrooms").child(postId.toString()).child("comments").push().setValue(entranceAlarm)
                }else{
                    val entranceAlarm = ChatModel.Comment("Admin", "${userName}님이 입장하셨습니다.", curTime)
                    mDatabaseReference.child("chatrooms").child(postId.toString()).child("comments").push().setValue(entranceAlarm)
                }

                // 입장할 때 공지사항
                val dlg: AlertDialog.Builder = AlertDialog.Builder(this@MessageActivity,  android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth)
                dlg.setTitle("공지") //제목
                dlg.setMessage("1. 각자 원하시는 메뉴를 말씀해주세요.\n" +
                        "2. 음식을 받을 위치를 결정해주세요.\n" +
                        "3. 시키시는 분(방장)은 계좌번호를 알려주세요.") // 메시지
                dlg.setPositiveButton("확인"){ _,_ ->
                }
                dlg.show()
            }
        },1000L)


        // 전송 버튼 누르면
        imageView.setOnClickListener {
            if(!editText.text.isEmpty()){
                val comment = ChatModel.Comment(uid, editText.text.toString(), curTime)
                mDatabaseReference.child("chatrooms").child(postId.toString()).child("comments").push().setValue(comment)
                messageActivity_editText.text = null
                Log.d("chatUidNotNull dest", "$postId")
            }
        }

        recyclerView?.layoutManager = LinearLayoutManager(this@MessageActivity)
        recyclerView?.adapter = RecyclerViewAdapter()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (uid == postMaster){
            menuInflater.inflate(R.menu.menu_message_master, menu)
        }
        else{
            menuInflater.inflate(R.menu.menu_message, menu)
        }
        //menuInflater.inflate(R.menu.menu_message_master, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                overridePendingTransition(R.anim.none, R.anim.none)
                return true
            }
            R.id.action_exit -> {
                if (uid == postMaster){// 방장이 나가면
                    MaterialAlertDialogBuilder(this@MessageActivity).setMessage("채팅방을 나가시겠습니까? 방장의 권한은 다른 사람에게 넘어갑니다.")
                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                            exitMasterPost()
                        })
                        .setNegativeButton("취소") { _, _ -> }.show()
                }
                else{ // 방장이 아닌사람이 나가면
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
        val finishAlarm = ChatModel.Comment("Admin", "모집이 마감되었습니다.", "end")
        mDatabaseReference.child("chatrooms").child(postId.toString()).child("comments").push().setValue(finishAlarm)
    }

    private fun exitPost() {
        // Post에 접근하여 uid 제거하기
        mDatabaseReference.child("Post").child(postId.toString()).child("users").child(uid.toString()).removeValue()
        // UserAccount에 접근하여 postId 제거하기
        mDatabaseReference.child("UserAccount").child(uid!!).child("postId").removeValue()
        // 메인 화면으로 돌아가기
        val intent = Intent(this@MessageActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
        // 퇴장 알림
        val exitAlarm = ChatModel.Comment("Admin", "${userName}님이 퇴장하셨습니다.", "exit")
        mDatabaseReference.child("chatrooms").child(postId.toString()).child("comments").push().setValue(exitAlarm)

        // 마지막 남은 사람인지 확인하고 맞다면 채팅방 폭파
        mDatabaseReference.child("Post").child(postId.toString()).child("users").get().addOnSuccessListener {
            if (it.value == null){
                //post 제거
                mDatabaseReference.child("Post").child(postId.toString()).removeValue()
                //chatroom 제거
                mDatabaseReference.child("chatrooms").child(postId.toString()).removeValue()
            }
        }.addOnFailureListener {
        }
    }

    private fun exitMasterPost() {
        var nextMaster : String? = null
        var currentUsers : HashMap<String, String> = HashMap()
        // Post에 접근하여 uid 제거하기
        mDatabaseReference.child("Post").child(postId.toString()).child("users").child(uid.toString()).removeValue()
        // UserAccount에 접근하여 postId 제거하기
        mDatabaseReference.child("UserAccount").child(uid!!).child("postId").removeValue()
        // 메인 화면으로 돌아가기
        val intent = Intent(this@MessageActivity, MainActivity::class.java)
        startActivity(intent)
        finish()

        // 방장 넘기기

        mDatabaseReference.child("Post").child(postId.toString()).child("users").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.children == null){ // 채팅방에서 모두 나갔는지 확인
                    //post 제거
                    mDatabaseReference.child("Post").child(postId.toString()).removeValue()
                    //chatroom 제거
                    mDatabaseReference.child("chatrooms").child(postId.toString()).removeValue()
                }
                else{ // 방장 넘기기
                    for (data in snapshot.children){
                        val key = data.key
                        val item = data.value

                        currentUsers.put(key.toString(), item.toString())
                    }
                    nextMaster = currentUsers.keys.toTypedArray()[0]
                    mDatabaseReference.child("Post").child(postId.toString()).child("uid").setValue(nextMaster)
                }
            }
        })

//        val nextMaster = users.keys.toTypedArray()[0]
//        mDatabaseReference.child("Post").child(postId.toString()).child("uid").setValue(nextMaster)
        // 퇴장 알림
        val exitAlarm1 = ChatModel.Comment("Admin", "${userName}님(방장)이 퇴장하셨습니다.", "exit")
        val exitAlarm2 = ChatModel.Comment("Admin", "방장이 ${currentUsers.get(nextMaster)}님으로 변경되었습니다.", "exit")
        mDatabaseReference.child("chatrooms").child(postId.toString()).child("comments").push().setValue(exitAlarm1)
        mDatabaseReference.child("chatrooms").child(postId.toString()).child("comments").push().setValue(exitAlarm2)



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
        private var post : Post? = null
        init{
            // 포스트 정보 받아오기
            mDatabaseReference.child("Post").child(postId.toString()).addListenerForSingleValueEvent(object : ValueEventListener{
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
            mDatabaseReference.child("chatrooms").child(postId.toString()).child("comments").addValueEventListener(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    comments.clear()
                    for (data in snapshot.children){
                        val item = data.getValue<ChatModel.Comment>()
                        comments.add(item!!)
                        println(comments)
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
            holder.textView_message.textSize = 20F
            val speakingUserId = comments[position].uid
            var speakingUserName = users?.get(speakingUserId!!)
            holder.textView_time.text = comments[position].time

            if (speakingUserId.equals(uid)){// 본인 채팅
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