package com.example.memorygameteam2.utils

import com.example.memorygameteam2.model.Game
import com.example.memorygameteam2.model.GameDto
import com.example.memorygameteam2.model.Rank
import com.example.memorygameteam2.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
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

    @POST("api/game/create")
    suspend fun createGame(
        @Body game: Game,
    ): Response<GameDto>

    @GET("api/game/find/{id}")
    suspend fun findGame(
        @Path("id") gameId: Int,
    ): Response<GameDto>
}
