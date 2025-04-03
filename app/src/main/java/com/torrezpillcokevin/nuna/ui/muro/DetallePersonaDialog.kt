package com.torrezpillcokevin.nuna.ui.muro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.torrezpillcokevin.nuna.R
import com.torrezpillcokevin.nuna.models.Persona

class DetallePersonaDialog : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.dialog_detalle_persona, container, false)

        val imgPersona: ImageView = view.findViewById(R.id.imgPersonaDetalle)
        val txtNombre: TextView = view.findViewById(R.id.txtNombreDetalle)
        val txtEdad: TextView = view.findViewById(R.id.txtEdadDetalle)
        val txtGenero: TextView = view.findViewById(R.id.txtGeneroDetalle)
        val txtDescripcion: TextView = view.findViewById(R.id.txtDescripcionDetalle)
        val txtFechaNacimiento: TextView = view.findViewById(R.id.txtFechaNacimientoDetalle)
        val txtFechaDesaparicion: TextView = view.findViewById(R.id.txtFechaDesaparicionDetalle)
        val txtLugarDesaparicion: TextView = view.findViewById(R.id.txtLugarDesaparicionDetalle)
        val txtEstadoInvestigacion: TextView = view.findViewById(R.id.txtEstadoInvestigacionDetalle)
        val txtCaracteristicas: TextView = view.findViewById(R.id.txtCaracteristicasDetalle)
        val txtTiempoDesaparecido: TextView = view.findViewById(R.id.txtTiempoDesaparecido)
        val btnCerrar: Button = view.findViewById(R.id.btnCerrar)

        val persona = arguments?.getParcelable<Persona>("persona")

        persona?.let {
            imgPersona.setImageResource(it.imagen)
            txtNombre.text = "${it.nombre} ${it.apellido}"
            txtEdad.text = "Edad: ${it.edad} años"
            txtGenero.text = "Género: ${it.genero}"
            txtDescripcion.text = "Descripción: ${it.descripcion}"
            txtFechaNacimiento.text = "Fecha de nacimiento: ${it.fechaNacimiento}"
            txtFechaDesaparicion.text = "Fecha de desaparición: ${it.fechaDesaparicion}"
            txtLugarDesaparicion.text = "Lugar de desaparición: ${it.lugarDesaparicion}"
            txtEstadoInvestigacion.text = "Estado: ${it.estadoInvestigacion}"
            txtCaracteristicas.text = "Características: ${it.caracteristicas}"
            txtTiempoDesaparecido.text = "Tiempo desaparecido: ${it.tiempoDesaparecido}"
        }

        btnCerrar.setOnClickListener { dismiss() }

        return view
    }

    companion object {
        fun newInstance(persona: Persona): DetallePersonaDialog {
            return DetallePersonaDialog().apply {
                arguments = Bundle().apply {
                    putParcelable("persona", persona)
                }
            }
        }
    }
}
