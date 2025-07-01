package com.example.memorygameteam2.model

import com.google.gson.annotations.SerializedName

data class GameDto(
    @SerializedName("gameId") val gameId: Int?,
    @SerializedName("userId") val userId: Int?,
    @SerializedName("username") val username: String?,
    @SerializedName("completionTime") val completionTime: Int?,
    @SerializedName("avatarImage") val avatarImage: String?,
)
