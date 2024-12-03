package com.torrezpillcokevin.nuna.ui.soporte

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.torrezpillcokevin.nuna.R

class SoporteFragment : Fragment() {

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
    ): View? {

        val view = inflater.inflate(R.layout.fragment_soporte, container, false)


        nameEditText = view.findViewById(R.id.nameEditText)
        phoneEditText = view.findViewById(R.id.phoneEditText)
        emailEditText = view.findViewById(R.id.emailEditText)
        subjectEditText = view.findViewById(R.id.subjectEditText)
        messageEditText = view.findViewById(R.id.messageEditText)
        clearButton = view.findViewById(R.id.clearButton)
        sendButton = view.findViewById(R.id.sendButton)


        clearButton.setOnClickListener {
            limpiarCampos()
        }


        sendButton.setOnClickListener {
            enviarSolicitud()
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

    private fun enviarSolicitud() {
        val nombre = nameEditText.text.toString()
        val telefono = phoneEditText.text.toString()
        val correo = emailEditText.text.toString()
        val asunto = subjectEditText.text.toString()
        val mensaje = messageEditText.text.toString()

        if (nombre.isBlank() || telefono.isBlank() || correo.isBlank() || asunto.isBlank() || mensaje.isBlank()) {
            Toast.makeText(requireContext(), "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
        } else {

            Toast.makeText(requireContext(), "Solicitud enviada con éxito", Toast.LENGTH_SHORT).show()
            limpiarCampos()
        }
    }
}
