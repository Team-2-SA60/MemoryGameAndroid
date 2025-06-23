package com.example.memorygameteam2.model

import com.example.memorygameteam2.R

object RankDAO {
    private val todayRankList =
        listOf(
            Rank(1, R.drawable.bird_3, "Zoe Zamboni", "00:58"),
            Rank(2, R.drawable.bird_6, "Liam Lightning", "01:02"),
            Rank(3, R.drawable.bird_2, "Maya Matrix", "01:10"),
        )

    private val last7RankList =
        listOf(
            Rank(1, R.drawable.bird_2, "Maya Matrix", "00:56"),
            Rank(2, R.drawable.bird_4, "Charlie and the chocolate factory", "00:59"),
            Rank(3, R.drawable.bird_1, "Alice in wonderland", "01:01"),
            Rank(4, R.drawable.bird_5, "Ed, Edd n Eddy", "01:05"),
            Rank(5, R.drawable.bird_6, "Zane Zenith", "01:08"),
            Rank(6, R.drawable.bird_3, "Liam Lightning", "01:12"),
        )

    private val last30RankList =
        listOf(
            Rank(1, R.drawable.bird_6, "Fred Mercury", "00:53"),
            Rank(2, R.drawable.bird_1, "Alice in wonderland", "00:55"),
            Rank(3, R.drawable.bird_5, "Nova Neptune", "00:57"),
            Rank(4, R.drawable.bird_3, "Charlie and the chocolate factory", "01:00"),
            Rank(5, R.drawable.bird_2, "Bob the builder", "01:02"),
            Rank(6, R.drawable.bird_4, "Xena Xylophone", "01:03"),
        )

    private val allTimeRankList =
        listOf(
            Rank(1, R.drawable.bird_1, "Fred Mercury", "00:49"),
            Rank(2, R.drawable.bird_3, "Maya Matrix", "00:50"),
            Rank(3, R.drawable.bird_2, "Alice in wonderland", "00:52"),
            Rank(4, R.drawable.bird_4, "Bob the builder", "00:55"),
            Rank(5, R.drawable.bird_5, "Charlie and the chocolate factory", "00:58"),
            Rank(6, R.drawable.bird_6, "Zoe Zamboni", "00:59"),
        )

    fun getRankList(type: String): List<Rank> {
        return when (type) {
            "today" -> todayRankList
            "last7" -> last7RankList
            "last30" -> last30RankList
            "allTime" -> allTimeRankList
            else -> todayRankList
        }
    }
}
