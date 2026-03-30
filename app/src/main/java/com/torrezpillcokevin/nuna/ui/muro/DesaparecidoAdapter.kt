package com.torrezpillcokevin.nuna.ui.muro

import android.graphics.BitmapFactory
import android.os.Build
import android.util.Base64
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
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class DesaparecidoAdapter(
    private val onItemClick: (ReporteDesaparecido) -> Unit
) : ListAdapter<ReporteDesaparecido, DesaparecidoAdapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<ReporteDesaparecido>() {
        override fun areItemsTheSame(oldItem: ReporteDesaparecido, newItem: ReporteDesaparecido) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ReporteDesaparecido, newItem: ReporteDesaparecido) =
            oldItem == newItem
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgPersona: ImageView = view.findViewById(R.id.imgFoto)
        val txtNombre: TextView = view.findViewById(R.id.txtNombre)
        val txtApellido: TextView = view.findViewById(R.id.txtApellido)
        val txtEdad: TextView = view.findViewById(R.id.txtEdad)
        val txtUbicacion: TextView = view.findViewById(R.id.txtUbicacion)
        val txtTiempoDesaparecido: TextView = view.findViewById(R.id.txtTiempoDesaparecido)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_desaparecido, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val persona = getItem(position)

        holder.txtNombre.text = persona.nombre
        holder.txtApellido.text = persona.apellido
        holder.txtEdad.text = "${persona.edad} años"
        holder.txtUbicacion.text = persona.lugar_desaparicion

        // ✅ fecha_desaparicion es String? (nullable) → usamos ?: ""
        holder.txtTiempoDesaparecido.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            calcularDiasDesaparecido(persona.fecha_desaparicion ?: "")
        } else {
            if (!persona.fecha_desaparicion.isNullOrEmpty()) "Desaparecido: ${persona.fecha_desaparicion}"
            else "Fecha desconocida"
        }

        if (persona.foto_perfil.isNotEmpty()) {
            mostrarFotoBase64(persona.foto_perfil, holder.imgPersona)
        } else {
            holder.imgPersona.setImageResource(R.drawable.infantes)
        }

        holder.itemView.setOnClickListener { onItemClick(persona) }
    }

    private fun mostrarFotoBase64(base64String: String, imageView: ImageView) {
        try {
            val base64Data = if (base64String.contains(",")) {
                base64String.substringAfter(",")
            } else {
                base64String
            }
            val bytes = Base64.decode(base64Data, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap)
            } else {
                imageView.setImageResource(R.drawable.ninaperdida)
            }
        } catch (e: Exception) {
            imageView.setImageResource(R.drawable.ninaperdida)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun calcularDiasDesaparecido(fechaStr: String): String {
        if (fechaStr.isEmpty()) return "Fecha desconocida"
        return try {
            val soloFecha = fechaStr.split("T")[0].split(" ")[0]
            val fecha = LocalDate.parse(soloFecha)
            val dias = ChronoUnit.DAYS.between(fecha, LocalDate.now())
            if (dias <= 0) "Desaparecido hoy" else "$dias días desaparecido"
        } catch (e: Exception) {
            "Fecha: $fechaStr"
        }
    }
}