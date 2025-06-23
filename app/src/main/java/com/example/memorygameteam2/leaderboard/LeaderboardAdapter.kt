package com.example.memorygameteam2.leaderboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.memorygameteam2.R
import com.example.memorygameteam2.model.Rank

class LeaderboardAdapter(
    private var rankingList: List<Rank>,
) : RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userRankView = view.findViewById<TextView>(R.id.user_rank)
        val userAvatarView = view.findViewById<ImageView>(R.id.user_avatar)
        val userNameView = view.findViewById<TextView>(R.id.user_name)
        val userTimeView = view.findViewById<TextView>(R.id.user_time)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_rank, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        val item = rankingList[position]
        holder.userRankView.text = item.rank.toString()
        holder.userAvatarView.setImageResource(item.avatar)
        holder.userNameView.text = item.username
        holder.userTimeView.text = item.time
    }

    override fun getItemCount(): Int = rankingList.size

    fun updateData(newRankingList: List<Rank>) {
        rankingList = newRankingList
        notifyDataSetChanged()
    }
}
