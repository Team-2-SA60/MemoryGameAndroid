package com.example.memorygameteam2.utils

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetroFitClient {
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://152.42.175.43/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
