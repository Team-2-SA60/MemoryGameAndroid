package com.example.memorygameteam2

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.memorygameteam2.databinding.ActivityLeaderboardBinding
import com.example.memorygameteam2.soundeffect.SoundManager

class LeaderboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLeaderboardBinding
    private lateinit var soundManager: SoundManager

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

        initButtons()
    }

    private fun initButtons() {
        soundManager = SoundManager(this)

        binding.apply {
            backButton.setOnClickListener {
                soundManager.play("button")
                finish()
            }

            todayBtn.setOnClickListener {
                soundManager.play("button")
            }

            last7DaysBtn.setOnClickListener {
                soundManager.play("button")
            }

            last30DaysBtn.setOnClickListener {
                soundManager.play("button")
            }

            allTimeBtn.setOnClickListener {
                soundManager.play("button")
            }
        }
    }

    override fun onDestroy() {
        soundManager.release()
        super.onDestroy()
    }
}
