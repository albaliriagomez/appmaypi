package com.torrezpillcokevin.nuna.ui.guia

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.torrezpillcokevin.nuna.R
import com.torrezpillcokevin.nuna.data.RetrofitInstance
import androidx.recyclerview.widget.LinearLayoutManager
import com.torrezpillcokevin.nuna.data.Guide
import com.torrezpillcokevin.nuna.data.GuideCategory
import com.torrezpillcokevin.nuna.data.GuideCategoryResponse
import com.torrezpillcokevin.nuna.databinding.FragmentGuiaBinding


class GuiaFragment : Fragment() {

    private lateinit var viewModel: GuiaViewModel
    private lateinit var binding: FragmentGuiaBinding
    private lateinit var guideAdapter: GuideAdapter // Cambié el nombre a GuideAdapter porque ahora son guías, no categorías
    private val allGuides = mutableListOf<Guide>()
    private var currentPage = 0 // Empieza en 0 según tu API
    private var totalPages = 1
    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGuiaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        setupRecyclerView()
        setupScrollListener()
        loadInitialData()
    }

    private fun setupViewModel() {
        val apiService = RetrofitInstance.api
        val factory = GuidesViewModelFactory(requireActivity().application, apiService)
        viewModel = ViewModelProvider(this, factory)[GuiaViewModel::class.java]

        viewModel.guides.observe(viewLifecycleOwner) { result ->
            isLoading = false
            Log.d("GuiaFragment", "Nuevo resultado recibido en el Fragment")

            result.onSuccess { response ->
                Log.d("GuiaFragment", "Datos exitosos recibidos. Total páginas: ${response.total_paginas}")
                Log.d("GuiaFragment", "Datos recibidos: ${response.data.map { it.title }}")

                if (response.data.isNotEmpty()) {
                    totalPages = response.total_paginas

                    val beforeAdd = allGuides.map { it.title }
                    Log.d("GuiaFragment", "Antes de añadir: $beforeAdd")

                    allGuides.addAll(response.data)

                    val afterAdd = allGuides.map { it.title }
                    Log.d("GuiaFragment", "Después de añadir: $afterAdd")

                    guideAdapter.updateData(allGuides)
                    currentPage++

                    Log.d("GuiaFragment", "Datos finales en adapter: ${guideAdapter.currentList().map { it.title }}")
                } else {
                    Log.d("GuiaFragment", "No hay más datos para cargar")
                    Toast.makeText(context, "No hay más datos para cargar", Toast.LENGTH_SHORT).show()
                }
            }.onFailure {
                Log.e("GuiaFragment", "Error recibido: ${it.message}", it)
                Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            isLoading = loading
            ///binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }
    }

    private fun setupRecyclerView() {
        guideAdapter = GuideAdapter { guide ->
            // Manejar clic en guía (guide)
        }
        binding.recyclerViewGuides2.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = guideAdapter
        }
    }

    private fun setupScrollListener() {
        binding.recyclerViewGuides2.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                val totalItems = layoutManager.itemCount

                if (!isLoading && lastVisibleItem == totalItems - 1 && currentPage < totalPages) {
                    loadNextPage()
                }
            }
        })
    }

    private fun loadInitialData() {
        if (allGuides.isEmpty()) {
            loadNextPage()
        }
    }

    private fun loadNextPage() {
        if (isLoading || currentPage >= totalPages) return

        isLoading = true
        viewModel.getGuides(currentPage, 5) // Cambié a getGuides para coincidir con el ViewModel
    }
}
