package com.songyu.commondelivery

import androidx.multidex.MultiDexApplication

class GlobalVariable : MultiDexApplication() {
    private var userLocation: String? = null
    private var currentCommentId: String? = null

    override fun onCreate() {
        super.onCreate()
    }

    override fun onTerminate() {
        super.onTerminate()
    }

    fun setUserLocation(loc: String?) { userLocation = loc }
    fun getUserLocation(): String? { return userLocation }

    fun setCurrentCommentId(commentId: String?) { currentCommentId = commentId }
    fun getCurrentCommentId(): String? { return currentCommentId }
}