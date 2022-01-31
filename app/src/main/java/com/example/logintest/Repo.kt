package com.example.logintest

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class Repo {
    fun getData(category: String?, selectedDormCategory: String?, userLocation: String?): LiveData<MutableList<Post>> {
        val mutableData = MutableLiveData<MutableList<Post>>()
        val myRef = FirebaseDatabase.getInstance().getReference("logintest/Post")
        myRef.addValueEventListener(object : ValueEventListener {
            val listData: MutableList<Post> = mutableListOf<Post>()
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for (PostSnapshot in snapshot.children){
                        val getData = PostSnapshot.getValue(Post::class.java)
                        if (category == "전체" || getData?.foodCategories?.contains(category)!!) {
                            if (selectedDormCategory == "전체") {
                                listData.add(getData!!)
                            }
                            else if (selectedDormCategory == "같은 건물만" && getData?.dorm == userLocation) {
                                listData.add(getData!!)
                            }
                            else if (selectedDormCategory == "북측기숙사" && isNorth(getData?.dorm)) {
                                listData.add(getData!!)
                            }
                            else if (selectedDormCategory == "서측기숙사" && isWest(getData?.dorm)) {
                                listData.add(getData!!)
                            }
                            else if (selectedDormCategory == "동측기숙사" && isEast(getData?.dorm)) {
                                listData.add(getData!!)
                            }
                            else if (selectedDormCategory == "문지캠" && isMunji(getData?.dorm)) {
                                listData.add(getData!!)
                            }
                            else if (selectedDormCategory == "화암캠" && isHwaam(getData?.dorm)) {
                                listData.add(getData!!)
                            }
                        }
                        mutableData.value = listData
                    }
                }
            }

            private fun isNorth(dorm: String?): Boolean {
                return dorm == "사랑관" || dorm == "소망관" || dorm == "성실관" || dorm == "진리관" || dorm == "아름관" || dorm == "신뢰관" || dorm == "지혜관"
            }
            private fun isWest(dorm: String?): Boolean {
                return dorm == "갈릴레이관" || dorm == "여울/나들관" || dorm == "다솜/희망관" || dorm == "원내아파트" || dorm == "나래/미르관" || dorm == "나눔관"
            }
            private fun isEast(dorm: String?): Boolean {
                return dorm == "세종관"
            }
            private fun isMunji(dorm: String?): Boolean {
                return dorm == "문지관"
            }
            private fun isHwaam(dorm: String?): Boolean {
                return dorm == "화암관"
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
        return mutableData
    }
}