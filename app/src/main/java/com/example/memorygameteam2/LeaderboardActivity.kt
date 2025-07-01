package com.example.memorygameteam2

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.ImageView
import android.widget.TextView
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
import java.io.IOException
import java.util.Locale

class LeaderboardActivity : AppCompatActivity() {
    companion object {
        const val CURRENT_GAME_ID = "CURRENT_GAME_ID"
    }

    private lateinit var binding: ActivityLeaderboardBinding
    private lateinit var leaderboardAdapter: LeaderboardAdapter
    private lateinit var rankingList: List<Rank>
    private val currentGameId: Int by lazy {
        intent.getIntExtra(CURRENT_GAME_ID, -1)
    }

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
        showPlayerCard()
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

        leaderboardAdapter =
            LeaderboardAdapter(
                rankingList,
                currentGameId,
            )
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
                    throw IOException("HTTP ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                // This is when there's an issue with the API request
                // Like erm daysAgo != Int, then it will catch exception and go through below
                // Or when URL does not exist or return anything
                withContext(Dispatchers.Main) {
                    showToast(e.toString())
                }
                Log.e("LeaderboardAPI", "Exception", e)
            }
        }
    }

    // Update recycler view with new ranking list with animations
    private fun updateRankingList(newRankingList: List<Rank>) {
        val rv = binding.leaderboardRecyclerView
        // Fade out current list (VISIBLE -> INVISIBLE)
        rv.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                // Update old Ranking list in adapter
                leaderboardAdapter.updateData(newRankingList)

                // start the falldown animation
                rv.scheduleLayoutAnimation()

                // fade in the list (INVISIBLE -> VISIBLE)
                rv.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .withEndAction {
                        // scroll to leaderboard position if any
                        val highlightPos =
                            newRankingList.indexOfFirst { it.gameId == currentGameId }
                        if (highlightPos >= 0) {
                            rv.postDelayed({
                                rv.smoothScrollToPosition(highlightPos)
                            }, 800)
                        }
                    }
                    .start()
            }
            .start()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG)
            .show()
    }

    private fun showPlayerCard() {
        val gameId = intent.getIntExtra(CURRENT_GAME_ID, -1)
        if (gameId < 0) return

        val card = findViewById<View>(R.id.current_player_card)
        val avatar = card.findViewById<ImageView>(R.id.user_avatar)
        val username = card.findViewById<TextView>(R.id.user_name)
        val completedTime = card.findViewById<TextView>(R.id.user_time)

        CoroutineScope(Dispatchers.Main).launch {
            val dto =
                withContext(Dispatchers.IO) {
                    val resp = RetroFitClient.api.findGame(gameId)
                    if (!resp.isSuccessful) throw IOException("HTTP ${resp.code()}")
                    resp.body()!!
                }

            card.apply {
                findViewById<TextView>(R.id.user_rank).visibility = View.GONE // hide user rank

                // set avatar
                dto.avatarImage?.let { b64 ->
                    val bytes = Base64.decode(b64, Base64.DEFAULT)
                    avatar.setImageBitmap(
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size),
                    )
                }

                // set name & time
                username.text = dto.username
                dto.completionTime?.let { secs ->
                    val min = secs / 60
                    val sec = secs % 60
                    completedTime.text = String.format(Locale.US, "Time: %02d:%02d", min, sec)
                }

                visibility = View.VISIBLE
            }
        }
    }
}
