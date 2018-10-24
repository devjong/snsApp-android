package com.example.jong.loginactivity1.model

data class FollowDTO (
    var followCount: Int = 0,
    var followers : MutableMap<String,Boolean> = HashMap(),

    var followingCount : Int = 0,
    var followings : MutableMap<String, Boolean> = HashMap()

)