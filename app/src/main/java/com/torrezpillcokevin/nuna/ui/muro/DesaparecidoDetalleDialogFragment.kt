package com.torrezpillcokevin.nuna.ui.muro

import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.torrezpillcokevin.nuna.R
import com.torrezpillcokevin.nuna.data.ReporteDesaparecido
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class DesaparecidoDetalleDialogFragment(
    private val reporte: ReporteDesaparecido
) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_detalle_persona, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imgPersona         = view.findViewById<ImageView>(R.id.imgPersonaDetalle)
        val txtNombre          = view.findViewById<TextView>(R.id.txtNombreDetalle)
        val txtEdad            = view.findViewById<TextView>(R.id.txtEdadDetalle)
        val txtGenero          = view.findViewById<TextView>(R.id.txtGeneroDetalle)
        val txtFechaNac        = view.findViewById<TextView>(R.id.txtFechaNacimientoDetalle)
        val txtTiempo          = view.findViewById<TextView>(R.id.txtTiempoDesaparecido)
        val txtFechaDesaparicion = view.findViewById<TextView>(R.id.txtFechaDesaparicionDetalle)
        val txtLugar           = view.findViewById<TextView>(R.id.txtLugarDesaparicionDetalle)
        val txtDescripcion     = view.findViewById<TextView>(R.id.txtDescripcionDetalle)
        val txtEstado          = view.findViewById<TextView>(R.id.txtEstadoInvestigacionDetalle)
        val txtCaracteristicas = view.findViewById<TextView>(R.id.txtCaracteristicasDetalle)
        val btnCerrar          = view.findViewById<Button>(R.id.btnCerrar)

        // ✅ La foto ahora es base64, no URL
        mostrarFotoBase64(reporte.foto_perfil, imgPersona)

        txtNombre.text          = "${reporte.nombre} ${reporte.apellido}"
        txtEdad.text            = "Edad: ${reporte.edad} años"
        txtGenero.text          = "Género: ${reporte.genero}"
        // ✅ fecha_nacimiento es String? — usamos ?: para evitar null
        txtFechaNac.text        = "Fecha de nacimiento: ${reporte.fecha_nacimiento ?: "No disponible"}"
        // ✅ fecha_desaparicion es String? — usamos ?: para evitar null
        txtFechaDesaparicion.text = "Fecha de desaparición: ${reporte.fecha_desaparicion ?: "No disponible"}"
        txtLugar.text           = "Lugar de desaparición: ${reporte.lugar_desaparicion}"
        txtDescripcion.text     = "Descripción: ${reporte.descripcion}"
        txtEstado.text          = "Estado: ${reporte.estado_investigacion ?: "Pendiente"}"
        txtCaracteristicas.text = "Características: ${reporte.caracteristicas}"

        // ✅ calcularDiasDesaparecido recibe String, pasamos ?: ""
        val dias = calcularDiasDesaparecido(reporte.fecha_desaparicion ?: "")
        txtTiempo.text = if (dias > 0) "$dias días desaparecido" else "Fecha desconocida"

        btnCerrar.setOnClickListener { dismiss() }
    }

    private fun mostrarFotoBase64(base64String: String, imageView: ImageView) {
        try {
            val base64Data = if (base64String.contains(",")) {
                base64String.substringAfter(",")
            } else {
                base64String
            }
            val bytes = Base64.decode(base64Data, Base64.DEFAULT)
            val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
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
    private fun calcularDiasDesaparecido(fechaDesaparicion: String): Long {
        if (fechaDesaparicion.isEmpty()) return 0
        return try {
            val soloFecha = fechaDesaparicion.split("T")[0].split(" ")[0]
            val fecha = LocalDate.parse(soloFecha, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            ChronoUnit.DAYS.between(fecha, LocalDate.now())
        } catch (e: Exception) {
            0
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}