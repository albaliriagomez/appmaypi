package com.torrezpillcokevin.nuna.ui.contactosEmergencia

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.torrezpillcokevin.nuna.R
import com.torrezpillcokevin.nuna.data.ContactoRequest
import com.torrezpillcokevin.nuna.data.RetrofitInstance
import kotlinx.coroutines.launch
import java.io.File

class ContactoEmergenciaFragment : Fragment() {

    private lateinit var tableLayout: TableLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_contacto_emergencia, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tableLayout = view.findViewById(R.id.contactTable)

        val addContactButton = view.findViewById<ImageButton>(R.id.buttonAddContact)
        addContactButton.setOnClickListener {
            val dialog = AgregarContactoDialog()
            dialog.show(childFragmentManager, "AgregarContacto")
        }

        cargarContactos()  // Cargar los contactos cuando el fragmento se crea
    }

    private fun cargarContactos() {
        lifecycleScope.launch {
            try {
                val token = getToken()
                if (token != null) {
                    val response = RetrofitInstance.api.getContactos("Bearer $token", 0, 5)
                    if (response.isSuccessful) {
                        // Acceder a la propiedad `data` que contiene los contactos
                        val contactos = response.body()?.data ?: emptyList()
                        tableLayout.removeAllViews()  // Limpiar la tabla antes de agregar nuevos contactos
                        for (contacto in contactos) {
                            val row = TableRow(requireContext())

                            val nombre = TextView(requireContext()).apply {
                                text = contacto.nombre
                                setPadding(16, 8, 16, 8)
                            }

                            val telefono = TextView(requireContext()).apply {
                                text = contacto.telefono.toString()
                                setPadding(16, 8, 16, 8)
                            }

                            val linea = TextView(requireContext()).apply {
                                text = when (contacto.linea_telefonica) {
                                    1 -> "Entel"
                                    2 -> "Tigo"
                                    3 -> "Viva"
                                    else -> "Desconocido"
                                }
                                setPadding(16, 8, 16, 8)
                            }

                            val acciones = ImageView(requireContext()).apply {
                                setImageResource(R.drawable.ic_delete)
                                setPadding(16, 8, 16, 8)
                                setColorFilter(ContextCompat.getColor(requireContext(), R.color.primary_color))
                                setOnClickListener {
                                    Toast.makeText(requireContext(), "Eliminar ${contacto.nombre}", Toast.LENGTH_SHORT).show()
                                }
                            }

                            row.addView(nombre)
                            row.addView(telefono)
                            row.addView(linea)
                            row.addView(acciones)

                            tableLayout.addView(row)
                        }
                    } else {
                        Log.e("API", "Error: ${response.code()} ${response.errorBody()?.string()}")
                        Toast.makeText(requireContext(), "Error al cargar contactos", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("API", "Excepción: ${e.message}", e)
                Toast.makeText(requireContext(), "Error de red", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getToken(): String? {
        return try {
            val sharedPreferences = EncryptedSharedPreferences.create(
                requireContext(),
                "SECURE_APP_PREFS",
                MasterKey.Builder(requireContext()).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            val token = sharedPreferences.getString("JWT_TOKEN", null)
            Log.d("TOKEN_DEBUG", "Token recuperado: ${token?.take(5)}...")
            token
        } catch (e: Exception) {
            Log.e("SECURE_STORAGE", "Error al recuperar el token", e)
            null
        }
    }



    class AgregarContactoDialog : DialogFragment() {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(requireContext())
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_agregar_contacto, null)

            val spinnerLinea = view.findViewById<Spinner>(R.id.spinnerLinea)
            val lineaOptions = listOf("Entel", "Tigo", "Viva")

            val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item3, lineaOptions)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerLinea.adapter = adapter

            val cancelButton = view.findViewById<Button>(R.id.buttonCancel)
            val saveButton = view.findViewById<Button>(R.id.buttonSave)

            cancelButton.setOnClickListener {
                dismiss()
            }

            saveButton.setOnClickListener {
                val name = view.findViewById<EditText>(R.id.editTextName2).text.toString()
                val phone = view.findViewById<EditText>(R.id.editTextPhone2).text.toString()
                val linea = spinnerLinea.selectedItem.toString()

                if (name.isBlank() || phone.isBlank()) {
                    Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val lineaId = when (linea) {
                    "Entel" -> 1
                    "Tigo" -> 2
                    "Viva" -> 3
                    else -> 0
                }

                val contacto = ContactoRequest(
                    nombre = name,
                    telefono = phone.toLong(),
                    linea_telefonica = lineaId,
                    accion = "crear"
                )

                Log.d("CONTACTO_DEBUG", "Enviando contacto: $contacto")

                lifecycleScope.launch {
                    try {
                        val token = getToken()
                        if (token == null) {
                            Toast.makeText(requireContext(), "Error: Sesión no válida", Toast.LENGTH_SHORT).show()
                            Log.e("TOKEN_ERROR", "Token es nulo, no se puede continuar")
                            return@launch
                        }

                        Log.d("API_DEBUG", "Enviando petición con token: ${token.take(10)}...")

                        val response = RetrofitInstance.api.createContacto("Bearer $token", contacto)

                        Log.d("API_DEBUG", "Código de respuesta: ${response.code()}")
                        Log.d("API_DEBUG", "Cuerpo exitoso: ${response.body()}")
                        Log.d("API_DEBUG", "ErrorBody: ${response.errorBody()?.string()}")

                        if (response.isSuccessful) {
                            Toast.makeText(requireContext(), "Contacto guardado exitosamente", Toast.LENGTH_SHORT).show()
                            //cargarContactos()
                            dismiss()
                        } else {
                            Toast.makeText(requireContext(), "Error al guardar (${response.code()})", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e("API_ERROR", "Excepción al guardar contacto", e)
                        Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }

            builder.setView(view)
            return builder.create()
        }

        private fun getToken(): String? {
            return try {
                val sharedPreferences = EncryptedSharedPreferences.create(
                    requireContext(),
                    "SECURE_APP_PREFS",
                    MasterKey.Builder(requireContext()).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
                val token = sharedPreferences.getString("JWT_TOKEN", null)
                Log.d("TOKEN_DEBUG", "Token recuperado: ${token?.take(5)}...")  // Muestra solo los primeros 5 caracteres por seguridad
                token
            } catch (e: Exception) {
                Log.e("SECURE_STORAGE", "Error al recuperar el token", e)
                null
            }
        }

        override fun onStart() {
            super.onStart()
            dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }
}
