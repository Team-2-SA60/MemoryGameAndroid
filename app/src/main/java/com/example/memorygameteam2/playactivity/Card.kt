package com.example.memorygameteam2.playactivity

import android.graphics.Bitmap

data class Card(
    val id: Int,
//    val imageRes: Int,
    val image: Bitmap,
    var isFaceUp: Boolean = false,
    var isMatched: Boolean = false,
)
