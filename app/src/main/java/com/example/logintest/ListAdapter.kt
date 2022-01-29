package com.example.logintest

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ListAdapter(private val context: Context): RecyclerView.Adapter<ListAdapter.ViewHolder>() {
    private var postList = mutableListOf<Post>()

    fun setListData(data:MutableList<Post>){
        postList = data
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListAdapter.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item,parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListAdapter.ViewHolder, position: Int) {
        val post : Post = postList[position]
        holder.restaurantName.text = post.restaurantName
        holder.restaurantCategory1.text = post.foodCategories?.get(0).toString()
        holder.restaurantCategory2.text = post.foodCategories?.get(1).toString()
        holder.restaurantCategory3.text = post.foodCategories?.get(2).toString()
        holder.restaurantCategory4.text = post.foodCategories?.get(3).toString()
        holder.mainText.text = post.mainText
        holder.dorm.text = post.dorm
        holder.timeLimit.text = post.timeLimit + "까지"
        holder.deliveryFee.text = "${post.minDeliveryFee}원 ~ ${post.maxDeliveryFee}원"
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val restaurantName : TextView = itemView.findViewById(R.id.txt_restaurantName)
        val restaurantCategory1 : TextView = itemView.findViewById(R.id.txt_restaurantCategory1)
        val restaurantCategory2 : TextView = itemView.findViewById(R.id.txt_restaurantCategory2)
        val restaurantCategory3 : TextView = itemView.findViewById(R.id.txt_restaurantCategory3)
        val restaurantCategory4 : TextView = itemView.findViewById(R.id.txt_restaurantCategory4)
        val mainText: TextView = itemView.findViewById(R.id.txt_mainText)
        val dorm: TextView = itemView.findViewById(R.id.txt_dorm)
        val timeLimit: TextView = itemView.findViewById(R.id.txt_timeLimit)
        val deliveryFee: TextView = itemView.findViewById(R.id.txt_deliveryFee)
    }

}