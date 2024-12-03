package com.torrezpillcokevin.nuna.ui.guia

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.torrezpillcokevin.nuna.R

class GuiaFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_guia, container, false)

        // Configuración del botón "Volver al Centro de Ayuda"
        val backToHelpCenter = view.findViewById<TextView>(R.id.backToHelpCenter)
        backToHelpCenter.setOnClickListener {
            requireActivity().onBackPressed()
        }

        // Configuración de clics en elementos de "Getting Started"
        val whatIsThisApp = view.findViewById<TextView>(R.id.whatIsThisApp)
        whatIsThisApp.setOnClickListener {
            navigateToDetails("What is this app?")
        }

        val startUsingApp = view.findViewById<TextView>(R.id.startUsingApp)
        startUsingApp.setOnClickListener {
            navigateToDetails("Start using the app")
        }

        return view
    }

    private fun navigateToDetails(title: String) {
        val intent = Intent(requireContext(), GuiaViewModel::class.java)
        intent.putExtra("TITLE", title)
        startActivity(intent)
    }
}
