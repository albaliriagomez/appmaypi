package com.torrezpillcokevin.nuna.ui.codigoRastreo

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.torrezpillcokevin.nuna.R

class CodigoRastreoFragment : Fragment() {

    companion object {
        fun newInstance() = CodigoRastreoFragment()
    }

    private val viewModel: CodigoRastreoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_codigo_rastreo, container, false)
    }
}