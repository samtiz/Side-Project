package com.example.logintest

import kotlin.collections.HashMap

class ChatModel (
    val post : HashMap<String, Boolean> = HashMap(),
    //val users: ArrayList<String> = ArrayList(),
    val comments : HashMap<String, Comment> = HashMap()){
    class Comment(val uid: String? = null, val message: String? = null, val time: String? = null)
}