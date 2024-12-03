package com.torrezpillcokevin.nuna.ui.guia

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.torrezpillcokevin.nuna.R

class GuiaViewModel : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_guiaviewmodel)

        // Configuración del botón de retroceso
        val backToGettingStarted = findViewById<TextView>(R.id.backToGettingStarted)
        backToGettingStarted.setOnClickListener {
            finish()
        }

        // Mostrar el título y contenido de la guía seleccionada
        val title = intent.getStringExtra("TITLE") ?: "Guía"
        val titleTextView = findViewById<TextView>(R.id.whatIsThisAppTitle)
        titleTextView.text = title

        // Contenido dinámico según el título
        val contentTextView = findViewById<TextView>(R.id.introText)
        contentTextView.text = "Este es el contenido para la guía titulada: $title"
    }
}
