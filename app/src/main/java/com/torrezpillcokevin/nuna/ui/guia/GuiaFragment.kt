package com.torrezpillcokevin.nuna.ui.guia

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.torrezpillcokevin.nuna.R

class GuiaFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_guia, container, false)

        // Botón de "Más Información"
        view.findViewById<Button>(R.id.moreInfoButton)?.setOnClickListener {
            MoreInfoModal().show(parentFragmentManager, "MoreInfoModal")
        }

        // Función para abrir el modal con una guía específica
        fun openGuide(title: String, content: String) {
            val modal = GuiaModalFragment(title, content)
            modal.show(parentFragmentManager, "GuiaModal")
        }

        // Configurar los clics en las preguntas
        val questions = mapOf(
            R.id.whatIsThisApp to Pair("¿Cómo funciona esta app?", "Aquí te explicamos cómo usar la aplicación..."),
            R.id.emergencyActions to Pair("¿Qué hacer en caso de emergencia?", "Te damos instrucciones sobre cómo actuar en situaciones de riesgo."),
            R.id.setupContacts to Pair("¿Cómo configurar mis contactos de emergencia?", "Aprende a gestionar tus contactos de confianza."),
            R.id.activateSOS to Pair("Activar el modo SOS", "Descubre cómo activar y usar el modo de emergencia."),
            R.id.shareLocation to Pair("Compartir mi ubicación en tiempo real", "Aprende a compartir tu ubicación con contactos de confianza."),
            R.id.sendAlert to Pair("Enviar una alerta a mis contactos", "Cómo enviar una alerta de seguridad de manera rápida."),
            R.id.panicButton to Pair("Usar el botón de pánico", "Cómo funciona y cuándo utilizar el botón de pánico."),
            R.id.missingPerson to Pair("Cómo actuar si alguien desaparece", "Pasos a seguir en caso de desaparición de una persona."),
            R.id.streetSafety to Pair("Medidas de seguridad en la calle", "Consejos para mantenerte seguro en espacios públicos."),
            R.id.dangerActions to Pair("Qué hacer si me siento en peligro", "Estrategias para protegerte en situaciones de riesgo."),
            R.id.configureAlerts to Pair("Configurar alertas y notificaciones", "Personaliza las notificaciones de seguridad."),
            R.id.manageContacts to Pair("Administrar contactos de confianza", "Agrega o elimina contactos de confianza en la app."),
            R.id.contactSupport to Pair("Contactar soporte", "Opciones para obtener ayuda y resolver dudas.")
        )

        questions.forEach { (id, guide) ->
            view.findViewById<TextView>(id)?.setOnClickListener {
                openGuide(guide.first, guide.second)
            }
        }

        return view
    }

    // Modal dentro de GuiaFragment
    class MoreInfoModal : DialogFragment() {
        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            val view = inflater.inflate(R.layout.modal_more_info, container, false)

            // Cerrar el modal al hacer clic en la "X"
            view.findViewById<ImageView>(R.id.btnClose)?.setOnClickListener {
                dismiss()
            }

            return view
        }

        override fun onStart() {
            super.onStart()
            dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }
}
