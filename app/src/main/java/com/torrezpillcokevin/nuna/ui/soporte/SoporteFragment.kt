package com.torrezpillcokevin.nuna.ui.soporte

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.torrezpillcokevin.nuna.R
import com.torrezpillcokevin.nuna.data.RetrofitInstance
import com.torrezpillcokevin.nuna.data.SupportRequest
import java.time.LocalDateTime

class SoporteFragment : Fragment() {

    private lateinit var viewModel: SoporteViewModel

    private lateinit var nameEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var subjectEditText: EditText
    private lateinit var messageEditText: EditText
    private lateinit var clearButton: Button
    private lateinit var sendButton: Button

    @RequiresApi(Build.VERSION_CODES.O)
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

        // Observa el resultado del envío de la solicitud
        viewModel.status.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                limpiarCampos()
            }.onFailure {
                Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }

        // Observa los datos del usuario para precargar los campos
        viewModel.user.observe(viewLifecycleOwner) { user ->
            nameEditText.setText(user.nombres)
            emailEditText.setText(user.email)
        }

        // Llama a fetchUserInfo para obtener datos del usuario
        viewModel.fetchUserInfo()

        clearButton.setOnClickListener { limpiarCampos() }

        sendButton.setOnClickListener {
            val nombre = nameEditText.text.toString()
            val correo = emailEditText.text.toString()
            val asunto = subjectEditText.text.toString()
            val mensaje = messageEditText.text.toString()

            if (nombre.isBlank() || correo.isBlank() || asunto.isBlank() || mensaje.isBlank()) {
                Toast.makeText(requireContext(), "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            } else {
                // Asegúrate de tener los datos del usuario disponibles
                val currentUser = viewModel.user.value
                if (currentUser != null) {
                    val supportRequest = SupportRequest(
                        user_id = currentUser.id,
                        name = nombre,
                        email = correo,
                        subject = asunto,
                        message = mensaje,
                        sent_at = LocalDateTime.now().toString()
                    )
                    viewModel.sendSupportRequest(supportRequest)
                } else {
                    Toast.makeText(requireContext(), "Cargando datos de usuario...", Toast.LENGTH_SHORT).show()
                }
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
