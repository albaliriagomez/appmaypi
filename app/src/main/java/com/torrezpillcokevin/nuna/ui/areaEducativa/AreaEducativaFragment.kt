package com.torrezpillcokevin.nuna.ui.areaEducativa

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.torrezpillcokevin.nuna.R

class AreaEducativaFragment : Fragment() {

    companion object {
        fun newInstance() = AreaEducativaFragment()
    }

    private val viewModel: AreaEducativaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = inflater.inflate(R.layout.fragment_area_educativa, container, false)

        // Llenar cursos de Videos Educativos
        val videosLayout = view.findViewById<LinearLayout>(R.id.videosLayout)
        for (i in 1..10) { // Cambia 10 por el número de cursos que desees agregar
            val videoLayout = LayoutInflater.from(context).inflate(R.layout.item_video, videosLayout, false)
            val imageView = videoLayout.findViewById<ImageView>(R.id.imageView)
            val textView = videoLayout.findViewById<TextView>(R.id.textView)

            // Configura la imagen y la descripción
            imageView.setImageResource(R.drawable.baseline_personal_video_24) // Cambia por la imagen real
            textView.text = "Curso Educativo $i"

            videosLayout.addView(videoLayout)
        }

        // Llenar cursos de Infografía
        val infografiaLayout = view.findViewById<LinearLayout>(R.id.infografiaLayout)
        for (i in 1..10) { // Cambia 10 por el número de infografías que desees agregar
            val infografiaLayoutItem = LayoutInflater.from(context).inflate(R.layout.item_infografia, infografiaLayout, false)
            val imageView = infografiaLayoutItem.findViewById<ImageView>(R.id.imageView)
            val textView = infografiaLayoutItem.findViewById<TextView>(R.id.textView)

            // Configura la imagen y la descripción
            imageView.setImageResource(R.drawable.baseline_personal_video_24) // Cambia por la imagen real
            textView.text = "Infografía $i"

            infografiaLayout.addView(infografiaLayoutItem)
        }

        return view
    }
}
