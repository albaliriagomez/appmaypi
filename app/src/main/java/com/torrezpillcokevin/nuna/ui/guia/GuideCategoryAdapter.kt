package com.torrezpillcokevin.nuna.ui.guia

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.torrezpillcokevin.nuna.R
import com.torrezpillcokevin.nuna.data.GuideCategory

class GuideCategoryAdapter(
    private var categories: List<GuideCategory> = emptyList(),
    private val onItemClick: (GuideCategory) -> Unit
) : RecyclerView.Adapter<GuideCategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.categoryTitle)

        fun bind(category: GuideCategory) {
            titleTextView.text = category.title
            itemView.setOnClickListener { onItemClick(category) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_guide_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int = categories.size

    fun updateData(newCategories: List<GuideCategory>) {
        Log.d("GuideAdapter", "Actualizando datos. Recibidos: ${newCategories.map { it.title }}")

        categories = newCategories.sortedBy { category ->
            val num = category.title.replace("TEMA", "").trim().toIntOrNull() ?: 0
            Log.d("GuideAdapter", "Ordenando: ${category.title} -> $num")
            num
        }

        Log.d("GuideAdapter", "Datos ordenados: ${categories.map { it.title }}")
        notifyDataSetChanged()
    }

    fun currentList(): List<GuideCategory> = categories

}