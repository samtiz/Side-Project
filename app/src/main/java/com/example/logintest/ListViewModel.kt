package com.example.logintest

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ListViewModel : ViewModel() {
    private val repo = Repo()
    fun fetchData(category: String?, selectedDormCategory: String?, userLocation: String?): LiveData<MutableList<Post>> {
        val mutableData = MutableLiveData<MutableList<Post>>()
        repo.getData(category, selectedDormCategory, userLocation).observeForever{
            mutableData.value = it
        }
        return mutableData
    }
    fun fetchMyPost(): LiveData<MutableList<Post>> {
        val mutableData = MutableLiveData<MutableList<Post>>()
        repo.getMyPost().observeForever{
            mutableData.value = it
        }
        return mutableData
    }

}