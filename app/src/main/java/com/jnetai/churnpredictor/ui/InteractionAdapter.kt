package com.jnetai.churnpredictor.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jnetai.churnpredictor.databinding.ItemInteractionBinding
import com.jnetai.churnpredictor.model.Interaction
import com.jnetai.churnpredictor.model.InteractionType

class InteractionAdapter(
    private val onDelete: (Interaction) -> Unit
) : ListAdapter<Interaction, InteractionAdapter.ViewHolder>(InteractionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemInteractionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemInteractionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(interaction: Interaction) {
            binding.tvType.text = try {
                InteractionType.valueOf(interaction.type).displayName
            } catch (e: Exception) {
                interaction.type
            }
            binding.tvDescription.text = interaction.description
            binding.tvDate.text = interaction.date
            if (interaction.feedbackScore != null) {
                binding.tvFeedbackScore.text = "★ ${String.format("%.1f", interaction.feedbackScore)}"
            } else {
                binding.tvFeedbackScore.text = ""
            }
            binding.btnDelete.setOnClickListener { onDelete(interaction) }
        }
    }

    class InteractionDiffCallback : DiffUtil.ItemCallback<Interaction>() {
        override fun areItemsTheSame(oldItem: Interaction, newItem: Interaction) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Interaction, newItem: Interaction) = oldItem == newItem
    }
}