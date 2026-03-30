package com.torrezpillcokevin.nuna.ui.muro

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.torrezpillcokevin.nuna.R
import com.torrezpillcokevin.nuna.data.RetrofitInstance

class MuroFragment : Fragment() {

    private lateinit var viewModel: MuroViewModel
    private lateinit var adapter: DesaparecidoAdapter
    private lateinit var txtResultados: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_muro, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerPersonas)
        val txtResultados = view.findViewById<TextView>(R.id.txtResultados)
        val fabAgregar = view.findViewById<FloatingActionButton>(R.id.fabAgregarPersona)
        // Configuración de UI
        adapter = DesaparecidoAdapter { persona ->
            val dialog = DesaparecidoDetalleDialogFragment(persona)
            dialog.show(parentFragmentManager, "detalle_desaparecido")
        }

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = adapter

        // ViewModel Setup
        val apiService = RetrofitInstance.api
        val factory = MuroViewModelFactory(requireActivity().application, apiService)
        viewModel = ViewModelProvider(this, factory)[MuroViewModel::class.java]

        // Infinite Scroll
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                if (layoutManager.findLastVisibleItemPosition() >= layoutManager.itemCount - 2) {
                    viewModel.obtenerDesaparecidos()
                }
            }
        })

        // Observadores
        viewModel.desaparecidos.observe(viewLifecycleOwner) { lista ->
            adapter.submitList(lista)
            txtResultados.text = "Encontrados: ${lista.size}"
        }

        viewModel.status.observe(viewLifecycleOwner) { result ->
            result.exceptionOrNull()?.let {
                Log.e("MURO_ERROR", "Error detallado: ", it) // Esto te dirá en el Logcat qué es nulo exactamente
                val mensaje = it.message ?: "Error desconocido en la conexión"
                Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
            }
        }

        // Carga inicial (Solo si es la primera vez que se entra)
        if (!viewModel.yaSeCargaronInicialmente) {
            viewModel.recargarDesdePrimeraPagina()
            viewModel.yaSeCargaronInicialmente = true
        }

        fabAgregar.setOnClickListener {
            try {
                // Cambiamos R.id.reportarFragment por el ID del nuevo formulario final
                findNavController().navigate(R.id.nav_DesaparecidoFragment)
            } catch (e: Exception) {
                Log.e("MuroFragment", "Error al navegar al formulario final", e)
                Toast.makeText(context, "Error al abrir el formulario: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        // Verificar si regresamos de un registro exitoso para refrescar
        val refresh = findNavController().currentBackStackEntry
            ?.savedStateHandle?.get<Boolean>("recargar_muro") ?: false

        if (refresh) {
            viewModel.recargarDesdePrimeraPagina()
            findNavController().currentBackStackEntry?.savedStateHandle?.remove<Boolean>("recargar_muro")
        }
    }
}