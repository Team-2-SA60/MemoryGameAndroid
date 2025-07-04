package com.example.memorygameteam2

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Base64
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.xml.KonfettiView
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class PlayActivity : AppCompatActivity() {
    companion object {
        private const val TOTAL_PAIRS = 6
        const val CURRENT_GAME_ID = "CURRENT_GAME_ID"
    }

    private lateinit var cards: MutableList<Card>
    private var firstPos: Int? = null // save first tapped card's pos
    private var matches = 0
    private var soundEnabled = true
    private lateinit var timer: Chronometer
    private lateinit var tvMatches: TextView
    private lateinit var advert: ImageView
    private var adJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_play)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // display ads
        val prefsHelper = PrefsHelper(this)
        val isPremium = prefsHelper.isPremium()
        advert = findViewById<ImageView>(R.id.ivAdvert)
        advert.visibility = if (isPremium) View.GONE else View.VISIBLE
        if (!isPremium) {
            adJob = startAdRotation()
        }

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

    // testing createDeck() with selected images
    private fun createDeck(): MutableList<Card> {
        val imagePosArray = intent.getIntegerArrayListExtra("imageList")
        val fileDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val cards = mutableListOf<Card>()
        var id = 1

        imagePosArray?.forEach { pos ->
            var file = File(fileDir, "image_$pos.jpg")
            var bitmap = BitmapFactory.decodeFile(file.absolutePath)
            cards += Card(id++, bitmap)
            cards += Card(id++, bitmap)
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
            // if (cards[prev].imageRes == cards[pos].imageRes) { // old code
            if (cards[prev].image == cards[pos].image) {
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
        // play win sound
        if (soundEnabled) {
            SoundManager.playGameWin(this)
        }
        celebrateWin()

        // show win toast
        Toast.makeText(this, "You Win!", Toast.LENGTH_LONG).show()

        // send score backend
        val prefsHelper = PrefsHelper(this)
        val userId = prefsHelper.getUserID()
        val userIdInt = userId?.toIntOrNull() ?: 0
        val elapsedSeconds = computeElapsedSeconds()
        postScore(userIdInt, elapsedSeconds)
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
        val game =
            Game(
                userId = userId,
                completionTime = elapsedSeconds,
                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetroFitClient.api.createGame(game)

                if (!response.isSuccessful) {
                    throw IOException("HTTP ${response.code()} ${response.message()}")
                }

                // success
                val dto = response.body()!!
                val currentGameId = dto.gameId
                Log.d("Current game id", "$currentGameId")
                withContext(Dispatchers.Main) {
                    delay(2500)
                    launchLeaderboard(currentGameId)
                }
            } catch (e: Exception) {
                // catch network failures, HTTP errors
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@PlayActivity,
                        "Error posting score: ${e.localizedMessage}",
                        Toast.LENGTH_LONG,
                    ).show()
                }
                Log.e("PlayActivity", "postScore failed", e)
            }
        }
    }

    // confetti
    private fun celebrateWin() {
        val konfettiView = findViewById<KonfettiView>(R.id.konfettiView)
        val party =
            Party(
                speed = 0f,
                maxSpeed = 30f,
                damping = 0.9f,
                spread = 360,
                colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
                emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100),
                position = Position.Relative(0.5, 0.3),
            )
        konfettiView.start(party)
    }

    private fun launchLeaderboard(gameId: Int?) {
        val intent =
            Intent(this, LeaderboardActivity::class.java).apply {
                putExtra(CURRENT_GAME_ID, gameId)
            }
        startActivity(intent)
    }

    // fetch ad every 30s
    private fun startAdRotation(): Job {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        return scope.launch {
            while (isActive) {
                try {
                    val response = RetroFitClient.api.getRandomAdvertisement()
                    if (response.isSuccessful) {
                        val bytes = Base64.decode(response.body()!!.bytes(), Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        withContext(Dispatchers.Main) {
                            advert.setImageBitmap(bitmap)
                            advert.requestLayout()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("PlayActivity", "Error fetching ad", e)
                }
                delay(30_000)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        adJob?.cancel()
    }
}
