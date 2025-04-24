package com.torrezpillcokevin.nuna.ui.guia

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.torrezpillcokevin.nuna.data.Guide
import com.torrezpillcokevin.nuna.databinding.ItemGuideBinding


class GuidesAdapter(private val guidesByCategory: Map<String, List<Guide>>) : RecyclerView.Adapter<GuidesAdapter.GuideViewHolder>() {

    private val categories = guidesByCategory.keys.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuideViewHolder {
        val binding = ItemGuideBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GuideViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GuideViewHolder, position: Int) {
        val category = categories[position]
        val guides = guidesByCategory[category] ?: emptyList()
        holder.bind(category, guides)
    }

    override fun getItemCount(): Int = categories.size

    class GuideViewHolder(private val binding: ItemGuideBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(category: String, guides: List<Guide>) {
            binding.categoryTitle.text = category

            // Configura el RecyclerView anidado para mostrar las guías de esta categoría
            binding.guideList.layoutManager = LinearLayoutManager(binding.root.context)
            binding.guideList.adapter = GuideListAdapter(guides)
        }
    }
}

