package com.example.memorygameteam2

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Chronometer
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memorygameteam2.playactivity.Card
import com.example.memorygameteam2.playactivity.CardAdapter

/*
To DO:
1. Flip sound effect
2. Winning screen
3. Format theme - font etc.
4. Dynamic : Ads, Pictures
 */
class PlayActivity : AppCompatActivity() {
    private lateinit var cards: MutableList<Card>
    private var firstPos: Int? = null // rmbr first tapped card's pos
    private var matches = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_play)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // create deck + start timer
        cards = createDeck()
        findViewById<Chronometer>(R.id.timer).start()

        // setup RecyclerView with 4 x 3 cads
        val rv = findViewById<RecyclerView>(R.id.rvCards)
        rv.layoutManager = GridLayoutManager(this, 3)
        rv.adapter = CardAdapter(cards) { pos -> onCardClicked(pos, rv.adapter as CardAdapter) }

    }

    private fun createDeck(): MutableList<Card> {
        val images = listOf(
            R.drawable.bird_1,
            R.drawable.bird_2,
            R.drawable.bird_3,
            R.drawable.bird_4,
            R.drawable.bird_5,
            R.drawable.bird_6
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

    private fun onCardClicked(pos: Int, adapter: CardAdapter) {
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
                findViewById<TextView>(R.id.tvMatches).text = getString(R.string.matches, matches)
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
}
