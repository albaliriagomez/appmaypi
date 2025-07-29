package com.torrezpillcokevin.nuna.ui.muro

import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
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

        // Asigna los datos al diseño
        val imgPersona = view.findViewById<ImageView>(R.id.imgPersonaDetalle)
        val txtNombre = view.findViewById<TextView>(R.id.txtNombreDetalle)
        val txtEdad = view.findViewById<TextView>(R.id.txtEdadDetalle)
        val txtGenero = view.findViewById<TextView>(R.id.txtGeneroDetalle)
        val txtFechaNac = view.findViewById<TextView>(R.id.txtFechaNacimientoDetalle)
        val txtTiempo = view.findViewById<TextView>(R.id.txtTiempoDesaparecido)
        val txtFechaDesaparicion = view.findViewById<TextView>(R.id.txtFechaDesaparicionDetalle)
        val txtLugar = view.findViewById<TextView>(R.id.txtLugarDesaparicionDetalle)
        val txtDescripcion = view.findViewById<TextView>(R.id.txtDescripcionDetalle)
        val txtEstado = view.findViewById<TextView>(R.id.txtEstadoInvestigacionDetalle)
        val txtCaracteristicas = view.findViewById<TextView>(R.id.txtCaracteristicasDetalle)

        val btnCerrar = view.findViewById<Button>(R.id.btnCerrar)


        cargarImagenDesdeUrl(reporte.foto_perfil, imgPersona)

        txtNombre.text = "${reporte.nombre} ${reporte.apellido}"
        txtEdad.text = "Edad: ${reporte.edad} años"
        txtGenero.text = "Género: ${reporte.genero}"
        txtFechaNac.text = "Fecha de nacimiento: ${reporte.fecha_nacimiento}"
        txtFechaDesaparicion.text = "Fecha de desaparición: ${reporte.fecha_desaparicion}"
        txtLugar.text = "Lugar de desaparición: ${reporte.lugar_desaparicion}"
        txtDescripcion.text = "Descripción: ${reporte.descripcion}"
        txtEstado.text = "Estado: ${reporte.estado_investigacion}"
        txtCaracteristicas.text = "Características: ${reporte.caracteristicas}"

        // Cálculo del tiempo desaparecido (opcional)
        val dias = calcularDiasDesaparecido(reporte.fecha_desaparicion)
        txtTiempo.text = "$dias días desaparecido"

        btnCerrar.setOnClickListener { dismiss() }
    }
    private fun cargarImagenDesdeUrl(url: String, imageView: ImageView) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val input = URL(url).openStream()
                val bitmap = BitmapFactory.decodeStream(input)
                withContext(Dispatchers.Main) {
                    imageView.setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    imageView.setImageResource(R.drawable.ninaperdida) // imagen fallback
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun calcularDiasDesaparecido(fechaDesaparicion: String): Long {
        return try {
            val formato = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val fecha = LocalDate.parse(fechaDesaparicion, formato)
            val hoy = LocalDate.now()
            ChronoUnit.DAYS.between(fecha, hoy)
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
