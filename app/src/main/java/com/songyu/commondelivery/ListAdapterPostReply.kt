package com.songyu.commondelivery

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ListAdapterPostReply(private val context: Context, private val postUid: String?): RecyclerView.Adapter<ListAdapterPostReply.ViewHolder>() {
    private val replyList = mutableListOf<PostComment.Reply>()
    private lateinit var mDatabaseReference: DatabaseReference

    fun setReplyData(data: HashMap<String, PostComment.Reply>) {
        replyList.clear()
        for ((key, value) in data) {
            replyList.add(value)
        }
        replyList.sortBy {
            it.time
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListAdapterPostReply.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.reply_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListAdapterPostReply.ViewHolder, position: Int) {
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("logintest")
        val reply: PostComment.Reply = replyList[position]
        if (reply.uid == postUid) {
            holder.userName.text = reply.userName + "(글쓴이)"
            holder.userName.setTextColor(ContextCompat.getColor(context, R.color.themeColor))
        } else {
            holder.userName.text = reply.userName
            holder.userName.setTextColor(ContextCompat.getColor(context, R.color.black))
        }
        holder.mainText.text = reply.mainText
        holder.itemView.setOnLongClickListener {
            MaterialAlertDialogBuilder(context).setMessage("이 답글을 신고하시겠습니까?")
                .setPositiveButton("확인") { _, _ ->
                    reply.replyId?.let { it1 ->
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
        return replyList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.txt_reply_username)
        val mainText: TextView = itemView.findViewById(R.id.txt_reply_main)
    }

}