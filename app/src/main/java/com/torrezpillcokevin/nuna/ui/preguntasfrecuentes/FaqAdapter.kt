package com.torrezpillcokevin.nuna.ui.preguntasfrecuentes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.torrezpillcokevin.nuna.data.Faq
import com.torrezpillcokevin.nuna.databinding.ItemFaqBinding

class FaqAdapter : ListAdapter<Faq, FaqAdapter.FaqViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Faq>() {
            override fun areItemsTheSame(oldItem: Faq, newItem: Faq) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Faq, newItem: Faq) = oldItem == newItem
        }
    }

    private var expandedPosition = RecyclerView.NO_POSITION

    inner class FaqViewHolder(private val binding: ItemFaqBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(faq: Faq, isExpanded: Boolean) {
            binding.textQuestion.text = faq.question
            binding.textAnswer.text = faq.answer

            // Controlar visibilidad de la respuesta
            binding.textAnswer.visibility = if (isExpanded) View.VISIBLE else View.GONE

            // Rotar la flechita (0 grados abajo, 180 arriba)
            binding.imgArrow.rotation = if (isExpanded) 180f else 0f

            binding.faqItemLayout.setOnClickListener {
                val previousExpanded = expandedPosition
                expandedPosition = if (isExpanded) RecyclerView.NO_POSITION else adapterPosition
                notifyItemChanged(previousExpanded)
                notifyItemChanged(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaqViewHolder {
        val binding = ItemFaqBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FaqViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FaqViewHolder, position: Int) {
        val faq = getItem(position)
        val isExpanded = position == expandedPosition
        holder.bind(faq, isExpanded)
    }
}
