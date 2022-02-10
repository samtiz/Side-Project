package com.example.logintest

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import kotlinx.android.synthetic.main.activity_message.*
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
    //private var chatRoomName : String? = null
    private val users : HashMap<String, String> = HashMap()

    private var recyclerView : RecyclerView? = null

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

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
        mDatabaseReference.child("Post").child(postId.toString()).child("users").addValueEventListener(object : ValueEventListener{
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
                println("아뭔데진짜")
                println(postId)
                mDatabaseReference.child("Post").child(postId.toString()).child("users").setValue(users)

                //입장 알람
                val entranceAlarm = ChatModel.Comment("Admin", "${userName}이 입장하셨습니다.", curTime)
                mDatabaseReference.child("chatrooms").child(postId.toString()).child("comments").push().setValue(entranceAlarm)

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
            val chatModel = ChatModel()
//            for (post_uid in usersId!!){
//                if (post_uid != uid) {
//                    chatModel.users.add(post_uid)
//                }
//            }
//            chatModel.users.add(uid.toString())
            //chatModel.post.put(postId!!, true)
            val comment = ChatModel.Comment(uid, editText.text.toString(), curTime)

            if(chatRoomUid == null){
                imageView.isEnabled = false
                mDatabaseReference.child("chatrooms").push().setValue(chatModel).addOnSuccessListener {
                    // 채팅방 생성
                    runBlocking {
                        val runCheckChatRoom = launch{
                            checkChatRoom()
                        }
                        runCheckChatRoom.join()
                    }
                    if (chatRoomUid != null){
                        mDatabaseReference.child("chatrooms").child(chatRoomUid.toString()).child("comments").push().setValue(comment)
                        messageActivity_editText.text = null
                    }
                    // 메세지 보내기
//                    Handler().postDelayed({
//                        println(chatRoomUid)
//                        mDatabaseReference.child("chatrooms").child(chatRoomUid.toString()).child("comments").push().setValue(comment)
//                        messageActivity_editText.text = null
//                    }, 1000L)
                    Log.d("chatUidNull dest", "$postId")
                }
            }else{
                mDatabaseReference.child("chatrooms").child(chatRoomUid.toString()).child("comments").push().setValue(comment)
                messageActivity_editText.text = null
                Log.d("chatUidNotNull dest", "$postId")
            }
        }

        val btnExit: Button = findViewById(R.id.btn_exit)
        val btnEnd : Button = findViewById(R.id.btn_end)
        if (postMaster == uid){ // 내가 만든 채팅방
            btnExit.visibility = View.GONE
            btnEnd.setOnClickListener{

            }
        }
        else{ // 남이 만든 채팅방
            btnEnd.visibility = View.GONE
            btnExit.setOnClickListener{
                // Post에 접근하여 uid 제거하기
                mDatabaseReference.child("Post").child(postId.toString()).child("users").child(uid.toString()).removeValue()
                // UserAccount에 접근하여 postId 제거하기
                mDatabaseReference.child("UserAccount").child(uid!!).child("postId").removeValue()
                // 메인 화면으로 돌아가기
                val intent = Intent(this@MessageActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
                // 퇴장 알림
                val exitAlarm = ChatModel.Comment("Admin", "${userName}이 퇴장하셨습니다.", curTime)
                mDatabaseReference.child("chatrooms").child(postId.toString()).child("comments").push().setValue(exitAlarm)
            }
        }
        checkChatRoom()
    }

    private fun checkChatRoom(){
        mDatabaseReference.child("chatrooms").orderByChild("$postId")
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                }
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (item in snapshot.children) {
                        print(item)
                        //chatRoomUid = item.key
                        chatRoomUid = postId
                        messageActivity_ImageView.isEnabled = true
                        recyclerView?.layoutManager = LinearLayoutManager(this@MessageActivity)
                        recyclerView?.adapter = RecyclerViewAdapter()
                    }

//                    for (item in snapshot.children){
//                        println(item)
//                        val chatModel = item.getValue<ChatModel>()
//                        if(chatModel?.post!!.containsKey(postId)){
//                            chatRoomUid = item.key
//                            messageActivity_ImageView.isEnabled = true
//                            recyclerView?.layoutManager = LinearLayoutManager(this@MessageActivity)
//                            recyclerView?.adapter = RecyclerViewAdapter()
//                        }
//                    }
                }
            })
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
            mDatabaseReference.child("chatrooms").child(chatRoomUid.toString()).child("comments").addValueEventListener(object : ValueEventListener{
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