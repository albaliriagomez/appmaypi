package com.torrezpillcokevin.nuna.ui.preguntasfrecuentes

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.torrezpillcokevin.nuna.data.RetrofitInstance
import com.torrezpillcokevin.nuna.databinding.FragmentPreguntasFrecuentesBinding

class PreguntasFrecuentesFragment : Fragment() {

    private lateinit var binding: FragmentPreguntasFrecuentesBinding
    private lateinit var viewModel: PreguntasFrecuentesViewModel
    private lateinit var adapter: FaqAdapter

    private var currentPage = 0
    private var totalPages = 1
    private var isLoading = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPreguntasFrecuentesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupViewModel()
        setupRecyclerView()
        setupScrollListener()
        loadInitialFaqs()
    }

    private fun setupViewModel() {
        val factory = PreguntasFrecuentesViewModelFactory(requireActivity().application, RetrofitInstance.api)
        viewModel = ViewModelProvider(this, factory)[PreguntasFrecuentesViewModel::class.java]

        viewModel.faqs.observe(viewLifecycleOwner) { result ->
            isLoading = false
            result.onSuccess { response ->
                totalPages = response.total_paginas

                if (response.data.isNotEmpty()) {
                    adapter.submitList(response.data.toList())  // Lista ya acumulada
                    currentPage++  // Incrementa sólo si recibiste datos
                } else {
                    // No hay más datos, evitar futuras cargas
                    currentPage = totalPages
                }
            }.onFailure {
                Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            isLoading = loading
            // Puedes mostrar u ocultar progress bar aquí si tienes
        }
    }

    private fun setupRecyclerView() {
        adapter = FaqAdapter()
        binding.recyclerViewFaqs.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewFaqs.adapter = adapter
    }

    private fun setupScrollListener() {
        binding.recyclerViewFaqs.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = rv.layoutManager as LinearLayoutManager
                val lastVisible = layoutManager.findLastVisibleItemPosition()
                val totalItems = layoutManager.itemCount

                if (!isLoading && lastVisible == totalItems - 1 && currentPage < totalPages) {
                    loadNextPage()
                }
            }
        })
    }

    private fun loadInitialFaqs() {
        if (adapter.itemCount == 0) {
            loadNextPage()
        }
    }

    private fun loadNextPage() {
        if (isLoading || currentPage >= totalPages) return
        isLoading = true
        viewModel.getFaqs(currentPage, 5)
    }
}