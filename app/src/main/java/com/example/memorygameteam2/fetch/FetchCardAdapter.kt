package com.example.memorygameteam2.fetch

import android.graphics.Color
import android.util.Log.v
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.memorygameteam2.R

class FetchCardAdapter(
    private val cardList: List<FetchCard>
) : RecyclerView.Adapter<FetchCardAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fetchedImage: ImageView = itemView.findViewById(R.id.fetch_card)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_fetch, parent, false)
    )

    override fun onBindViewHolder(
        holder: ViewHolder,
        pos: Int
    ) {
        val card = cardList[pos]
        holder.fetchedImage.setImageResource(card.imageRes)

        holder.itemView.setOnClickListener {
            if (card.isSelected) {
                card.isSelected = false
                holder.fetchedImage.elevation = 2f // card does not float if deselected
            } else {
                card.isSelected = true
                holder.fetchedImage.elevation = 8f // card floats up when selected
            }
        }
    }

    override fun getItemCount(): Int = cardList.size
}