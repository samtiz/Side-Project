package com.example.logintest

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_post_detail.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PostDetailActivity : BasicActivity(){
    private var postId: String? = null
    private var isMyPost: Boolean? = null
    private var btnDelete: Button? = null
    private var btnModify: Button? = null
    private var btnJoinChat: Button? = null
    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var mDatabaseReference: DatabaseReference

    private lateinit var txtPostDetailToolbarTitle: TextView
    private lateinit var txtResName: TextView
    private lateinit var txtResCategory1: TextView
    private lateinit var txtResCategory2: TextView
    private lateinit var txtResCategory3: TextView
    private lateinit var txtResCategory4: TextView
    private lateinit var txtLocation: TextView
    private lateinit var txtFee: TextView
    private lateinit var txtTime: TextView
    private lateinit var txtHeadCount: TextView
    private lateinit var txtMain: TextView
    private lateinit var recyclerViewInquire: RecyclerView
    private lateinit var adapter: ListAdapterPostComment
    private lateinit var btnInquire: Button
    private lateinit var etInquireMain: EditText
    private lateinit var btnPostInquire: Button
    private lateinit var etReplyMain: EditText
    private lateinit var btnPostReply: Button
    private lateinit var layoutInquire: ConstraintLayout
    private lateinit var layoutReply: ConstraintLayout



    private val viewModel by lazy { ViewModelProvider(this@PostDetailActivity).get(CommentViewModel::class.java) }


    private var post: Post? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)

        mFirebaseAuth = FirebaseAuth.getInstance()
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("logintest")

        btnModify = findViewById(R.id.btn_modify)
        btnDelete = findViewById(R.id.btn_delete)
        btnJoinChat = findViewById(R.id.btn_join_chat)

        setSupportActionBar(toolbar_post_detail)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        postId = intent.getStringExtra("postId")





        txtPostDetailToolbarTitle = findViewById(R.id.txt_post_detail_toolbar_title)
        txtResName = findViewById(R.id.txt_detail_resname)
        txtResCategory1 = findViewById(R.id.txt_detail_rescategory1)
        txtResCategory2 = findViewById(R.id.txt_detail_rescategory2)
        txtResCategory3 = findViewById(R.id.txt_detail_rescategory3)
        txtResCategory4 = findViewById(R.id.txt_detail_rescategory4)
        txtLocation = findViewById(R.id.txt_detail_location)
        txtFee = findViewById(R.id.txt_detail_fee)
        txtTime = findViewById(R.id.txt_detail_timelimit)
        txtHeadCount = findViewById(R.id.txt_detail_headcount)
        txtMain = findViewById(R.id.txt_detail_maintext)
        recyclerViewInquire = findViewById(R.id.recyclerView_post_detail)
        etInquireMain = findViewById(R.id.edit_inquire)
        etReplyMain = findViewById(R.id.edit_reply)
        btnPostInquire = findViewById(R.id.btn_post_inquire)
        btnPostReply = findViewById(R.id.btn_post_reply)
        btnInquire = findViewById(R.id.btn_inquire)
        layoutInquire = findViewById(R.id.inquire_constraintLayout)
        layoutReply = findViewById(R.id.reply_constraintLayout)


        mDatabaseReference.child("Post").child(postId!!).get().addOnSuccessListener {
            post = it.getValue(Post::class.java) as Post
            isMyPost = if (intent.hasExtra("uid")) {
                intent.getStringExtra("uid") == post?.uid
            } else {
                false
            }
            txtPostDetailToolbarTitle.text = "${post?.users?.get(post?.uid)}의 모집글"
            txtResName.text = post?.restaurantName
            txtResCategory1.text = post?.foodCategories?.get(0) ?: ""
            txtResCategory2.text = post?.foodCategories?.get(1) ?: ""
            txtResCategory3.text = post?.foodCategories?.get(2) ?: ""
            txtResCategory4.text = post?.foodCategories?.get(3) ?: ""
            txtLocation.text = "배달 수령 위치: ${post?.dorm}"
            txtFee.text = "배달비: ${post?.minDeliveryFee}원 ~ ${post?.maxDeliveryFee}원"
            txtTime.text = "모집 만료 시간: ${post?.timeLimit}" // TODO 시간 포맷 바꿔서 적용
            txtHeadCount.text = "총 참여 인원: ${post?.users?.size}명"
            txtMain.text = post?.mainText
            if (isMyPost!!) {
                btnDelete?.visibility = VISIBLE
                btnModify?.visibility = VISIBLE
                btnJoinChat?.visibility = GONE
                btnInquire.visibility = GONE
                btnDelete?.setOnClickListener{
                    MaterialAlertDialogBuilder(this@PostDetailActivity).setMessage("정말로 이 게시물을 삭제하시겠습니까?\n게시물을 삭제하면 이 게시물의 모집 채팅방도 같이 삭제됩니다.")
                            .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                                deletePost()
                                finish()
                            })
                            .setNegativeButton("취소") { _, _ -> }.show()
                }
                btnModify?.setOnClickListener{
                    MaterialAlertDialogBuilder(this@PostDetailActivity).setMessage("게시물을 수정하시겠습니까?")
                            .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                                val intent2 = Intent(this@PostDetailActivity, WritePostActivity::class.java)
                                intent2.putExtra("postId", postId)
                                intent2.putExtra("uid", mFirebaseAuth.currentUser?.uid)
                                startActivity(intent2)
                            })
                            .setNegativeButton("취소") { _, _ -> }.show()
                }
            }
            else {
                btnJoinChat?.setOnClickListener {
                    mDatabaseReference.child("UserAccount").child(mFirebaseAuth.currentUser?.uid!!).child("postId").get().addOnSuccessListener {
                        var userpostId = it.value.toString()
                        if (userpostId == postId){
                            val intent = Intent(this@PostDetailActivity, MessageActivity::class.java)
                            intent.putExtra("postId", postId)
                            startActivity(intent)
                        }
                        else if(userpostId == "null"){
                            MaterialAlertDialogBuilder(this@PostDetailActivity).setMessage("${post?.restaurantName} 모집 채팅방에 참여하시겠습니까?")
                                .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                                    val intent = Intent(this@PostDetailActivity, MessageActivity::class.java)
                                    intent.putExtra("postId", postId)
                                    mDatabaseReference.child("UserAccount").child(mFirebaseAuth.currentUser?.uid!!).child("postId").setValue(postId)
                                    startActivity(intent)
                                })
                                .setNegativeButton("취소") { _, _ -> }.show()
                        }
                        else{
                            Toast.makeText(applicationContext, "이미 참여한 모집방이 존재합니다.", Toast.LENGTH_SHORT).show()
                        }
                    }.addOnFailureListener {
                    }

                }
                btnInquire.setOnClickListener{
                    layoutReply.visibility = GONE
                    layoutInquire.visibility = VISIBLE
                    etInquireMain.requestFocus()
                }
            }
        }.addOnFailureListener { Toast.makeText(this@PostDetailActivity, "get postId failed", Toast.LENGTH_SHORT).show() }



        //TODO view랑 post 데이터 연동 및 댓글 데이터 연동
        adapter = ListAdapterPostComment(this@PostDetailActivity)
        recyclerViewInquire.layoutManager = WrapContentLinearLayoutManager(this@PostDetailActivity)
        recyclerViewInquire.adapter = adapter
        observeComments()

        btnPostInquire.setOnClickListener {
            val comment: PostComment = PostComment()
            val uid: String = mFirebaseAuth.currentUser?.uid!!
            comment.uid = uid
            mDatabaseReference.child("UserAccount").child(uid).child("nickname").get().addOnSuccessListener {
                comment.userName = it.value.toString()
                comment.mainText = etInquireMain.text.toString()
                val time = System.currentTimeMillis()
                val dateFormat = SimpleDateFormat("yyyy.MM.dd hh:mm:ss")
                comment.time = dateFormat.format(Date(time)).toString()
                val key = mDatabaseReference.child("Post").child(postId!!).child("comments").push().key
                if (key == null) {
                    Toast.makeText(this@PostDetailActivity, "서버 오류: 다시 시도해주세요", Toast.LENGTH_SHORT).show()
                }
                else {
                    comment.commentId = key
                    mDatabaseReference.child("Post").child(postId!!).child("comments").child(key).setValue(comment)
                    Toast.makeText(this@PostDetailActivity, "게시하였습니다.", Toast.LENGTH_SHORT).show()
                    etInquireMain.text.clear()
                    layoutInquire.visibility = GONE
                    Fragment().hideKeyboard()
                }
                observeComments()
            }.addOnFailureListener {
                Toast.makeText(this@PostDetailActivity, "닉네임 오류: 다시 시도해주세요", Toast.LENGTH_SHORT).show()
            }
        }

        btnPostReply.setOnClickListener {
            val app = applicationContext as GlobalVariable
            val commentId = app.getCurrentCommentId()
            val reply: PostComment.Reply = PostComment.Reply()
            reply.uid = mFirebaseAuth.currentUser?.uid
            mDatabaseReference.child("UserAccount").child(mFirebaseAuth.currentUser?.uid!!).child("nickname").get().addOnSuccessListener {
                reply.userName = it.value.toString()
                reply.mainText = etReplyMain.text.toString()
                val time = System.currentTimeMillis()
                val dateFormat = SimpleDateFormat("yyyy.MM.dd hh:mm:ss")
                reply.time = dateFormat.format(Date(time)).toString()
                val key = mDatabaseReference.child("Post").child(postId!!).child("comments").child(commentId!!).child("replys").push().key
                if (key == null) {
                    Toast.makeText(this@PostDetailActivity, "서버 오류: 다시 시도해주세요", Toast.LENGTH_SHORT).show()
                }
                else {
                    reply.replyId = key
                    mDatabaseReference.child("Post").child(postId!!).child("comments").child(commentId!!).child("replys").child(key).setValue(reply)
                    Toast.makeText(this@PostDetailActivity, "게시하였습니다.", Toast.LENGTH_SHORT).show()
                    etReplyMain.text.clear()
                    layoutReply.visibility = GONE
                    Fragment().hideKeyboard()
                }
                observeComments()
            }.addOnFailureListener {
                Toast.makeText(this@PostDetailActivity, "닉네임 오류: 다시 시도해주세요", Toast.LENGTH_SHORT).show()
            }

        }


    }

    override fun onResume() {
        super.onResume()
        mDatabaseReference.child("Post").child(postId!!).get().addOnSuccessListener {
            post = it.getValue(Post::class.java) as Post
            txtPostDetailToolbarTitle.text = "${post?.users?.get(post?.uid)}의 모집글"
            txtResName.text = post?.restaurantName
            txtResCategory1.text = post?.foodCategories?.get(0) ?: ""
            txtResCategory2.text = post?.foodCategories?.get(1) ?: ""
            txtResCategory3.text = post?.foodCategories?.get(2) ?: ""
            txtResCategory4.text = post?.foodCategories?.get(3) ?: ""
            txtLocation.text = "배달 수령 위치: ${post?.dorm}"
            txtFee.text = "배달비: ${post?.minDeliveryFee}원 ~ ${post?.maxDeliveryFee}원"
            txtTime.text = "모집 만료 시간: ${post?.timeLimit}" // TODO 시간 포맷 바꿔서 적용
            txtHeadCount.text = "총 참여 인원: ${post?.users?.size}명"
            txtMain.text = post?.mainText
        }.addOnFailureListener { Toast.makeText(this@PostDetailActivity, "get postId failed", Toast.LENGTH_SHORT).show() }
        observeComments()
    }

    fun observeComments(){
        viewModel.fetchData(postId!!).observe(this, androidx.lifecycle.Observer {
            adapter.setCommentData(it)
            adapter.notifyDataSetChanged()
        })
    }

    private fun deletePost() {
        postId?.let { mDatabaseReference.child("Post").child(it).removeValue() }
        postId?.let { mDatabaseReference.child("chatrooms").child(it).removeValue() }
        // 현태 수정 ///
        mDatabaseReference.child("UserAccount").child(mFirebaseAuth.currentUser?.uid!!).child("postId").removeValue()
        //////////////
        // or
        // postId?.let { mDatabaseReference.child("Post").child(it).child("visibility").setValue(false) }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                overridePendingTransition(R.anim.none, R.anim.none)
                return true
            }
            R.id.report_post -> {
                Toast.makeText(this, "신고가 접수되셨습니다.", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}