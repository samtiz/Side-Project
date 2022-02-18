package com.example.logintest

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColor
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlin.collections.ArrayList

class ListAdapter(private val context: Context): RecyclerView.Adapter<ListAdapter.ViewHolder>(), Filterable {
    private var postList = mutableListOf<Post>()
    private var postListFiltered = mutableListOf<Post>()
    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var mDatabaseReference : DatabaseReference

    fun setListData(data: MutableList<Post>) {
        postList = data
        postListFiltered = data
//        mFirebaseAuth = FirebaseAuth.getInstance()
//        mDatabaseReference = FirebaseDatabase.getInstance().getReference("logintest")
//        val uid = mFirebaseAuth.currentUser?.uid!!
//        mDatabaseReference.child("UserAccount").child(uid).child("postId").get().addOnSuccessListener {
//            if (it.value.toString() != "null") {
//
//            }
//        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListAdapter.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListAdapter.ViewHolder, position: Int) {
        val post: Post = postListFiltered[position]

        if (post.dorm == "warning") {
            holder.restaurantName.text = ""
            holder.restaurantCategory1.text = ""
            holder.restaurantCategory2.text = ""
            holder.restaurantCategory3.text = ""
            holder.restaurantCategory4.text = ""
            holder.mainText.text = ""
            holder.dorm.text = if (post.mainText == null) {"모집글이 없습니다.\n하단의 '모집하기'를 눌러 새 모집글을 작성해보세요."} else {post.mainText}
            holder.timeLimit.text = ""
            holder.deliveryFee.text = ""
        }
        else {
            mFirebaseAuth = FirebaseAuth.getInstance()
            mDatabaseReference = FirebaseDatabase.getInstance().getReference("logintest")
            val uid = mFirebaseAuth.currentUser?.uid!!
            if (position == 0) {
                if (post.users.contains(uid)) {
                    holder.isParticipating.visibility = VISIBLE
                }
                else if (holder.isParticipating.visibility == VISIBLE) {
                    holder.isParticipating.visibility = GONE
                }
            }
            holder.restaurantName.text = post.restaurantName
            holder.restaurantCategory1.text = post.foodCategories?.get(0).toString()
            holder.restaurantCategory2.text = post.foodCategories?.get(1).toString()
            holder.restaurantCategory3.text = post.foodCategories?.get(2).toString()
            holder.restaurantCategory4.text = post.foodCategories?.get(3).toString()
            holder.mainText.text = post.mainText
            holder.dorm.text = post.dorm
            val time = post.timeLimit?.split(":")
            var strTime: String = ""
            strTime += if (time?.get(0)?.toInt()!! < 10) { "0${time[0]}" } else { time[0] }
            strTime += ":"
            strTime += if (time[1].toInt() < 10) { "0${time[1]}" } else { time[1] }
            holder.timeLimit.text = strTime + " 까지"
            holder.deliveryFee.text = "${post.minDeliveryFee}원 ~ ${post.maxDeliveryFee}원"

            // 게시물을 클릭하면 postDetail 로 넘어가게 함
            holder.itemView.setOnClickListener {

                mDatabaseReference.child("Post").child(post.postId.toString()).get().addOnSuccessListener {
                    if (it.value != null){
                        val intent = Intent(context, PostDetailActivity::class.java)
                        intent.putExtra("postId", post.postId)
                        intent.putExtra("uid", uid)
                        context.startActivity(intent)
                    }
                    else{
                        Toast.makeText(context, "이미 삭제된 게시물입니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            if (!post.visibility) {
                holder.restaurantName.setTextColor(ContextCompat.getColor(context, R.color.lightGray))
                holder.restaurantCategory1.setTextColor(ContextCompat.getColor(context, R.color.lightGray))
                holder.restaurantCategory2.setTextColor(ContextCompat.getColor(context, R.color.lightGray))
                holder.restaurantCategory3.setTextColor(ContextCompat.getColor(context, R.color.lightGray))
                holder.restaurantCategory4.setTextColor(ContextCompat.getColor(context, R.color.lightGray))
                holder.mainText.setTextColor(ContextCompat.getColor(context, R.color.lightGray))
                holder.dorm.setTextColor(ContextCompat.getColor(context, R.color.lightGray))
                holder.timeLimit.setTextColor(ContextCompat.getColor(context, R.color.lightGray))
                holder.deliveryFee.setTextColor(ContextCompat.getColor(context, R.color.lightGray))
            }
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
        val isParticipating: TextView = itemView.findViewById(R.id.txt_participating)
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
                if (postListFiltered.isEmpty()) {
                    val temp = Post()
                    temp.dorm = "warning"
                    temp.mainText = "검색 결과가 없습니다."
                    postListFiltered.add(temp)
                }
                notifyDataSetChanged()
            }

        }
    }
}