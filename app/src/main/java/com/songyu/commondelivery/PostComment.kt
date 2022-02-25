package com.songyu.commondelivery

class PostComment {
    var uid: String? = null
    var commentId: String? = null
    var userName: String? = null
    var mainText: String? = null
    var time: String? = null
    var replys: HashMap<String, Reply> = HashMap()

    class Reply {
        var uid: String? = null
        var replyId: String? = null
        var userName: String? = null
        var mainText: String? = null
        var time: String? = null
    }
}