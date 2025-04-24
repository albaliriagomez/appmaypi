package com.torrezpillcokevin.nuna.ui.guia

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Build
import android.os.Bundle
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


class GuiaFragment : Fragment() {

    private lateinit var viewModel: GuiaViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var guidesAdapter: GuidesAdapter

    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_guia, container, false)

        // Inicializa el ViewModel
        val apiService = RetrofitInstance.api
        val factory = GuidesViewModelFactory(requireActivity().application, apiService)
        viewModel = ViewModelProvider(this, factory)[GuiaViewModel::class.java]

        // Inicializa el RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewGuides2)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.guides.observe(viewLifecycleOwner) { result ->
            result.onSuccess { guidesByCategory ->
                if (!::guidesAdapter.isInitialized) {
                    guidesAdapter = GuidesAdapter(guidesByCategory)
                    recyclerView.adapter = guidesAdapter
                } else {
                    guidesAdapter.notifyDataSetChanged()
                }
            }.onFailure {
                Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }



        // Llama al ViewModel para obtener las guías
        viewModel.getGuides(pagina = 0, porPagina = 5)

        return view
    }
}
