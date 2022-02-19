package com.example.logintest

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

open class BasicActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        overridePendingTransition(R.anim.none, R.anim.none)
    }

    fun Context.hideKeyboard1(view: View) {
        val inputMethodManager = getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
    fun Context.showKeyboard1(view: View) {
        val inputMethodManager = getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(view, 0)
    }

    fun Fragment.hideKeyboard() {
        view?.let {
            activity?.hideKeyboard1(it)
        }
    }

    fun Fragment.showKeyboard() {
        view?.let {
            activity?.showKeyboard1(it)
        }
    }

    fun leftPad(strTime: String): String {
        val hourMinList = strTime.split(":")
        var hour: String? = null
        var minute: String? = null
        hour = if (hourMinList[0].length == 1) {
            "0${hourMinList[0]}"
        } else {
            hourMinList[0]
        }
        minute = if (hourMinList[1].length == 1) {
            "0${hourMinList[1]}"
        } else {
            hourMinList[1]
        }
        return "${hour}:${minute}"
    }





}



