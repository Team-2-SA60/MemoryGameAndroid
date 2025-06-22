package com.example.memorygameteam2

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.memorygameteam2.databinding.ActivityMainBinding
import com.example.memorygameteam2.menu.Menu
import com.example.memorygameteam2.menu.MenuAdapter
import com.example.memorygameteam2.soundeffect.SoundManager

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var menuAdapter: MenuAdapter
    private var activityIntent = Intent()

    // temporary items on menu for testing/developing purpose
    private val menuList =
        listOf(
            Menu("Login"),
            Menu("Fetch"),
            Menu("Play"),
            Menu("Leaderboard"),
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        SoundManager.controlBackgroundMusic(this, SoundManager.PLAY_BACKGROUND_MUSIC)
        initRecyclerView()
        initButtons()
    }

    private fun initRecyclerView() {
        binding.menuRecyclerView.layoutManager = GridLayoutManager(this, 2)
        menuAdapter =
            MenuAdapter(menuList) { selectedItem ->
                SoundManager.playButtonClick(this)
                launch(selectedItem)
            }
        binding.menuRecyclerView.adapter = menuAdapter
    }

    private fun initButtons() {
        // tracker to determine if background music is playing
        var musicIsPlaying = true
        binding.apply {
            // Toggle background music
            backgroundMusicBtn.setOnClickListener {
                SoundManager.playButtonClick(this@MainActivity)
                if (musicIsPlaying) {
                    // stops background music and change button text
                    SoundManager.controlBackgroundMusic(this@MainActivity, SoundManager.STOP_BACKGROUND_MUSIC)
                    backgroundMusicBtn.text = "Turn ON music"
                } else {
                    // plays background music and change button text
                    SoundManager.controlBackgroundMusic(this@MainActivity, SoundManager.PLAY_BACKGROUND_MUSIC)
                    backgroundMusicBtn.text = "Turn OFF music"
                }
                musicIsPlaying = !musicIsPlaying
            }
        }
    }

    // temporary menu activity explicit intents
    private fun launch(selectedItem: Menu) {
        when (selectedItem.text.lowercase()) {
            // launch leaderboard activity
            "leaderboard" -> {
                activityIntent = Intent(this, LeaderboardActivity::class.java)
                startActivity(activityIntent)
            }
            "play" -> {
                intent = Intent(this, PlayActivity::class.java)
                startActivity(intent)
            }
        }
    }

    // play/stop background music
//    private fun toggleBackgroundMusic(action: String) {
//        musicIntent = Intent(this, SoundService::class.java)
//        musicIntent.setAction(action)
//        musicIntent.putExtra("song", R.raw.gamebg)
//        startService(musicIntent)
//    }
}
