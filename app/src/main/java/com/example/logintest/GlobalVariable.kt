package com.example.logintest

import android.app.Application

class GlobalVariable : Application() {
    private var userLocation: String? = null

    override fun onCreate() {
        super.onCreate()
    }

    override fun onTerminate() {
        super.onTerminate()
    }

    fun setUserLocation(loc: String?) {
        userLocation = loc
    }

    fun getUserLocation(): String? {
        return userLocation
    }
}