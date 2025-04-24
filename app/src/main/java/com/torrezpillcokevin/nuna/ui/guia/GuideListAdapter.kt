package com.torrezpillcokevin.nuna.ui.guia

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.torrezpillcokevin.nuna.data.Guide
import com.torrezpillcokevin.nuna.databinding.ItemGuideListBinding

class GuideListAdapter(private val guides: List<Guide>) : RecyclerView.Adapter<GuideListAdapter.GuideItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuideItemViewHolder {
        val binding = ItemGuideListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GuideItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GuideItemViewHolder, position: Int) {
        holder.bind(guides[position])
    }

    override fun getItemCount(): Int = guides.size

    class GuideItemViewHolder(private val binding: ItemGuideListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(guide: Guide) {
            binding.guideTitle.text = guide.subtitle
            binding.guideContent.text = guide.content
        }
    }
}
