package com.torrezpillcokevin.nuna.ui.preguntasfrecuentes

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import com.torrezpillcokevin.nuna.R

class PreguntasFrecuentesFragment : Fragment() {

    // Lista de TextViews para manejar cada pregunta y su respuesta
    private lateinit var preguntas: List<TextView>
    private lateinit var respuestas: List<TextView>

    companion object {
        fun newInstance() = PreguntasFrecuentesFragment()
    }

    private val viewModel: PreguntasFrecuentesViewModel by viewModels()

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_preguntas_frecuentes, container, false)

        // Inicializamos las vistas
        preguntas = listOf(
            view.findViewById(R.id.tituloPregunta1),

            // Agrega más TextViews si es necesario
        )
        respuestas = listOf(
            view.findViewById(R.id.respuesta1),

            // Agrega más TextViews si es necesario
        )

        // Establecemos el OnClickListener para cada pregunta
        preguntas.forEachIndexed { index, textView ->
            textView.setOnClickListener {
                onPreguntaClick(index)
            }
        }

        return view
    }

    // Método que se llama cuando se hace clic en una pregunta
    fun onPreguntaClick(index: Int) {
        val respuestaTextView = respuestas[index]

        // Comprobamos si la respuesta está visible o no
        if (respuestaTextView.visibility == View.GONE) {
            // Si está oculta, la mostramos con una animación
            respuestaTextView.visibility = View.VISIBLE

            // Animación de deslizamiento hacia abajo
            val slideDown = ObjectAnimator.ofFloat(respuestaTextView, "translationY", -respuestaTextView.height.toFloat(), 0f)
            slideDown.duration = 300 // Duración de la animación
            slideDown.start()
        } else {
            // Si está visible, la ocultamos con una animación
            val slideUp = ObjectAnimator.ofFloat(respuestaTextView, "translationY", 0f, -respuestaTextView.height.toFloat())
            slideUp.duration = 190 // Duración de la animación

            // Agregamos un listener a la animación
            slideUp.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    // Ocultamos la vista después de la animación
                    respuestaTextView.visibility = View.GONE
                }
            })
            slideUp.start()
        }
    }
}
