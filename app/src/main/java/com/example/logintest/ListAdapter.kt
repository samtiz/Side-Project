package com.example.logintest

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
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
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

        if (post.dorm == "warning" && position == 0) {
            holder.restaurantName.text = if (post.mainText == null) {"모집글이 없습니다."} else {post.mainText}
            holder.mainText.text = if (post.mainText == null) {"하단의 '모집하기'를 눌러 새 모집글을 작성해보세요."} else {""}
            holder.dormNtimeNfee.text = ""
            holder.dormNtimeNfee.setTextColor(ContextCompat.getColor(context, R.color.black))
            if (holder.isParticipating.visibility == VISIBLE) {
                holder.isParticipating.visibility = GONE
            }
            if (holder.numUsers.visibility == VISIBLE) {
                holder.numUsers.visibility = GONE
            }
            holder.restaurantName.setTextColor(ContextCompat.getColor(context, R.color.darkGray))
            holder.mainText.setTextColor(ContextCompat.getColor(context, R.color.darkGray))
            holder.dormNtimeNfee.setTextColor(ContextCompat.getColor(context, R.color.darkGray))
        }
        else {
            mFirebaseAuth = FirebaseAuth.getInstance()
            mDatabaseReference = FirebaseDatabase.getInstance().getReference("logintest")
            val uid = mFirebaseAuth.currentUser?.uid!!
            val time = post.timeLimit?.split(":")
            if (position == 0) {
                if (post.users.contains(uid)) {
                    holder.isParticipating.visibility = VISIBLE
                }
                else if (holder.isParticipating.visibility == VISIBLE) {
                    holder.isParticipating.visibility = GONE
                }

                if (holder.numUsers.visibility == GONE) {
                    holder.numUsers.visibility = VISIBLE
                }

                if (!post.visibility) {
                    holder.restaurantName.setTextColor(ContextCompat.getColor(context, R.color.lightGray))
                    holder.mainText.setTextColor(ContextCompat.getColor(context, R.color.lightGray))
                    holder.dormNtimeNfee.setTextColor(ContextCompat.getColor(context, R.color.lightGray))
                }
                else {
                    holder.restaurantName.setTextColor(ContextCompat.getColor(context, R.color.black))
                    holder.mainText.setTextColor(ContextCompat.getColor(context, R.color.black))
                    holder.dormNtimeNfee.setTextColor(ContextCompat.getColor(context, R.color.black))
                }
            }
            if (post.restaurantName == "미정") {
                val strTemp = post.restaurantName.toString() +" (" + post.foodCategories?.get(0).toString() + ")"
                holder.restaurantName.text = strTemp
            } else {
                holder.restaurantName.text = post.restaurantName
            }
            holder.mainText.text = post.mainText

            var temp = ""
            temp += "배달위치 ${post.dorm}"
            temp += " · "

            var strTime: String = ""
            strTime += if (time?.get(0)?.let{ it1 -> it1.toInt() < 10} == true) { "0${time[0]}" } else {
                time?.get(0)
            }
            strTime += ":"
            strTime += if (time?.get(1)?.let{ it1 -> it1.toInt() < 10} == true) { "0${time[1]}" } else {
                time?.get(1)
            }
            temp += "모집마감 $strTime"
            temp += "\n"
            temp += "배달비 ${post.minDeliveryFee}원 ~ ${post.maxDeliveryFee}원"

            holder.dormNtimeNfee.text = temp
            holder.numUsers.text = post.users.size.toString()

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
        }
        ////////////////////
    }

    override fun getItemCount(): Int {
        return postListFiltered.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val restaurantName: TextView = itemView.findViewById(R.id.txt_restaurantName)
        val mainText: TextView = itemView.findViewById(R.id.txt_mainText)
        val dormNtimeNfee: TextView = itemView.findViewById(R.id.txt_dormNtimeNfee)
        val isParticipating: TextView = itemView.findViewById(R.id.txt_participating)
        val numUsers: TextView = itemView.findViewById(R.id.txt_numUsers)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString = constraint?.toString() ?: ""
                if (charString.isEmpty()) {
                    postListFiltered = postList
                }
                else if (postList.isNotEmpty() && postList[0].dorm == "warning") {
                    postListFiltered = ArrayList<Post>()
                }
                else {
                    println(postList)
                    val filteredList = ArrayList<Post>()
                    val temp = ArrayList<Post>()
                    temp.addAll(postList)
                    temp
                            .filter {
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