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
        activarAcordeon()
    }

    private fun activarAcordeon() {
        configurarToggle(binding.header1, binding.content1)
        configurarToggle(binding.header2, binding.content2)
    }

    private fun configurarToggle(header: View, content: View) {
        header.setOnClickListener {
            content.visibility =
                if (content.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}