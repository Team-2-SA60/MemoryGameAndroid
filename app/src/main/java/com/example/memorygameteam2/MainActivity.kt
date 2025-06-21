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

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var menuAdapter: MenuAdapter
    private var intent: Intent = Intent()

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

        initRecyclerView()
    }

    fun initRecyclerView() {
        binding.menuRecyclerView.layoutManager = GridLayoutManager(this, 2)
        menuAdapter =
            MenuAdapter(menuList) { selectedItem ->
                launch(selectedItem)
            }
        binding.menuRecyclerView.adapter = menuAdapter
    }

    fun launch(selectedItem: Menu) {
        when (selectedItem.text.lowercase()) {
            "leaderboard" -> {
                intent = Intent(this, LeaderboardActivity::class.java)
                startActivity(intent)
            }
        }
    }
}
