package com.torrezpillcokevin.nuna

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class EvaluacionActivity : AppCompatActivity() {

    data class Question(val text: String, val help: String = "")

    private val questions = listOf(
        Question("¿La persona ha presentado cambios recientes en su comportamiento habitual?", "Ej.: ánimo, conducta, intereses"),
        Question("¿La persona se ha aislado de familiares o amigos?"),
        Question("¿Ha reducido la comunicación?"),
        Question("¿Contacto con desconocidos?"),
        Question("¿Cambio en uso del celular?", "Uso oculto o excesivo"),
        Question("¿Ha recibido cosas sin explicación?"),
        Question("¿Existe violencia en su entorno?"),
        Question("¿Está en situación vulnerable?"),
        Question("¿Tiene red de apoyo?"),
        Question("¿Ha dicho que quiere desaparecer?"),
        Question("¿Cambio de rutina sin avisar?"),
        Question("¿Eventos recientes de riesgo?")
    )

    private var index = 0
    private var score = 0

    private lateinit var tvPregunta: TextView
    private lateinit var tvAyuda: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutResultado: LinearLayout
    private lateinit var tvResultado: TextView
    private lateinit var tvScore: TextView
    private lateinit var tvAcciones: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evaluacion)

        tvPregunta = findViewById(R.id.tvPregunta)
        tvAyuda = findViewById(R.id.tvAyuda)
        progressBar = findViewById(R.id.progressBar)
        layoutResultado = findViewById(R.id.layoutResultado)
        tvResultado = findViewById(R.id.tvResultado)
        tvScore = findViewById(R.id.tvScore)
        tvAcciones = findViewById(R.id.tvAcciones)

        findViewById<Button>(R.id.btnNo).setOnClickListener { responder(0) }
        findViewById<Button>(R.id.btnAveces).setOnClickListener { responder(1) }
        findViewById<Button>(R.id.btnSi).setOnClickListener { responder(2) }

        findViewById<Button>(R.id.btnReiniciar).setOnClickListener {
            reiniciar()
        }

        mostrarPregunta()
    }

    private fun mostrarPregunta() {
        val q = questions[index]
        tvPregunta.text = "${index + 1}. ${q.text}"
        tvAyuda.text = q.help
        tvAyuda.visibility = if (q.help.isEmpty()) View.GONE else View.VISIBLE
        progressBar.progress = index
    }

    private fun responder(valor: Int) {
        score += valor
        index++

        if (index < questions.size) {
            mostrarPregunta()
        } else {
            mostrarResultado()
        }
    }

    private fun mostrarResultado() {
        findViewById<LinearLayout>(R.id.containerPregunta).visibility = View.GONE
        progressBar.visibility = View.GONE
        layoutResultado.visibility = View.VISIBLE

        tvScore.text = "Puntuación: $score / 24"

        when {
            score <= 8 -> {
                tvResultado.text = "Riesgo Bajo"
                tvResultado.setTextColor(ContextCompat.getColor(this, R.color.olive))
                tvAcciones.text = "• Mantener comunicación\n• Fortalecer vínculos\n• Observación preventiva"
            }
            score <= 16 -> {
                tvResultado.text = "Riesgo Moderado"
                tvResultado.setTextColor(ContextCompat.getColor(this, R.color.amber))
                tvAcciones.text = "• Hablar sin juzgar\n• Acompañar\n• Activar red de apoyo"
            }
            else -> {
                tvResultado.text = "Riesgo Alto"
                tvResultado.setTextColor(ContextCompat.getColor(this, R.color.charcoal))
                tvAcciones.text = "• ACTUAR DE INMEDIATO\n• Contactar autoridades\n• No dejar sola a la persona"
            }
        }
    }

    private fun reiniciar() {
        index = 0
        score = 0

        findViewById<LinearLayout>(R.id.containerPregunta).visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE
        layoutResultado.visibility = View.GONE

        mostrarPregunta()
    }
}