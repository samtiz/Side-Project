package com.songyu.commondelivery

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ListAdapterPostComment(private val context: Context, private val postUid: String?): RecyclerView.Adapter<ListAdapterPostComment.ViewHolder>() {
    private var commentList = mutableListOf<PostComment>()
    private lateinit var mDatabaseReference: DatabaseReference

    fun setCommentData(data: MutableList<PostComment>) {
        commentList = data
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListAdapterPostComment.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.comment_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListAdapterPostComment.ViewHolder, position: Int) {
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("logintest")
        val comment: PostComment = commentList[position]
        if (comment.uid == postUid) {
            holder.userName.text = comment.userName + "(작성자)"
            holder.userName.setTextColor(ContextCompat.getColor(context, R.color.themeColor))
        } else {
            holder.userName.text = comment.userName
            holder.userName.setTextColor(ContextCompat.getColor(context, R.color.black))
        }
        holder.mainText.text = comment.mainText
        holder.replyView.layoutManager = WrapContentLinearLayoutManager(context)
        val adapter = ListAdapterPostReply(context, postUid)
        holder.replyView.adapter = adapter
        adapter.setReplyData(comment.replys)
        adapter.notifyDataSetChanged()

        holder.btnReply.setOnClickListener {
            val c = context as PostDetailActivity
            val layoutReply: ConstraintLayout = c.findViewById(R.id.reply_constraintLayout)
            val layoutInquire: ConstraintLayout = c.findViewById(R.id.inquire_constraintLayout)
            val etReplyMain: EditText = c.findViewById(R.id.edit_reply)
            val layoutEntire: SwipeRefreshLayout = c.findViewById(R.id.swipeRefreshLayout_postDetail)
            val params = layoutEntire.layoutParams as ViewGroup.MarginLayoutParams
            val px = context.dpToPx(45)
            params.bottomMargin = px
            layoutEntire.layoutParams = params
            layoutInquire.visibility = GONE
            layoutReply.visibility = VISIBLE
            etReplyMain.requestFocus()
            val app = context.applicationContext as GlobalVariable
            app.setCurrentCommentId(comment.commentId)
        }

        holder.itemView.setOnLongClickListener {
            MaterialAlertDialogBuilder(context).setMessage("이 문의를 신고하시겠습니까?")
                .setPositiveButton("확인") { _, _ ->
                    comment.commentId?.let { it1 ->
                        mDatabaseReference.child("Report").child("comment").child(
                            it1
                        ).setValue(true)
                    }
                    Toast.makeText(context, "신고가 접수되셨습니다.", Toast.LENGTH_SHORT).show()
                }.setNegativeButton("취소") { _, _ -> }.show()
            return@setOnLongClickListener true
        }

    }

    override fun getItemCount(): Int {
        var num: Int = 0
        for (comment in commentList) {
            num += 1
            num += comment.replys.size
        }
        return num
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.txt_reply_username)
        val mainText: TextView = itemView.findViewById(R.id.txt_reply_main)
        val replyView: RecyclerView = itemView.findViewById(R.id.recyclerView_reply)
        val btnReply: Button = itemView.findViewById(R.id.btn_reply)
    }

    fun Context.dpToPx(dp: Int): Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics).toInt()

}