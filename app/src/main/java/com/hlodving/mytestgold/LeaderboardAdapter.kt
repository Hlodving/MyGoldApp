package com.hlodving.mytestgold

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.hlodving.mytestgold.databinding.ItemLeaderBinding

data class LeaderboardRow(
    val position: Int,
    val alias: String,
    val score: Int,
    val isMe: Boolean
)

class LeaderboardAdapter : RecyclerView.Adapter<LeaderboardAdapter.VH>() {
    private val items = mutableListOf<LeaderboardRow>()

    fun submit(rows: List<LeaderboardRow>) {
        items.clear()
        items.addAll(rows)
        notifyDataSetChanged()
    }

    class VH(val b: ItemLeaderBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemLeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.b.tvPos.text = item.position.toString()
        holder.b.tvAlias.text = item.alias
        holder.b.tvScore.text = item.score.toString()

        // Подсветка своей строки (опционально)
        val ctx = holder.itemView.context
        val color = if (item.isMe)
            ContextCompat.getColor(ctx, R.color.widget_hint_color)
        else
            ContextCompat.getColor(ctx, android.R.color.transparent)
        holder.itemView.setBackgroundColor(color)
    }

    override fun getItemCount() = items.size
}
