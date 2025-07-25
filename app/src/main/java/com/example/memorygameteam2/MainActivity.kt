package com.example.memorygameteam2

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.memorygameteam2.databinding.ActivityMainBinding
import com.example.memorygameteam2.menu.MenuAdapter
import com.example.memorygameteam2.model.Menu
import com.example.memorygameteam2.soundeffect.SoundManager

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var menuAdapter: MenuAdapter
    private var activityIntent = Intent()
    private var musicIsOn: Boolean = true

    // temporary items on menu for testing/developing purpose
    private val menuList =
        mutableListOf(
            Menu("Play"),
            Menu("Login"),
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

        initBackGroundSong()
        initRecyclerView()
        initButtons()
    }

    private fun initBackGroundSong() {
        // tracker to determine if background music is toggled on by user
        musicIsOn = getSharedPreferences("music", MODE_PRIVATE).getBoolean("isOn", true)
        if (musicIsOn) {
            SoundManager.controlBackgroundMusic(this, SoundManager.PLAY_BACKGROUND_MUSIC)
        } else {
            binding.backgroundMusicBtn.text = "Turn ON music"
        }
    }

    private fun initRecyclerView() {
        binding.menuRecyclerView.layoutManager = GridLayoutManager(this, 1)
        menuAdapter =
            MenuAdapter(getCurrentMenuList()) { selectedItem ->
                SoundManager.playButtonClick(this)
                launch(selectedItem)
            }
        binding.menuRecyclerView.adapter = menuAdapter
    }

    private fun initButtons() {
        binding.apply {
            // Toggle background music
            backgroundMusicBtn.setOnClickListener {
                SoundManager.playButtonClick(this@MainActivity)
                if (musicIsOn) {
                    // stops background music and change button text
                    SoundManager.controlBackgroundMusic(this@MainActivity, SoundManager.STOP_BACKGROUND_MUSIC)
                    backgroundMusicBtn.text = "Turn ON music"
                } else {
                    // plays background music and change button text
                    SoundManager.controlBackgroundMusic(this@MainActivity, SoundManager.PLAY_BACKGROUND_MUSIC)
                    backgroundMusicBtn.text = "Turn OFF music"
                }
                musicIsOn = !musicIsOn
                with(getSharedPreferences("music", MODE_PRIVATE).edit()) {
                    putBoolean("isOn", musicIsOn)
                    apply()
                }
            }
        }
    }

    private fun getCurrentMenuList(): List<Menu> {
        val isLoggedIn = !PrefsHelper(this).getUsername().isNullOrEmpty()
        return menuList.map { menuItem ->
            if (menuItem.text.equals("login", ignoreCase = true)) {
                if (isLoggedIn) menuItem.copy(text = "Logout") else menuItem
            } else {
                menuItem
            }
        }
    }

    // temporary menu activity explicit intents
    private fun launch(selectedItem: Menu) {
        when (selectedItem.text.lowercase()) {
            // launch leaderboard activity
            "logout" -> {
                // Perform logout
                PrefsHelper(this).clearUser()
                SoundManager.controlBackgroundMusic(this, SoundManager.STOP_BACKGROUND_MUSIC)
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }

            "login" -> {
                activityIntent = Intent(this, LoginActivity::class.java)
                startActivity(activityIntent)
            }
            "play" -> {
                activityIntent = Intent(this, FetchActivity::class.java)
                startActivity(activityIntent)
            }
        }
    }
}
