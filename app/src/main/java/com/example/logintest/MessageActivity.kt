package com.example.logintest

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import kotlinx.android.synthetic.main.activity_message.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MessageActivity : AppCompatActivity() {

    private val fireDatabase = FirebaseDatabase.getInstance().reference
    private var chatRoomUid : String? = null
    private var destinationUid : String? = null
    private var uid : String? = null
    private var recyclerView : RecyclerView? = null

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)
        val imageView = findViewById<ImageView>(R.id.messageActivity_ImageView)
        val editText = findViewById<TextView>(R.id.messageActivity_editText)

        // 메세지를 보낸 시간
        val time = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("MM월dd일 hh:mm")
        val curTime = dateFormat.format(Date(time)).toString()

        destinationUid = intent.getStringExtra("destinationUid")
        uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
        recyclerView = findViewById(R.id.messageActivity_recyclerview)

        imageView.setOnClickListener {
            Log.d("클릭시 dest", "$destinationUid")
            val chatModel = ChatModel()
            chatModel.users[uid.toString()] = true
            chatModel.users[destinationUid!!] = true

            val comment = ChatModel.Comment(uid, editText.text.toString(), curTime)
            if(chatRoomUid == null){
                imageView.isEnabled = false
                fireDatabase.child("chatrooms").push().setValue(chatModel).addOnSuccessListener {
                    //채팅방 생성
                    checkChatRoom()
                    //메세지 보내기
                    Handler().postDelayed({
                        println(chatRoomUid)
                        fireDatabase.child("chatrooms").child(chatRoomUid.toString()).child("comments").push().setValue(comment)
                        messageActivity_editText.text = null
                    }, 1000L)
                    Log.d("chatUidNull dest", "$destinationUid")
                }
            }else{
                fireDatabase.child("chatrooms").child(chatRoomUid.toString()).child("comments").push().setValue(comment)
                messageActivity_editText.text = null
                Log.d("chatUidNotNull dest", "$destinationUid")
            }
        }
        checkChatRoom()
    }

    private fun checkChatRoom(){
        fireDatabase.child("chatrooms").orderByChild("users/$uid").equalTo(true).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                for (item in snapshot.children){
                    println(item)
                    val chatModel = item.getValue<ChatModel>()
                    if (chatModel?.users!!.containsKey(destinationUid)){
                        chatRoomUid = item.key
                        messageActivity_ImageView.isEnabled = true
                        recyclerView?.layoutManager = LinearLayoutManager(this@MessageActivity)
                        recyclerView?.adapter = RecyclerViewAdapter()
                    }
                }
            }
        })
    }
    inner class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerViewAdapter.MessageViewHolder>(){

        inner class MessageViewHolder(view : View) : RecyclerView.ViewHolder(view) {
            val textView_message : TextView = view.findViewById(R.id.messageItem_textview_message)
            val textView_name : TextView = view.findViewById(R.id.messageItem_textview_name)
            val imageView_profile : ImageView = view.findViewById(R.id.messageItem_imageview_profile)
            val layout_destination : LinearLayout = view.findViewById(R.id.messageItem_layout_destination)
            val layout_main : LinearLayout = view.findViewById(R.id.messageItem_linearlayout_main)
            val textView_time : TextView = view.findViewById(R.id.messageItem_textview_time)
        }

        private val comments = ArrayList<ChatModel.Comment>()
        private var post : Post? = null
        private val user :Post.User()
        init{
            fireDatabase.child("users").child(destinationUid.toString()).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    post = snapshot.getValue<Post>()
                    message_lists_top_name.text = post?.restaurantName + " / " + post?.foodCategory
                    getMessageList()
                }
            })
        }
        fun getMessageList(){
            fireDatabase.child("chatrooms").child(chatRoomUid.toString()).child("comments").addValueEventListener(object :
                ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    comments.clear()
                    for(data in snapshot.children){
                        val item = data.getValue<ChatModel.Comment>()
                        comments.add(item!!)
                        println(comments)
                    }
                    notifyDataSetChanged()
                    //메시지를 보낼 시 화면을 맨 밑으로 내림
                    recyclerView?.scrollToPosition(comments.size - 1)
                }
            })
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
            val view : View = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)

            return MessageViewHolder(view)
        }

        @SuppressLint("RtlHardcoded")
        override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
            holder.textView_message.textSize = 20F
            holder.textView_message.text = comments[position].message
            holder.textView_time.text = comments[position].time
            if(comments[position].uid.equals(uid)){ // 본인 채팅
                holder.textView_message.setBackgroundResource(R.drawable.rightbubble)
                holder.textView_name.visibility = View.INVISIBLE
                holder.layout_destination.visibility = View.INVISIBLE
                holder.layout_main.gravity = Gravity.RIGHT
            }else{ // 다른 사람 채팅
                Glide.with(holder.itemView.context)
                    .load(post.User?.profileImageUrl)
                    .apply(RequestOptions().circleCrop())
                    .into(holder.imageView_profile)
                holder.textView_name.text = Post.User?.name
                holder.textView_name.visibility = View.VISIBLE
                holder.layout_destination.visibility = View.VISIBLE
                holder.textView_message.setBackgroundResource(R.drawable.leftbubble)
                holder.layout_main.gravity = Gravity.LEFT
            }
            holder.textView_name
            holder.imageView_profile
            holder.layout_destination
            holder.layout_main
        }

        override fun getItemCount(): Int {
            return comments.size
        }
    }
}