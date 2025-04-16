package com.torrezpillcokevin.nuna.ui.muro.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.torrezpillcokevin.nuna.R
import com.torrezpillcokevin.nuna.models.Persona

class PersonaAdapter(
    private val listOriginal: List<Persona>,
    private val onItemClick: (Persona) -> Unit
) : RecyclerView.Adapter<PersonaAdapter.PersonaViewHolder>() {

    private var listaFiltrada: List<Persona> = listOriginal

    inner class PersonaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgPersona: ImageView = itemView.findViewById(R.id.imgPersona)
        val txtNombre: TextView = itemView.findViewById(R.id.txtNombre)
        val txtApellido: TextView = itemView.findViewById(R.id.txtApellido)
        val txtEdad: TextView = itemView.findViewById(R.id.txtEdad)
        val txtUbicacion: TextView = itemView.findViewById(R.id.txtUbicacion)
        val txtTiempoDesaparecido: TextView = itemView.findViewById(R.id.txtTiempoDesaparecido)

        fun bind(persona: Persona) {
            imgPersona.setImageResource(persona.imagen)
            txtNombre.text = persona.nombre
            txtApellido.text = persona.apellido
            txtEdad.text = "${persona.edad} años"
            txtUbicacion.text = persona.lugarDesaparicion
            txtTiempoDesaparecido.text = persona.tiempoDesaparecido

            // Ajustar el tamaño del ícono de ubicación
            val locationIcon = ResourcesCompat.getDrawable(
                itemView.context.resources,
                android.R.drawable.ic_menu_mylocation,
                null
            )
            locationIcon?.setBounds(0, 0, 36, 36)  // Tamaño más pequeño

            // Configurar los drawables
            val compoundDrawables = txtUbicacion.compoundDrawables
            txtUbicacion.setCompoundDrawables(
                locationIcon,  // Start (izquierda)
                compoundDrawables[1],  // Top
                compoundDrawables[2],  // End (derecha)
                compoundDrawables[3]   // Bottom
            )

            itemView.setOnClickListener {
                onItemClick(persona)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_persona, parent, false)
        return PersonaViewHolder(view)
    }

    override fun onBindViewHolder(holder: PersonaViewHolder, position: Int) {
        holder.bind(listaFiltrada[position])
    }

    override fun getItemCount(): Int = listaFiltrada.size

    fun filtrar(texto: String) {
        listaFiltrada = if (texto.isEmpty()) {
            listOriginal
        } else {
            listOriginal.filter { persona ->
                persona.nombre.contains(texto, ignoreCase = true) ||
                        persona.apellido.contains(texto, ignoreCase = true) ||
                        persona.lugarDesaparicion.contains(texto, ignoreCase = true)
            }
        }
        notifyDataSetChanged()
    }
}