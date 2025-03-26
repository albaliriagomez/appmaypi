package com.torrezpillcokevin.nuna.ui.guia

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.torrezpillcokevin.nuna.R

class GuiaModalFragment(private val title: String, private val content: String) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_guiaviewmodel, container, false)

        // Configurar título y contenido
        val modalTitle = view.findViewById<TextView>(R.id.modalTitle)
        val modalContent = view.findViewById<TextView>(R.id.modalContent)
        modalTitle.text = title
        modalContent.text = content

        // Botón para cerrar el modal
        val closeModal = view.findViewById<ImageView>(R.id.closeModal)
        closeModal.setOnClickListener { dismiss() }

        return view
    }
}
