package model

data class User(
    val login: String,
    val followers: Int,
    val following: Int,
    val created_at: String
)
