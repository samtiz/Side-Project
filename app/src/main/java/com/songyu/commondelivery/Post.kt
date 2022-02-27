package com.songyu.commondelivery

import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class Post {
    var dorm: String? = null
    var foodCategories: ArrayList<String>? = null
    var restaurantName: String? = null
    var minDeliveryFee: Int? = null
    var maxDeliveryFee: Int? = null
    var timeLimit: String? = null
    var mainText: String? = null
    var uid: String? = null
    // 현태가 수정한 부분 /////
    var postId: String? = null
    var users : HashMap<String, String> = HashMap()
    //class userStat(val nickname : String? = null, var index : Int? = null, var stat : Boolean? = null)
    //var usersId : ArrayList<String>? = null
    //val userName : MutableSet<String>? = null
    var visibility: Boolean = true
    // TODO 댓글 데이터 클래스 만들어서 val inquires 에 빈 ArrayList로 init
    //////////////////////
    var comments: HashMap<String, PostComment> = HashMap()
}