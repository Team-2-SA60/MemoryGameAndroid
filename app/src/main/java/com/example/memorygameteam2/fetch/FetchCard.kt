package com.example.memorygameteam2.fetch

import android.graphics.Bitmap

data class FetchCard(
    var image: Bitmap? = null,
    var isSelected: Boolean = false,
)
