package com.torrezpillcokevin.nuna.ui.muro

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.torrezpillcokevin.nuna.R
import com.torrezpillcokevin.nuna.models.Persona

class DetallePersonaDialog : DialogFragment() {

    private lateinit var imgPersona: ImageView
    private lateinit var txtNombre: TextView
    private lateinit var txtTiempoDesaparecido: TextView
    private lateinit var txtEdad: TextView
    private lateinit var txtGenero: TextView
    private lateinit var txtFechaNacimiento: TextView
    private lateinit var txtFechaDesaparicion: TextView
    private lateinit var txtLugarDesaparicion: TextView
    private lateinit var txtDescripcion: TextView
    private lateinit var txtEstadoInvestigacion: TextView
    private lateinit var txtCaracteristicas: TextView
    private lateinit var btnReportar: Button
    private lateinit var btnCerrar: Button

    private var persona: Persona? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            persona = it.getParcelable(ARG_PERSONA)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.dialog_detalle_persona, container, false)

        // Configurar fondo transparente y sin título
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)

        inicializarVistas(view)
        mostrarDatosPersona()
        configurarBotones()

        return view
    }

    private fun inicializarVistas(view: View) {
        imgPersona = view.findViewById(R.id.imgPersonaDetalle)
        txtNombre = view.findViewById(R.id.txtNombreDetalle)
        txtTiempoDesaparecido = view.findViewById(R.id.txtTiempoDesaparecido)
        txtEdad = view.findViewById(R.id.txtEdadDetalle)
        txtGenero = view.findViewById(R.id.txtGeneroDetalle)
        txtFechaNacimiento = view.findViewById(R.id.txtFechaNacimientoDetalle)
        txtFechaDesaparicion = view.findViewById(R.id.txtFechaDesaparicionDetalle)
        txtLugarDesaparicion = view.findViewById(R.id.txtLugarDesaparicionDetalle)
        txtDescripcion = view.findViewById(R.id.txtDescripcionDetalle)
        txtEstadoInvestigacion = view.findViewById(R.id.txtEstadoInvestigacionDetalle)
        txtCaracteristicas = view.findViewById(R.id.txtCaracteristicasDetalle)
        btnReportar = view.findViewById(R.id.btnReportar)
        btnCerrar = view.findViewById(R.id.btnCerrar)
    }

    private fun mostrarDatosPersona() {
        persona?.let { p ->
            imgPersona.setImageResource(p.imagen)

            txtNombre.text = "${p.nombre} ${p.apellido}"
            txtTiempoDesaparecido.text = p.tiempoDesaparecido
            txtEdad.text = "Edad: ${p.edad} años"
            txtGenero.text = "Género: ${p.genero}"
            txtFechaNacimiento.text = "Fecha de nacimiento: ${p.fechaNacimiento}"
            txtFechaDesaparicion.text = "Fecha de desaparición: ${p.fechaDesaparicion}"
            txtLugarDesaparicion.text = "Lugar de desaparición: ${p.lugarDesaparicion}"
            txtEstadoInvestigacion.text = "Estado: ${p.estadoInvestigacion}"
            txtCaracteristicas.text = "Características: ${p.caracteristicas}"
            txtDescripcion.text = "Descripción: ${p.descripcion}"
        }
    }

    private fun configurarBotones() {
        btnReportar.setOnClickListener {
            // Primero cerrar el diálogo actual
            dismiss()

            // Navegar al fragmento de reportar con información para prellenar los campos
            val muroFragment = parentFragment as? MuroFragment
            muroFragment?.let {
                // El ReportarFragment espera: nombre, email, telefono, fecha_avistamiento, ubicacion_avistamiento, descripcion
                val bundle = Bundle().apply {
                    // Prefill los campos con la información de la persona
                    putString("nombre_prefill", persona?.nombre ?: "")
                    putString("email_prefill", persona?.emailContacto ?: "")
                    putString("telefono_prefill", persona?.telefonoContacto?.replace("-", "") ?: "")
                    putString("fecha_prefill", persona?.fechaDesaparicion ?: "")
                    putString("ubicacion_prefill", persona?.ubicacionDesaparicion ?: "")
                    putString("descripcion_prefill", "He visto a ${persona?.nombre} ${persona?.apellido}. ${persona?.caracteristicas}")
                }

                // Navegar al fragmento de reportar
                it.findNavController().navigate(
                    R.id.action_navigation_muro_to_reportarFragment,
                    bundle
                )
            }
        }

        btnCerrar.setOnClickListener {
            dismiss()
        }
    }

    companion object {
        private const val ARG_PERSONA = "arg_persona"

        fun newInstance(persona: Persona): DetallePersonaDialog {
            val fragment = DetallePersonaDialog()
            val args = Bundle()
            args.putParcelable(ARG_PERSONA, persona)
            fragment.arguments = args
            return fragment
        }
    }
    // En tu método onStart() del DialogFragment
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}