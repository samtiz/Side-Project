package com.example.logintest

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.*
import kotlin.collections.ArrayList

class ListAdapter(private val context: Context): RecyclerView.Adapter<ListAdapter.ViewHolder>(), Filterable {
    private var postList = mutableListOf<Post>()
    private var postListFiltered = mutableListOf<Post>()

    fun setListData(data: MutableList<Post>) {
        postList = data
        postListFiltered = data
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListAdapter.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListAdapter.ViewHolder, position: Int) {
        val post: Post = postListFiltered[position]
        holder.restaurantName.text = post.restaurantName
        holder.restaurantCategory1.text = post.foodCategories?.get(0).toString()
        holder.restaurantCategory2.text = post.foodCategories?.get(1).toString()
        holder.restaurantCategory3.text = post.foodCategories?.get(2).toString()
        holder.restaurantCategory4.text = post.foodCategories?.get(3).toString()
        holder.mainText.text = post.mainText
        holder.dorm.text = post.dorm
        holder.timeLimit.text = post.timeLimit + "까지"
        holder.deliveryFee.text = "${post.minDeliveryFee}원 ~ ${post.maxDeliveryFee}원"

        // 현태가 수정한 부분 ///
        // 게시물을 클릭하면 채팅 activity로 넘어가게 함
        holder.itemView.setOnClickListener {
            val intent = Intent(context, MessageActivity::class.java)
            intent.putExtra("postId", post.postId)
            context.startActivity(intent)
        }
        ////////////////////
    }

    override fun getItemCount(): Int {
        return postListFiltered.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val restaurantName: TextView = itemView.findViewById(R.id.txt_restaurantName)
        val restaurantCategory1: TextView = itemView.findViewById(R.id.txt_restaurantCategory1)
        val restaurantCategory2: TextView = itemView.findViewById(R.id.txt_restaurantCategory2)
        val restaurantCategory3: TextView = itemView.findViewById(R.id.txt_restaurantCategory3)
        val restaurantCategory4: TextView = itemView.findViewById(R.id.txt_restaurantCategory4)
        val mainText: TextView = itemView.findViewById(R.id.txt_mainText)
        val dorm: TextView = itemView.findViewById(R.id.txt_dorm)
        val timeLimit: TextView = itemView.findViewById(R.id.txt_timeLimit)
        val deliveryFee: TextView = itemView.findViewById(R.id.txt_deliveryFee)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString = constraint?.toString() ?: ""
                if (charString.isEmpty()) {
                    postListFiltered = postList
                }
                else {
                    val filteredList = ArrayList<Post>()
                    val temp = ArrayList<Post>()
                    temp.addAll(postList)
                    temp
                            .filter {
//                                        (it.dorm?.contains(constraint!!, ignoreCase = true))!! or
//                                        (it.foodCategories?.get(0)?.contains(constraint!!))!! or
//                                        (it.foodCategories?.get(1)?.contains(constraint!!))!! or
//                                        (it.foodCategories?.get(2)?.contains(constraint!!))!! or
//                                        (it.foodCategories?.get(3)?.contains(constraint!!))!! or
                                          (it.restaurantName?.contains(constraint!!, ignoreCase = true))!!

                            }
                            .forEach { filteredList.add(it) }
                    postListFiltered = filteredList

                }
                return FilterResults().apply { values = postListFiltered }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                postListFiltered = if (results?.values == null)
                    ArrayList()
                else
                    results.values as ArrayList<Post>
                notifyDataSetChanged()
            }

        }
    }
}