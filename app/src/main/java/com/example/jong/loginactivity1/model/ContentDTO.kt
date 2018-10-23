package com.example.jong.loginactivity1.model

import com.facebook.internal.Mutable

data class ContentDTO(
    var explain: String? = null,
    var imageUrl: String? = null,
    var uid: String? = null, // 유저마다의 고유한 해쉬코드
    var userId: String? = null, // 이메일주소
    var timestamp: Long? = null, // 업로드 시간
    var favoriteCount: Int = 0,
    var favorites: MutableMap<String, Boolean> = HashMap() // 좋아요 중복체크해가 위해서
) {
    data class Comment(
        var uid: String? = null,
        var userId: String? = null,
        var comment: String? = null,
        var timestamp: Long? = null
    )

}



