package com.torrezpillcokevin.nuna.ui.muro.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.torrezpillcokevin.nuna.R
import com.torrezpillcokevin.nuna.data.ReporteDesaparecido
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class ReporteAdapter(
    private val listaOriginal: List<ReporteDesaparecido>,
    private val onItemClick: (ReporteDesaparecido) -> Unit
) : RecyclerView.Adapter<ReporteAdapter.ReporteViewHolder>() {

    private var listaFiltrada: List<ReporteDesaparecido> = listaOriginal

    inner class ReporteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgPersona: ImageView = itemView.findViewById(R.id.imgFoto)
        private val txtNombre: TextView = itemView.findViewById(R.id.txtNombre)
        private val txtApellido: TextView = itemView.findViewById(R.id.txtApellido)
        private val txtEdad: TextView = itemView.findViewById(R.id.txtEdad)
        private val txtUbicacion: TextView = itemView.findViewById(R.id.txtUbicacion)
        private val txtTiempoDesaparecido: TextView = itemView.findViewById(R.id.txtTiempoDesaparecido)

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(reporte: ReporteDesaparecido) {
            txtNombre.text = reporte.nombre
            txtApellido.text = reporte.apellido
            txtEdad.text = "${reporte.edad} años"
            txtUbicacion.text = reporte.lugar_desaparicion

            // Puedes calcular el tiempo desaparecido si quieres. Aquí va un placeholder:
            txtTiempoDesaparecido.text = calcularTiempoDesaparecido(reporte.fecha_desaparicion)

            // Cargar imagen desde URL si usas Glide
            /*
            Glide.with(itemView.context)
                .load(reporte.foto_perfil)
                .placeholder(R.drawable.ic_placeholder)
                .into(imgPersona)
            */

            // Si no usas Glide, muestra imagen por defecto o nada
            imgPersona.setImageResource(R.drawable.infantes)

            // Ícono de ubicación
            val locationIcon = ResourcesCompat.getDrawable(
                itemView.context.resources,
                android.R.drawable.ic_menu_mylocation,
                null
            )
            locationIcon?.setBounds(0, 0, 36, 36)

            val compoundDrawables = txtUbicacion.compoundDrawables
            txtUbicacion.setCompoundDrawables(
                locationIcon,
                compoundDrawables[1],
                compoundDrawables[2],
                compoundDrawables[3]
            )

            itemView.setOnClickListener {
                onItemClick(reporte)
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun calcularTiempoDesaparecido(fecha: String): String {
            return try {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd") // o el formato que uses
                val fechaDesaparicion = LocalDate.parse(fecha, formatter)
                val dias = ChronoUnit.DAYS.between(fechaDesaparicion, LocalDate.now())
                "Desaparecido hace $dias día(s)"
            } catch (e: Exception) {
                "Tiempo no disponible"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReporteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_persona, parent, false)
        return ReporteViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ReporteViewHolder, position: Int) {
        holder.bind(listaFiltrada[position])
    }

    override fun getItemCount(): Int = listaFiltrada.size

    fun actualizarLista(nuevaLista: List<ReporteDesaparecido>) {
        listaFiltrada = nuevaLista
        notifyDataSetChanged()
    }
}
