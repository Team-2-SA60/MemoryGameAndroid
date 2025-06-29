package com.example.memorygameteam2

import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.memorygameteam2.databinding.ActivityLeaderboardBinding
import com.example.memorygameteam2.leaderboard.LeaderboardAdapter
import com.example.memorygameteam2.model.Rank
import com.example.memorygameteam2.soundeffect.SoundManager
import com.example.memorygameteam2.utils.RetroFitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        rankingList = listOf<Rank>()
        binding.btnGroup.selectButton(binding.todayBtn)
        fetchRankingList(1)
    }

    private fun initRecyclerView() {
        val animation: LayoutAnimationController? =
            AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_fall_down)
        binding.leaderboardRecyclerView.layoutAnimation = animation
        binding.leaderboardRecyclerView.itemAnimator = DefaultItemAnimator()
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
                fetchRankingList(1)
            }

            last7DaysBtn.setOnClickListener {
                SoundManager.playButtonClick(this@LeaderboardActivity)
                fetchRankingList(7)
            }

            last30DaysBtn.setOnClickListener {
                SoundManager.playButtonClick(this@LeaderboardActivity)
                fetchRankingList(30)
            }

            allTimeBtn.setOnClickListener {
                SoundManager.playButtonClick(this@LeaderboardActivity)
                fetchRankingList()
            }
        }
    }

    // Fetch API request using Retrofit with Coroutine
    private fun fetchRankingList(daysAgo: Int = 0) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetroFitClient.api.getTopGames(daysAgo)
                if (response.isSuccessful) {
                    val ranks = response.body()!!.toList()
                    withContext(Dispatchers.Main) {
                        updateRankingList(ranks)
                    }
                } else {
                    // This is when response not between code 200-300
                    // so Not FOUND, UNAUTHORIZED etc will go through below
                    withContext(Dispatchers.Main) {
                        showToast("Failed to retrieve ranking list ${response.code()}")
                    }
                    Log.e("API_error", "Response failed: ${response.code()}")
                }
            } catch (e: Exception) {
                // This is when there's an issue with the API request
                // Like erm daysAgo != Int, then it will catch exception and go through below
                // Or when URL does not exist or return anything
                withContext(Dispatchers.Main) {
                    showToast(e.toString())
                }
                Log.e("API_exception", e.toString())
            }
        }
    }

    // Update recycler view with new ranking list with animations
    private fun updateRankingList(newRankingList: List<Rank>) {
        // Fade out current list (VISIBLE -> INVISIBLE)
        binding.leaderboardRecyclerView.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                // Update old Ranking list in adapter
                leaderboardAdapter.updateData(newRankingList)

                // start the falldown animation
                binding.leaderboardRecyclerView.scheduleLayoutAnimation()

                // fade in the list (INVISIBLE -> VISIBLE)
                binding.leaderboardRecyclerView.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start()
            }
            .start()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG)
            .show()
    }
}
