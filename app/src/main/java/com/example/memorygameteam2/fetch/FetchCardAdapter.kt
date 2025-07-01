package com.example.memorygameteam2.fetch

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
    ) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_fetch, parent, false),
    )

    override fun onBindViewHolder(
        holder: ViewHolder,
        pos: Int,
    ) {
        val card = cardList[pos]
        holder.fetchedImage.setImageBitmap(card.image) // sets image for fetch_card ImageView

        if (card.isSelected) {
            // if card is selected, make it look "popped up"
            holder.cardView.apply {
                elevation = 16f
                scaleX = 1.05f
                scaleY = 1.05f
                cardElevation = 16f
                radius = 12f
            }
        } else {
            // if card is not selected or deselected, show normal state
            holder.cardView.apply {
                elevation = 4f
                scaleX = 1f
                scaleY = 1f
                cardElevation = 4f
                radius = 8f
            }
        }

        holder.itemView.setOnClickListener {
            onClick(pos)
        }
    }

    override fun getItemCount(): Int = cardList.size
}
