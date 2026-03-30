package com.torrezpillcokevin.nuna

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.torrezpillcokevin.nuna.databinding.FragmentGuiaBinding

class GuiaFragment : Fragment() {

    private var _binding: FragmentGuiaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGuiaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activarAcordeon()
    }

    private fun activarAcordeon() {
        // Item 1: Bienestar Emocional
        binding.header1.setOnClickListener { toggleContent(binding.content1) }

        // Item 2: Apoyo a la familia
        binding.header2.setOnClickListener { toggleContent(binding.content2) }

        // Item 3: Niños y Adolescentes
        binding.header3.setOnClickListener { toggleContent(binding.content3) }

        // Item 4: Apoyo Psicológico
        binding.header4.setOnClickListener { toggleContent(binding.content4) }
    }

    private fun toggleContent(content: View) {
        content.visibility = if (content.visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}