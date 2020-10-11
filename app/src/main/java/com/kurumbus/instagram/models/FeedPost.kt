package com.kurumbus.instagram.models

import com.google.firebase.database.Exclude
import com.google.firebase.database.ServerValue
import java.sql.Date

data class FeedPost(val uid: String = "",
                    val username: String = "",
                    val image: String = "",
                    val likesCount: Int = 0,
                    @Exclude val commentsCount: Int = 0,
                    val caption: String = "",
                    val comments: List<Comment> = emptyList(),
                    val timestamp: Any = ServerValue.TIMESTAMP,
                    val photo: String? = "",
                    @Exclude val id: String? = ""
) {
    fun timestampDate(): Date = Date(timestamp as Long)
}