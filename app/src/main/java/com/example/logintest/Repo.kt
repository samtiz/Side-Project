package com.example.logintest

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class Repo {
    fun getData(category:String): LiveData<MutableList<Post>> {
        val mutableData = MutableLiveData<MutableList<Post>>()
        val myRef = FirebaseDatabase.getInstance().getReference("logintest/Post")
        myRef.addValueEventListener(object : ValueEventListener {
            val listData: MutableList<Post> = mutableListOf<Post>()
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for (PostSnapshot in snapshot.children){
                        val getData = PostSnapshot.getValue(Post::class.java)
                        if (category == "전체" || getData?.foodCategories?.contains(category)!!) {
                            listData.add(getData!!)
                        }


                        mutableData.value = listData
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
        return mutableData
    }
}