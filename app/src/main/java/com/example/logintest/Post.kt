package com.example.logintest

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
    val users : HashMap<String, String> = HashMap()
    //var usersId : ArrayList<String>? = null
    //val userName : MutableSet<String>? = null
    //////////////////////
}