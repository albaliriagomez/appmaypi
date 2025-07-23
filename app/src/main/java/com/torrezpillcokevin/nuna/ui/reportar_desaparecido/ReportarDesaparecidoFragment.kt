package com.torrezpillcokevin.nuna.ui.reportar_desaparecido

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.torrezpillcokevin.nuna.R

class ReportarDesaparecidoFragment : Fragment() {

    companion object {
        fun newInstance() = ReportarDesaparecidoFragment()
    }

    private val viewModel: ReportarDesaparecidoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_reportar_desaparecido, container, false)
    }
}