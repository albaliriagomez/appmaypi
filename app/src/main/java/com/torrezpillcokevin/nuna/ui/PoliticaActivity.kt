package com.torrezpillcokevin.nuna

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.torrezpillcokevin.nuna.databinding.ActivityPoliticaBinding

class PoliticaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPoliticaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPoliticaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        activarEventos()
    }

    private fun activarEventos() {

        // NORMA CENTRAL
        binding.btnCpe.setOnClickListener {
            actualizarInfo(
                "1.- Constitución Política del Estado",
                "Norma base",
                "Protección constitucional",
                "La Constitución Política del Estado boliviano reconoce y protege derechos fundamentales vinculados con la búsqueda de personas: vida, integridad, dignidad, libertad y seguridad. También garantiza la privacidad, honra, imagen y datos personales.\n\nAdemás establece igualdad, no discriminación y protección especial a grupos vulnerables como niños, mujeres y personas en riesgo."
            )
        }

        // LEY 263
        binding.btn263.setOnClickListener {
            actualizarInfo(
                "2.- Ley Nº 263",
                "Ley especial",
                "Prevención y protección",
                "Establece medidas de prevención, protección, atención y persecución penal frente a la trata y tráfico de personas, incluyendo principios como confidencialidad y no revictimización."
            )
        }

        // DECRETO 1486
        binding.btn1486.setOnClickListener {
            actualizarInfo(
                "3.- D.S. Nº 1486",
                "Reglamentación",
                "Aplicación operativa",
                "Regula la aplicación de la Ley 263 mediante coordinación institucional, sistemas de información y seguimiento de casos."
            )
        }

        // CODIGO PENAL
        binding.btn281.setOnClickListener {
            actualizarInfo(
                "4.- Art. 281 Bis",
                "Tipo penal",
                "Persecución penal",
                "Tipifica la trata de personas con actos como captación, traslado o retención con fines de explotación mediante engaño o violencia."
            )
        }

        // LEY 264
        binding.btn264.setOnClickListener {
            actualizarInfo(
                "5.- Ley Nº 264",
                "Seguridad ciudadana",
                "Sistema institucional",
                "Organiza el sistema nacional de seguridad ciudadana con coordinación entre instituciones y niveles del Estado."
            )
        }

        // LEY 3933
        binding.btn3933.setOnClickListener {
            actualizarInfo(
                "6.- Ley Nº 3933",
                "Búsqueda prioritaria",
                "Niñez extraviada",
                "Regula la búsqueda de niños y adolescentes extraviados bajo principios de rapidez, gratuidad y prioridad."
            )
        }

        // LEY 548
        binding.btn548.setOnClickListener {
            actualizarInfo(
                "7.- Ley Nº 548",
                "Protección integral",
                "Interés superior del niño",
                "Garantiza la protección integral de niños, niñas y adolescentes priorizando su interés superior."
            )
        }

        // LEY 348
        binding.btn348.setOnClickListener {
            actualizarInfo(
                "8.- Ley Nº 348",
                "Vida libre de violencia",
                "Protección de mujeres",
                "Establece medidas contra la violencia hacia las mujeres, incluyendo protección inmediata y reserva de información."
            )
        }

        // LEY 031
        binding.btn031.setOnClickListener {
            actualizarInfo(
                "9.- Ley Nº 031",
                "Articulación estatal",
                "Coordinación intergubernativa",
                "Define la coordinación entre niveles del Estado para una respuesta efectiva en seguridad y gestión territorial."
            )
        }
    }

    private fun actualizarInfo(
        titulo: String,
        badge: String,
        area: String,
        contenido: String
    ) {
        binding.tvTitulo.text = titulo
        binding.tvBadge.text = badge
        binding.tvArea.text = area
        binding.tvContenido.text = contenido
    }
}