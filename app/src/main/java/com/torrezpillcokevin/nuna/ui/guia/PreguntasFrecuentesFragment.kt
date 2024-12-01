package com.torrezpillcokevin.nuna.ui.guia

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.torrezpillcokevin.nuna.R

class PreguntasFrecuentesFragment : Fragment() {

    companion object {
        fun newInstance() = PreguntasFrecuentesFragment()
    }

    private val viewModel: PreguntasFrecuentesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_preguntas_frecuentes, container, false)
    }
}