package com.torrezpillcokevin.nuna.ui.muro.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.torrezpillcokevin.nuna.R
import com.torrezpillcokevin.nuna.models.Persona

class PersonaAdapter(
    private var listaPersonas: List<Persona>,
    private val onPersonaClick: (Persona) -> Unit
) : RecyclerView.Adapter<PersonaAdapter.PersonaViewHolder>() {

    private var listaFiltrada = listaPersonas.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_persona, parent, false)
        return PersonaViewHolder(view)
    }

    override fun onBindViewHolder(holder: PersonaViewHolder, position: Int) {
        val persona = listaFiltrada[position]
        holder.bind(persona)
        holder.itemView.setOnClickListener { onPersonaClick(persona) }
        holder.btnMasInfo.setOnClickListener { onPersonaClick(persona) }
    }

    override fun getItemCount(): Int = listaFiltrada.size

    fun filtrar(texto: String) {
        listaFiltrada = if (texto.isEmpty()) {
            listaPersonas.toMutableList()
        } else {
            listaPersonas.filter { it.nombre.contains(texto, ignoreCase = true) }.toMutableList()
        }
        notifyDataSetChanged()
    }

    class PersonaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val nombre: TextView = view.findViewById(R.id.txtNombre)
        private val edad: TextView = view.findViewById(R.id.txtEdad)
        private val imagen: ImageView = view.findViewById(R.id.imgPersona)
        private val tiempoDesaparecido: TextView = view.findViewById(R.id.txtTiempoDesaparecido)
        val btnMasInfo: Button = view.findViewById(R.id.btnMasInfo)

        fun bind(persona: Persona) {
            nombre.text = persona.nombre
            edad.text = "${persona.edad} años"
            tiempoDesaparecido.text = persona.tiempoDesaparecido
            imagen.setImageResource(persona.imagen)
        }
    }
}
