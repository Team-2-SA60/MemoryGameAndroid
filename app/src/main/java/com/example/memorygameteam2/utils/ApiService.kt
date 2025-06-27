package com.example.memorygameteam2.utils

import com.example.memorygameteam2.model.Rank
import com.example.memorygameteam2.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("api/user/login")
    suspend fun validateUser(
        @Body user: User,
    ): Response<User>

    @GET("api/game/top10")
    suspend fun getTopGames(
        @Query("daysAgo") daysAgo: Int,
    ): Response<List<Rank>>
}
