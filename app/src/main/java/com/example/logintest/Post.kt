package com.example.logintest

class Post {
    var dorm: String? = null
    var foodCategory: String? = null
    var restaurantName: String? = null
    var minDeliveryFee: Int? = null
    var maxDeliveryFee: Int? = null
    var timeLimit: String? = null
    var mainText: String? = null
    var uid: String? = null

    class User (val name : String? = null, val profileImageUrl : String? = null, val pid : String? = null)
}