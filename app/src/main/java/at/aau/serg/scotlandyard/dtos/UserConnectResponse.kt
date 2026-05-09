package at.aau.serg.scotlandyard.dtos

data class UserConnectResponse(
    val success: Boolean,
    val message: String,
    val user: User?
)

data class User(
    val id: String,
    val nickName: String
)