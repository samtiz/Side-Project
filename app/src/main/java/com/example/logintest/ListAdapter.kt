package com.example.logintest

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ListAdapter(private val context: Context): RecyclerView.Adapter<ListAdapter.ViewHolder>() {
    private var userList = mutableListOf<Post>()

    fun setListData(data:MutableList<Post>){
        userList = data
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListAdapter.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item,parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListAdapter.ViewHolder, position: Int) {
        val post : Post = userList[position]
        holder.name.text = post.name
        holder.age.text = post.age.toString()
        holder.region.text = post.region
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val name : TextView = itemView.findViewById(R.id.name)
        val age : TextView = itemView.findViewById(R.id.age)
        val region : TextView = itemView.findViewById(R.id.region)

    }

}