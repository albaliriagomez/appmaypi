package com.torrezpillcokevin.nuna.ui.soporte

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.torrezpillcokevin.nuna.R
import com.torrezpillcokevin.nuna.data.RetrofitInstance

class SoporteFragment : Fragment() {

    private lateinit var viewModel: SoporteViewModel

    private lateinit var nameEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var subjectEditText: EditText
    private lateinit var messageEditText: EditText
    private lateinit var clearButton: Button
    private lateinit var sendButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_soporte, container, false)

        // Inicializa ViewModel
        val apiService = RetrofitInstance.api
        val factory = SoporteViewModelFactory(requireActivity().application, apiService)
        viewModel = ViewModelProvider(this, factory)[SoporteViewModel::class.java]

        // Enlazar views
        nameEditText = view.findViewById(R.id.nameEditText)
        phoneEditText = view.findViewById(R.id.phoneEditText)
        emailEditText = view.findViewById(R.id.emailEditText)
        subjectEditText = view.findViewById(R.id.subjectEditText)
        messageEditText = view.findViewById(R.id.messageEditText)
        clearButton = view.findViewById(R.id.clearButton)
        sendButton = view.findViewById(R.id.sendButton)

        // Observa el resultado del envío
        viewModel.status.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                limpiarCampos()
            }.onFailure {
                Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }

        clearButton.setOnClickListener { limpiarCampos() }

        sendButton.setOnClickListener {
            val nombre = nameEditText.text.toString()
            val correo = emailEditText.text.toString()
            val asunto = subjectEditText.text.toString()
            val mensaje = messageEditText.text.toString()
            val telefono = phoneEditText.text.toString().takeIf { it.isNotBlank() }

            if (nombre.isBlank() || correo.isBlank() || asunto.isBlank() || mensaje.isBlank()) {
                Toast.makeText(
                    requireContext(),
                    "Por favor, completa los campos obligatorios",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // Llama al ViewModel con parámetros individuales (no SupportRequest)
                viewModel.sendSupportRequest(
                    nombre   = nombre,
                    email    = correo,
                    titulo   = asunto,
                    mensaje  = mensaje,
                    telefono = telefono
                )
            }
        }

        return view
    }

    private fun limpiarCampos() {
        nameEditText.text.clear()
        phoneEditText.text.clear()
        emailEditText.text.clear()
        subjectEditText.text.clear()
        messageEditText.text.clear()
        Toast.makeText(requireContext(), "Campos limpiados", Toast.LENGTH_SHORT).show()
    }
}