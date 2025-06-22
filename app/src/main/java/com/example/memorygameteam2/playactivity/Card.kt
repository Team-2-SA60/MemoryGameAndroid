package com.example.memorygameteam2.playactivity

data class Card(
    val id: Int,
    val imageRes: Int,
    var isFaceUp: Boolean = false,
    var isMatched: Boolean = false,
)
