package com.example.memorygameteam2.leaderboard

import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.memorygameteam2.R
import com.example.memorygameteam2.model.Rank
import java.util.Locale

class LeaderboardAdapter(
    private var rankingList: List<Rank>,
) : RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userRankView: TextView = view.findViewById<TextView>(R.id.user_rank)
        val userAvatarView: ImageView = view.findViewById<ImageView>(R.id.user_avatar)
        val userNameView: TextView = view.findViewById<TextView>(R.id.user_name)
        val userTimeView: TextView = view.findViewById<TextView>(R.id.user_time)
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
        // User rank
        var rank = (position + 1).toString()

        // User Avatar Image
        val avatarImageBytes = Base64.decode(item.avatarImage, Base64.DEFAULT)
        val avatarImage = BitmapFactory.decodeByteArray(avatarImageBytes, 0, avatarImageBytes.size)

        // Beautify rankings, if rank = 1..3, Bold text and show a medal beside
        if (rank in listOf("1", "2", "3")) {
            holder.userRankView.setTypeface(null, Typeface.BOLD)
            holder.userRankView.textSize = 18f
        }

        when (rank) {
            "1" -> {
                rank = "🥇 $rank"
            }
            "2" -> {
                rank = "🥈 $rank"
            }
            "3" -> {
                rank = "🥉 $rank"
            }
            else -> {
                rank
            }
        }

        // Set views
        holder.userRankView.text = rank
        holder.userAvatarView.setImageBitmap(avatarImage)
        holder.userNameView.text = item.username
        holder.userTimeView.text = formatSeconds(item.completionTime)
    }

    override fun getItemCount(): Int = rankingList.size

    fun updateData(newRankingList: List<Rank>) {
        rankingList = newRankingList
        notifyDataSetChanged()
    }

    fun formatSeconds(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60

        return String.format(Locale.US, "%02d:%02d", minutes, remainingSeconds)
    }
}
