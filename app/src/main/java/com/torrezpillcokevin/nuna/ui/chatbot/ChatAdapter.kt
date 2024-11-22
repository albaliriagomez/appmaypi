package com.torrezpillcokevin.nuna.ui.chatbot

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.torrezpillcokevin.nuna.R

// Clase del adaptador
class ChatAdapter(private val messages: List<String>) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    // ViewHolder que define la vista para cada elemento
    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
    }

    // Inflar la vista del elemento
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return ChatViewHolder(view)
    }

    // Bind de los datos con la vista
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]
        holder.messageTextView.text = message
    }

    // Número de elementos en la lista
    override fun getItemCount(): Int = messages.size
}