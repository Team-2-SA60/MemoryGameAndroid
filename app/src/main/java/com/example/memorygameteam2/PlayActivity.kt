package com.example.memorygameteam2

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.Chronometer
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memorygameteam2.model.Game
import com.example.memorygameteam2.playactivity.Card
import com.example.memorygameteam2.playactivity.CardAdapter
import com.example.memorygameteam2.soundeffect.SoundManager
import com.example.memorygameteam2.utils.RetroFitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PlayActivity : AppCompatActivity() {
    companion object {
        private const val TOTAL_PAIRS = 6
        private const val PREFS_NAME = "game_prefs"
        private const val KEY_IS_PREMIUM = "isPremium"
    }

    private lateinit var cards: MutableList<Card>
    private var firstPos: Int? = null // save first tapped card's pos
    private var matches = 0
    private var soundEnabled = true
    private lateinit var timer: Chronometer
    private lateinit var tvMatches: TextView
    private lateinit var advert: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_play)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // check if user isPremium
        // dummy test: seed a fake premium flag
        val gamePrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        if (!gamePrefs.contains(KEY_IS_PREMIUM)) {
            gamePrefs.edit {
                putBoolean(KEY_IS_PREMIUM, true)
            }
        }
        // real data usage
        val isPremium = gamePrefs.getBoolean(KEY_IS_PREMIUM, false)
        advert = findViewById<ImageView>(R.id.ivAdvert)
        advert.visibility = if (isPremium) View.GONE else View.VISIBLE

        // ref timer, matches
        timer = findViewById(R.id.timer)
        tvMatches = findViewById(R.id.tvMatches)

        // set matches as 0 / TOTAL_PAIRS
        tvMatches.text = getString(R.string.matches, matches, TOTAL_PAIRS)

        // create deck + start timer
        cards = createDeck()
        timer.start()

        // setup RecyclerView with 4 x 3 cards
        val rv = findViewById<RecyclerView>(R.id.rvCards)
        rv.layoutManager = GridLayoutManager(this, 3)
        rv.adapter = CardAdapter(cards) { pos -> onCardClicked(pos, rv.adapter as CardAdapter) }

        // control bg music
        val prefs = getSharedPreferences("music", MODE_PRIVATE)
        soundEnabled = prefs.getBoolean("isOn", true)
        SoundManager.controlBackgroundMusic(
            this,
            if (soundEnabled) {
                SoundManager.RESUME_BACKGROUND_MUSIC
            } else {
                SoundManager.PAUSE_BACKGROUND_MUSIC
            },
        )
        // bind toggle for sound
        val soundSwitch = findViewById<SwitchCompat>(R.id.swSound)
        soundSwitch.isChecked = soundEnabled
        soundSwitch.setOnCheckedChangeListener { _, isOn ->
            soundEnabled = isOn
            prefs.edit { putBoolean("isOn", isOn) }
            SoundManager.controlBackgroundMusic(
                this@PlayActivity,
                if (isOn) {
                    SoundManager.RESUME_BACKGROUND_MUSIC
                } else {
                    SoundManager.PAUSE_BACKGROUND_MUSIC
                },
            )
        }
    }

    private fun createDeck(): MutableList<Card> {
        val images =
            listOf(
                R.drawable.bird_1,
                R.drawable.bird_2,
                R.drawable.bird_3,
                R.drawable.bird_4,
                R.drawable.bird_5,
                R.drawable.bird_6,
            )
        val cards = mutableListOf<Card>()
        var id = 1
        for (img in images) {
            cards += Card(id++, img)
            cards += Card(id++, img)
        }
        cards.shuffle() // shuffle elements in mutable list
        return cards
    }

    private fun onCardClicked(
        pos: Int,
        adapter: CardAdapter,
    ) {
        // play sound
        if (soundEnabled) {
            SoundManager.playCardFlip(this)
        }

        // flip card face up
        cards[pos].isFaceUp = true
        adapter.notifyItemChanged(pos)

        if (firstPos != null) {
            val prev = firstPos!!
            // if second tapped card matches
            if (cards[prev].imageRes == cards[pos].imageRes) {
                cards[prev].isMatched = true
                cards[pos].isMatched = true
                matches++
                tvMatches.text = getString(R.string.matches, matches, TOTAL_PAIRS)

                // check if game won
                if (matches == TOTAL_PAIRS) {
                    onGameWin()
                }
            } else {
                // no match -> flip back after some time
                Handler(Looper.getMainLooper()).postDelayed({
                    cards[prev].isFaceUp = false
                    cards[pos].isFaceUp = false
                    adapter.notifyItemChanged(prev)
                    adapter.notifyItemChanged(pos)
                }, 800)
            }
            // reset firstPos for next tap
            firstPos = null
        } else {
            // set pos as the first card tapped
            firstPos = pos
        }
    }

    private fun onGameWin() {
        playWinSound()
        showWinToast()
        // send score backend - hardcoded for testing first
        // val prefs = getSharedPreferences("game_prefs", MODE_PRIVATE)
        // val userId = prefs.getInt("userId", 0)
        val userId = 1
        val elapsedSeconds = computeElapsedSeconds()
        postScore(userId, elapsedSeconds)
        launchLeaderboard(elapsedSeconds)
    }

    private fun playWinSound() {
        if (soundEnabled) {
            SoundManager.playGameWin(this)
        }
    }

    private fun showWinToast() {
        Toast.makeText(this, "You Win!", Toast.LENGTH_LONG).show()
    }

    private fun computeElapsedSeconds(): Int {
        timer.stop()
        val elapsedMs = SystemClock.elapsedRealtime() - timer.base
        return (elapsedMs / 1000).toInt()
    }

    private fun postScore(
        userId: Int,
        elapsedSeconds: Int,
    ) {
        val game = Game(
            userId = userId,
            completionTime = elapsedSeconds,
            date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetroFitClient.api.createGame(game)

                if (!response.isSuccessful) {
                    throw IOException("HTTP ${response.code()} ${response.message()}")
                }

                // success
                withContext(Dispatchers.Main) {
                    Log.d("PlayActivity", "Score posted: ${response.body()}")
                }
            } catch (e: Exception) {
                // catch network failures, HTTP errors
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@PlayActivity,
                        "Error posting score: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                Log.e("PlayAPI", "Exception", e)
            }
        }
    }

    private fun launchLeaderboard(elapsedSeconds: Int) {
        val intent =
            Intent(this, LeaderboardActivity::class.java).apply {
                putExtra("finishTime", elapsedSeconds)
            }
        startActivity(intent)
        finish()
    }
}
