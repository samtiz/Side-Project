package com.example.logintest

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ListAdapterPostReply(private val context: Context): RecyclerView.Adapter<ListAdapterPostReply.ViewHolder>() {
    private var replyList = mutableListOf<PostComment.Reply>()

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
        val reply: PostComment.Reply = replyList[position]
        holder.userName.text = reply.userName
        holder.mainText.text = reply.mainText
    }

    override fun getItemCount(): Int {
        return replyList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.txt_reply_username)
        val mainText: TextView = itemView.findViewById(R.id.txt_reply_main)
    }

}