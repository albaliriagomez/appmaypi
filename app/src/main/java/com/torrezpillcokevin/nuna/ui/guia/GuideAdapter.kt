package com.torrezpillcokevin.nuna.ui.guia

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.torrezpillcokevin.nuna.data.Guide
import com.torrezpillcokevin.nuna.databinding.ItemGuideBinding

class GuideAdapter(
    private val onItemClick: (Guide) -> Unit
) : RecyclerView.Adapter<GuideAdapter.GuideViewHolder>() {

    private val guides = mutableListOf<Guide>()

    // Guarda los IDs de ítems expandidos
    private val expandedItems = mutableSetOf<Int>()

    fun updateData(newGuides: List<Guide>) {
        guides.clear()
        guides.addAll(newGuides)
        expandedItems.clear() // Opcional: resetear expandido al actualizar
        notifyDataSetChanged()
    }

    fun currentList(): List<Guide> = guides.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuideViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemGuideBinding.inflate(inflater, parent, false)
        return GuideViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GuideViewHolder, position: Int) {
        holder.bind(guides[position], expandedItems.contains(guides[position].id))
    }

    override fun getItemCount(): Int = guides.size

    inner class GuideViewHolder(private val binding: ItemGuideBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(guide: Guide, isExpanded: Boolean) {
            binding.textTitle.text = guide.title
            binding.textSubtitle.text = guide.subtitle

            // Mostrar contenido sólo si está expandido
            binding.textContent.visibility = if (isExpanded) View.VISIBLE else View.GONE
            binding.textContent.text = guide.content

            // Cambiar icono o estado visual si quieres
            binding.root.setOnClickListener {
                val currentlyExpanded = expandedItems.contains(guide.id)
                if (currentlyExpanded) {
                    expandedItems.remove(guide.id)
                } else {
                    expandedItems.add(guide.id)
                }
                notifyItemChanged(adapterPosition) // Actualiza sólo ese ítem

                // También puedes disparar callback si quieres
                onItemClick(guide)
            }
        }
    }
}
