package com.example.logintest

import kotlin.collections.HashMap

class ChatModel (val users : HashMap<String, Boolean> = HashMap(),
                 val comments : HashMap<String, Comment> = HashMap()) {
    class Comment (val uid : String? = null, val message : String?, val time : String? = null)
}