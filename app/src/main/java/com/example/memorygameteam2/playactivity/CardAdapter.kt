package com.example.memorygameteam2.playactivity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.memorygameteam2.R

class CardAdapter(
    private val cards: List<Card>,
    private val onClick: (position: Int) -> Unit,
) : RecyclerView.Adapter<CardAdapter.ViewHolder>() {
    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val imgFront: ImageView = v.findViewById(R.id.imgFront)
        val imgBack: ImageView = v.findViewById(R.id.imgBack)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card, parent, false)

        // Get current layout parameters for item_card (specifically FrameLayout)
        val params = view.layoutParams

        // Set the height of each item_card to ~23% of parent (recycler view)
        params.height = (parent.height * 0.23).toInt() - 5

        // Set parameter with updated height back to each item_card
        view.layoutParams = params

        // Now 4 rows of item_card will fit dynamically on different sized phones
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        pos: Int,
    ) {
        val card = cards[pos]

        // not face up & not matched: show back
        holder.imgBack.visibility =
            if (!card.isFaceUp && !card.isMatched) View.VISIBLE else View.GONE
        // face up or matched: show front
        holder.imgFront.visibility =
            if (card.isFaceUp || card.isMatched) View.VISIBLE else View.GONE
        if (card.isFaceUp || card.isMatched) {
            // holder.imgFront.setImageResource(card.imageRes) // old code
            holder.imgFront.setImageBitmap(card.image)
        }

        holder.itemView.setOnClickListener {
            if (!card.isFaceUp && !card.isMatched) {
                onClick(pos)
            }
        }
    }

    override fun getItemCount(): Int = cards.size
}
