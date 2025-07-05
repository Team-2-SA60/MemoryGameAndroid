package com.example.memorygameteam2.fetch

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.memorygameteam2.R

class FetchCardAdapter(
    private val cardList: List<FetchCard>,
    private val onClick: (position: Int) -> Unit,
) : RecyclerView.Adapter<FetchCardAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById<CardView>(R.id.view_card)
        val fetchedImage: ImageView = itemView.findViewById(R.id.fetch_card)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_fetch, parent, false)

        // Get current layout parameters for item_fetch (specifically CardView)
        val params = view.layoutParams

        // Set the height of each item_fetch to ~19% of parent (recycler view)
        params.height = (parent.height * 0.19).toInt()

        // Set parameter with updated height back to each item_fetch
        view.layoutParams = params

        // Now 4 rows of item_fetch will fit dynamically on different sized phones
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        pos: Int,
    ) {
        val card = cardList[pos]

        if (card.image != null) {
            holder.fetchedImage.setImageBitmap(card.image) // sets image for fetch_card ImageView
        } else {
            holder.fetchedImage.setImageResource(R.drawable.card_back) // set default for ImageView before loading
        }

        if (card.isSelected) {
            // if card is selected, make it look "popped up"
            holder.cardView.apply {
                elevation = 16f
                scaleX = 1.05f
                scaleY = 1.05f
                cardElevation = 16f
                radius = 60f
                setBackgroundColor(Color.LTGRAY)
            }
        } else {
            // if card is not selected or deselected, show normal state
            holder.cardView.apply {
                elevation = 4f
                scaleX = 1f
                scaleY = 1f
                cardElevation = 4f
                radius = 8f
                setBackgroundColor(Color.TRANSPARENT)
            }
        }

        holder.itemView.setOnClickListener {
            onClick(pos)
        }
    }

    override fun getItemCount(): Int = cardList.size
}
