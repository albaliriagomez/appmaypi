package com.torrezpillcokevin.nuna.ui.muro

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.torrezpillcokevin.nuna.R
import com.torrezpillcokevin.nuna.data.ReporteDesaparecido
import com.torrezpillcokevin.nuna.data.RetrofitInstance
import com.torrezpillcokevin.nuna.ui.muro.adapter.ReporteAdapter
import kotlinx.coroutines.launch

class MuroFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var txtResultados: TextView
    private lateinit var viewModel: MuroViewModel
    private lateinit var adapter: DesaparecidoAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_muro, container, false)

        recyclerView = view.findViewById(R.id.recyclerPersonas)
        searchView = view.findViewById(R.id.searchView)
        txtResultados = view.findViewById(R.id.txtResultados)

        recyclerView.layoutManager = GridLayoutManager(context, 2)
        adapter = DesaparecidoAdapter { persona ->
            val bottomSheet = DesaparecidoDetalleDialogFragment(persona)
            bottomSheet.show(parentFragmentManager, "detalle_desaparecido")
        }

        recyclerView.adapter = adapter

        // 🔽 AGREGAR ESTE BLOQUE
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                if (lastVisibleItemPosition >= totalItemCount - 2) {
                    viewModel.obtenerDesaparecidos()
                }
            }
        })

        // ViewModel y observadores
        val apiService = RetrofitInstance.api
        val factory = MuroViewModelFactory(requireActivity().application, apiService)
        viewModel = ViewModelProvider(this, factory)[MuroViewModel::class.java]

        viewModel.desaparecidos.observe(viewLifecycleOwner) { lista ->
            adapter.submitList(lista)
            txtResultados.text = "Resultados: ${lista.size}"
        }

        viewModel.status.observe(viewLifecycleOwner) {
            it.exceptionOrNull()?.let { error ->
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.recargarDesdePrimeraPagina()
        viewModel.yaSeCargaronInicialmente = true

        val fabAgregar = view.findViewById<FloatingActionButton>(R.id.fabAgregarPersona)
        fabAgregar.setOnClickListener {
            try {
                findNavController().navigate(R.id.reportarFragment)
            } catch (e: Exception) {
                Log.e("MuroFragment", "Navigation failed", e)
                Toast.makeText(context, "Error al navegar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
    private fun cargarReportesDesdeApi() {
        viewModel.obtenerDesaparecidos()
    }
    override fun onResume() {
        super.onResume()

        val recargar = findNavController()
            .currentBackStackEntry
            ?.savedStateHandle
            ?.get<Boolean>("recargar_muro") ?: false

        if (recargar) {
            viewModel.recargarDesdePrimeraPagina()
            findNavController()
                .currentBackStackEntry
                ?.savedStateHandle
                ?.remove<Boolean>("recargar_muro")
        }
    }


}
