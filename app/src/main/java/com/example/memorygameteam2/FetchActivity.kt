package com.example.memorygameteam2

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memorygameteam2.fetch.FetchCard
import com.example.memorygameteam2.fetch.FetchCardAdapter

class FetchActivity : AppCompatActivity() {
    private lateinit var fetchList: MutableList<FetchCard>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_fetch)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // fetch list of images // WIP: to be changed to fetch from URL
        fetchList = fetchImages()

        // set up RecyclerView with 4 columns
        var rv = findViewById<RecyclerView>(R.id.fetch_rv)
        rv.layoutManager = GridLayoutManager(this, 4)
        rv.adapter = FetchCardAdapter(fetchList)
    }

    private fun fetchImages(): MutableList<FetchCard> {
        val images = listOf (
            R.drawable.bird_1,
            R.drawable.bird_2,
            R.drawable.bird_3,
            R.drawable.bird_4,
            R.drawable.bird_5,
            R.drawable.bird_6,
            R.drawable.bird_7,
            R.drawable.bird_8,
            R.drawable.bird_9,
            R.drawable.bird_10,
            R.drawable.bird_11,
            R.drawable.bird_12,
            R.drawable.bird_13,
            R.drawable.bird_14,
            R.drawable.bird_15,
            R.drawable.bird_16,
            R.drawable.bird_17,
            R.drawable.bird_18,
            R.drawable.bird_19,
            R.drawable.bird_20
        )
        var fetchCards = mutableListOf<FetchCard>()
        for (img in images) {
            fetchCards.add(FetchCard(img))
        }
        // WIP: if count = 20, stop fetching
        return fetchCards
    }

}