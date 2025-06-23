package com.example.memorygameteam2

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.memorygameteam2.databinding.ActivityLeaderboardBinding
import com.example.memorygameteam2.leaderboard.LeaderboardAdapter
import com.example.memorygameteam2.model.Rank
import com.example.memorygameteam2.model.RankDAO
import com.example.memorygameteam2.soundeffect.SoundManager

class LeaderboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLeaderboardBinding
    private lateinit var leaderboardAdapter: LeaderboardAdapter
    private lateinit var rankingList: List<Rank>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLeaderboardBinding.inflate(layoutInflater)

        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initRankList()
        initRecyclerView()
        initButtons()
    }

    private fun initRankList() {
        rankingList = RankDAO.getRankList("today")
    }

    private fun initRecyclerView() {
        binding.leaderboardRecyclerView.layoutManager = LinearLayoutManager(this)
        leaderboardAdapter = LeaderboardAdapter(rankingList)
        binding.leaderboardRecyclerView.adapter = leaderboardAdapter
    }

    private fun initButtons() {
        binding.apply {
            backButton.setOnClickListener {
                SoundManager.playButtonClick(this@LeaderboardActivity)
                finish()
            }

            todayBtn.setOnClickListener {
                SoundManager.playButtonClick(this@LeaderboardActivity)
                rankingList = RankDAO.getRankList("today")
                leaderboardAdapter.updateData(rankingList)
            }

            last7DaysBtn.setOnClickListener {
                SoundManager.playButtonClick(this@LeaderboardActivity)
                rankingList = RankDAO.getRankList("last7")
                leaderboardAdapter.updateData(rankingList)
            }

            last30DaysBtn.setOnClickListener {
                SoundManager.playButtonClick(this@LeaderboardActivity)
                rankingList = RankDAO.getRankList("last30")
                leaderboardAdapter.updateData(rankingList)
            }

            allTimeBtn.setOnClickListener {
                SoundManager.playButtonClick(this@LeaderboardActivity)
                rankingList = RankDAO.getRankList("allTime")
                leaderboardAdapter.updateData(rankingList)
            }
        }
    }
}
