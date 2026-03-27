package com.torrezpillcokevin.nuna.ui.muro

import android.graphics.BitmapFactory
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.torrezpillcokevin.nuna.R
import com.torrezpillcokevin.nuna.data.ReporteDesaparecido
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// Archivo: com.torrezpillcokevin.nuna.ui.muro.DesaparecidoAdapter.kt

class DesaparecidoAdapter(
    private val onItemClick: (ReporteDesaparecido) -> Unit
) : ListAdapter<ReporteDesaparecido, DesaparecidoAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<ReporteDesaparecido>() {
        override fun areItemsTheSame(oldItem: ReporteDesaparecido, newItem: ReporteDesaparecido) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ReporteDesaparecido, newItem: ReporteDesaparecido) =
            oldItem == newItem
    }
) {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgPersona: ImageView = view.findViewById(R.id.imgFoto)
        val txtNombre: TextView = view.findViewById(R.id.txtNombre)
        val txtApellido: TextView = view.findViewById(R.id.txtApellido)
        val txtEdad: TextView = view.findViewById(R.id.txtEdad)
        val txtUbicacion: TextView = view.findViewById(R.id.txtUbicacion)
        val txtTiempoDesaparecido: TextView = view.findViewById(R.id.txtTiempoDesaparecido)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_desaparecido, parent, false)
        return ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val persona = getItem(position)

        holder.txtNombre.text = persona.nombre
        holder.txtApellido.text = persona.apellido
        holder.txtEdad.text = "${persona.edad} años"
        holder.txtUbicacion.text = persona.lugar_desaparicion
        holder.txtTiempoDesaparecido.text = calcularDiasDesaparecido(persona.fecha_desaparicion)

        // Reset de imagen para evitar parpadeos en el scroll
        holder.imgPersona.setImageResource(R.drawable.infantes)
        if (persona.foto_perfil.isNotEmpty()) {
            loadImageFromUrl(persona.foto_perfil, holder.imgPersona)
        }

        holder.itemView.setOnClickListener { onItemClick(persona) }
    }

    private fun loadImageFromUrl(url: String, imageView: ImageView) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bitmap = BitmapFactory.decodeStream(URL(url).openStream())
                withContext(Dispatchers.Main) { imageView.setImageBitmap(bitmap) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { imageView.setImageResource(R.drawable.ninaperdida) }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun calcularDiasDesaparecido(fechaStr: String): String {
        return try {
            val fecha = LocalDate.parse(fechaStr, DateTimeFormatter.ISO_LOCAL_DATE)
            val dias = ChronoUnit.DAYS.between(fecha, LocalDate.now())
            if (dias <= 0) "Desaparecido hoy" else "$dias días desaparecido"
        } catch (e: Exception) { "Fecha desconocida" }
    }
}
