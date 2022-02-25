package com.songyu.commondelivery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CommentViewModel: ViewModel() {
    private val repo = RepoComment()
    fun fetchData(postId: String): LiveData<MutableList<PostComment>> {
        val mutableData = MutableLiveData<MutableList<PostComment>>()
        repo.getData(postId).observeForever{
            mutableData.value = it
        }
        return mutableData
    }
}

class RepoComment {
    fun getData(postId: String): LiveData<MutableList<PostComment>> {
        val mutableData = MutableLiveData<MutableList<PostComment>>()
        val myRef = FirebaseDatabase.getInstance().getReference("logintest/Post/${postId}/comments")
        myRef.orderByChild("time").addListenerForSingleValueEvent(object : ValueEventListener {
            val listData: MutableList<PostComment> = mutableListOf<PostComment>()
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for (CommentSnapshot in snapshot.children) {
                        val getData = CommentSnapshot.getValue(PostComment::class.java)
                        listData.add(getData!!)
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
