package com.example.memorygameteam2.menu

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.memorygameteam2.R
import com.example.memorygameteam2.soundeffect.SoundEffectPlayer

class MenuAdapter(
    private val menuList: List<Menu>,
    private val onItemClick: (Menu) -> Unit,
) : RecyclerView.Adapter<MenuAdapter.ViewHolder>() {
    private var parentContext: Context? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        parentContext = parent.context
        val view = LayoutInflater.from(parent.context).inflate(R.layout.items_menu, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        val item = menuList[position]
        holder.cardTextView.text = item.text

        holder.itemView.setOnClickListener {
            SoundEffectPlayer.buttonClick(parentContext!!)
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = menuList.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardTextView = itemView.findViewById<TextView>(R.id.card_text_view)
    }
}
