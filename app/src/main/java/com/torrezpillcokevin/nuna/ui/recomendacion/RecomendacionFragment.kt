package com.torrezpillcokevin.nuna.ui.recomendacion

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.torrezpillcokevin.nuna.R
import com.google.gson.Gson

class RecomendacionFragment : Fragment() {

    companion object {
        fun newInstance() = RecomendacionFragment()
    }

    private val viewModel: RecomendacionViewModel by viewModels()
    private val gson = Gson()  // Instancia de Gson para convertir a JSON

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Observa los cambios en la lista de usuarios
        viewModel.users.observe(this) { userList ->
            // Aquí actualizas tu UI con la lista de usuarios
            Log.d("RecomendacionFragment", "Usuarios recibidos: $userList")
            // Convierte la lista de usuarios a JSON
            val userListJson = gson.toJson(userList)

            // Muestra el JSON en un Toast
            Toast.makeText(requireContext(), "Usuarios recibidos: $userListJson", Toast.LENGTH_SHORT).show()
            // Si tienes un RecyclerView, aquí puedes pasar la lista a tu adaptador

            // adapter.submitList(userList)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_recomendacion, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Llama a fetchUsers cuando la vista se ha creado
        viewModel.fetchUsers(page = 0, itemsPerPage = 5)
    }
}