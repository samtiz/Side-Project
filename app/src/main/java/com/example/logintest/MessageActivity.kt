package com.example.logintest

import android.annotation.SuppressLint
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    private var chatRoomName : String? = null
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
        val time = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("MM월dd일 hh:mm")
        val curTime = dateFormat.format(Date(time)).toString()

        // 클릭으로 넘어온 post Id, chatRoomName
        postId = intent.getStringExtra("postId")
        chatRoomName = intent.getStringExtra("chatRoomName")
        uid = mFirebaseAuth.currentUser?.uid!!
        recyclerView = findViewById(R.id.messageActivity_recyclerview)

        // 채팅방 이름 설정
        message_lists_top_name.text = chatRoomName

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
                println(users)
                mDatabaseReference.child("Post").child(postId.toString()).child("users").setValue(users)
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
        btnExit.setOnClickListener{
            // Post에 접근하여 uid 제거하기
            mDatabaseReference.child("Post").child(postId.toString()).child("users").child(uid.toString()).removeValue()
            // 메인 화면으로 돌아가기
            val intent = Intent(this@MessageActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
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
            holder.textView_message.text = comments[position].message
            holder.textView_time.text = comments[position].time
            if (speakingUserId.equals(uid)){// 본인 채팅
                holder.textView_message.setBackgroundResource(R.drawable.rightbubble)
                holder.textView_name.visibility = View.INVISIBLE
                holder.layout_destination.visibility = View.INVISIBLE
                holder.layout_main.gravity = Gravity.RIGHT
            }else{
                holder.textView_name.text = speakingUserName
                holder.layout_destination.visibility = View.VISIBLE
                holder.textView_name.visibility = View.VISIBLE
                holder.textView_message.setBackgroundResource(R.drawable.leftbubble)
                holder.layout_main.gravity = Gravity.LEFT
            }
        }

        inner class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view){
            val textView_message: TextView = view.findViewById(R.id.messageItem_textView_message)
            val textView_name: TextView = view.findViewById(R.id.messageItem_textview_name)
            //val imageView_profile: ImageView = view.findViewById(R.id.messageItem_imageview_profile)
            val layout_destination: LinearLayout = view.findViewById(R.id.messageItem_layout_destination)
            val layout_main: LinearLayout = view.findViewById(R.id.messageItem_linearlayout_main)
            val textView_time : TextView = view.findViewById(R.id.messageItem_textView_time)
        }

        override fun getItemCount(): Int {
            return comments.size
        }
    }

}