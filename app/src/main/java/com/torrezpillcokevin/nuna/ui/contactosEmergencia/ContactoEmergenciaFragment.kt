package com.torrezpillcokevin.nuna.ui.contactosEmergencia

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.torrezpillcokevin.nuna.R
import com.torrezpillcokevin.nuna.clases.BackgroundButtonService
import com.torrezpillcokevin.nuna.data.ContactoSupportRequest
import com.torrezpillcokevin.nuna.data.RetrofitInstance
import com.torrezpillcokevin.nuna.dbSqlite.DatabaseHelper
import com.torrezpillcokevin.nuna.models.Contact
import kotlinx.coroutines.launch
import java.io.File

class ContactoEmergenciaFragment : Fragment() {

    private lateinit var tableLayout: TableLayout
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var settingsButton: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_contacto_emergencia, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tableLayout = view.findViewById(R.id.contactTable)
        dbHelper = DatabaseHelper(requireContext())

        val addContactButton = view.findViewById<ImageButton>(R.id.buttonAddContact)
        addContactButton.setOnClickListener {
            val dialog = AgregarEditarContactoDialog()
            dialog.setTargetFragment(this, 0)
            dialog.show(parentFragmentManager, "AgregarContacto")
        }

        // Configurar el botón de settings
        settingsButton = view.findViewById(R.id.settingsButton)
        settingsButton.setOnClickListener {
            showEmergencyConfigDialog(requireContext())
        }

        cargarContactos()
    }

    fun cargarContactos() {
        tableLayout.removeAllViews()

        val contactos = dbHelper.getAllContacts()

        for (contacto in contactos) {
            val row = TableRow(requireContext())

            val nombre = TextView(requireContext()).apply {
                text = contacto.name
                setPadding(16, 8, 16, 8)
            }

            val telefono = TextView(requireContext()).apply {
                text = contacto.phone
                setPadding(16, 8, 16, 8)
            }

            val linea = TextView(requireContext()).apply {
                text = contacto.line
                setPadding(16, 8, 16, 8)
            }

            val editar = ImageView(requireContext()).apply {
                setImageResource(R.drawable.ic_edit)
                setPadding(16, 8, 16, 8)
                setColorFilter(ContextCompat.getColor(requireContext(), R.color.secondary_color))
                setOnClickListener {
                    val dialog = AgregarEditarContactoDialog.newInstance(contacto)
                    dialog.setTargetFragment(this@ContactoEmergenciaFragment, 0)
                    dialog.show(parentFragmentManager, "EditarContacto")
                }
            }

            val eliminar = ImageView(requireContext()).apply {
                setImageResource(R.drawable.ic_delete)
                setPadding(16, 8, 16, 8)
                setColorFilter(ContextCompat.getColor(requireContext(), R.color.primary_color))
                setOnClickListener {
                    // Mostrar el diálogo de confirmación antes de eliminar
                    showDeleteConfirmationDialog(contacto)
                }
            }

            row.addView(nombre)
            row.addView(telefono)
            row.addView(linea)
            row.addView(editar)
            row.addView(eliminar)

            tableLayout.addView(row)
        }
    }

    // Función para mostrar el diálogo de confirmación de eliminación
    private fun showDeleteConfirmationDialog(contact: Contact) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_delete_contact, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnDelete = dialogView.findViewById<Button>(R.id.btnDelete)
        val tvMessage = dialogView.findViewById<TextView>(R.id.tvDialogMessage)

        tvMessage.text = "¿Estás seguro de que deseas eliminar a ${contact.name}?"

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnDelete.setOnClickListener {
            val rowsDeleted = dbHelper.deleteContact(contact.id)
            if (rowsDeleted > 0) {
                cargarContactos() // Recargar la tabla
                Toast.makeText(requireContext(), "${contact.name} eliminado", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Error al eliminar", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun showEmergencyConfigDialog(context: Context) {
        val contacts = dbHelper.getAllContacts()

        if (contacts.isEmpty()) {
            Toast.makeText(context, "No hay contactos guardados", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = LayoutInflater.from(context).inflate(R.layout.bottomsheet_emergency, null)
        val bottomSheetDialog = BottomSheetDialog(context)
        bottomSheetDialog.setContentView(dialogView)

        val spinnerCallContact = dialogView.findViewById<Spinner>(R.id.spinnerCallContact)
        val listViewContacts = dialogView.findViewById<ListView>(R.id.lvContacts)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val switchBackgroundService = dialogView.findViewById<Switch>(R.id.switchBackgroundService)

        val contactNames = contacts.map { "${it.name} (${it.phone})" }

        val callAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, contactNames)
        spinnerCallContact.adapter = callAdapter

        val smsAdapter = ArrayAdapter(context, android.R.layout.simple_list_item_multiple_choice, contactNames)
        listViewContacts.adapter = smsAdapter
        listViewContacts.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        val sharedPreferences = context.getSharedPreferences("EmergencyPrefs", Context.MODE_PRIVATE)
        val savedCallContact = sharedPreferences.getString("emergency_call_phone", null)
        val savedSMSContacts = sharedPreferences.getStringSet("emergency_sms_phones", emptySet())
        val isServiceEnabled = sharedPreferences.getBoolean("background_service_enabled", false)

        switchBackgroundService.isChecked = isServiceEnabled

        savedCallContact?.let { phone ->
            contacts.indexOfFirst { it.phone == phone }
                .takeIf { it >= 0 }
                ?.let { spinnerCallContact.setSelection(it) }
        }

        savedSMSContacts?.forEach { savedPhone ->
            contacts.indexOfFirst { it.phone == savedPhone }
                .takeIf { it >= 0 }
                ?.let { listViewContacts.setItemChecked(it, true) }
        }

        switchBackgroundService.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showPermissionDialog(context, switchBackgroundService)
            } else {
                stopBackgroundService(context)
            }
        }

        btnSave.setOnClickListener {
            val selectedCallContact = contacts[spinnerCallContact.selectedItemPosition]
            val selectedSMSContacts = mutableSetOf<String>()

            for (i in 0 until listViewContacts.count) {
                if (listViewContacts.isItemChecked(i)) {
                    selectedSMSContacts.add(contacts[i].phone)
                }
            }

            with(sharedPreferences.edit()) {
                if (savedCallContact != selectedCallContact.phone) {
                    putString("emergency_call_phone", selectedCallContact.phone)
                }
                if (savedSMSContacts != selectedSMSContacts) {
                    putStringSet("emergency_sms_phones", selectedSMSContacts)
                }
                if (isServiceEnabled != switchBackgroundService.isChecked) {
                    putBoolean("background_service_enabled", switchBackgroundService.isChecked)
                }
                apply()
            }

            Toast.makeText(context, "Configuración guardada", Toast.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss()
        }

        btnCancel.setOnClickListener { bottomSheetDialog.dismiss() }
        bottomSheetDialog.show()
    }

    private fun startBackgroundService(context: Context) {
        val serviceIntent = Intent(context, BackgroundButtonService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    private fun stopBackgroundService(context: Context) {
        val serviceIntent = Intent(context, BackgroundButtonService::class.java)
        context.stopService(serviceIntent)
    }

    private fun showPermissionDialog(context: Context, @SuppressLint("UseSwitchCompatOrMaterialCode") switchService: Switch) {
        AlertDialog.Builder(context)
            .setTitle("Permiso necesario")
            .setMessage("¿Quieres permitir la ejecución en segundo plano?")
            .setPositiveButton("Sí") { _, _ ->
                switchService.isChecked = true
                startBackgroundService(context)
            }
            .setNegativeButton("No") { _, _ ->
                switchService.isChecked = false
            }
            .setCancelable(false)
            .show()
    }

    class AgregarEditarContactoDialog : DialogFragment() {

        companion object {
            private const val ARG_CONTACT = "contact"
            fun newInstance(contact: Contact): AgregarEditarContactoDialog {
                val fragment = AgregarEditarContactoDialog()
                val bundle = Bundle()
                bundle.putSerializable(ARG_CONTACT, contact)
                fragment.arguments = bundle
                return fragment
            }
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(requireContext())
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_agregar_contacto, null)

            val spinnerLinea = view.findViewById<Spinner>(R.id.spinnerLinea)
            val lineaOptions = listOf("Entel", "Tigo", "Viva")
            val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item3, lineaOptions)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerLinea.adapter = adapter

            val nameEditText = view.findViewById<EditText>(R.id.editTextName2)
            val phoneEditText = view.findViewById<EditText>(R.id.editTextPhone2)

            val contactToEdit = arguments?.getSerializable(ARG_CONTACT) as? Contact
            if (contactToEdit != null) {
                nameEditText.setText(contactToEdit.name)
                phoneEditText.setText(contactToEdit.phone)
                val selectedIndex = lineaOptions.indexOf(contactToEdit.line)
                spinnerLinea.setSelection(if (selectedIndex != -1) selectedIndex else 0)
            }

            view.findViewById<Button>(R.id.buttonCancel).setOnClickListener { dismiss() }

            view.findViewById<Button>(R.id.buttonSave).setOnClickListener {
                val name = nameEditText.text.toString()
                val phone = phoneEditText.text.toString()
                val linea = spinnerLinea.selectedItem.toString()

                if (name.isBlank() || phone.isBlank()) {
                    Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val dbHelper = DatabaseHelper(requireContext())
                val newContact = Contact(
                    id = contactToEdit?.id ?: 0,
                    name = name,
                    phone = phone,
                    line = linea
                )

                // 1. GUARDADO LOCAL (Para SMS y Offline)
                val result = if (contactToEdit == null) {
                    dbHelper.addContact(newContact)
                } else {
                    dbHelper.updateContact(newContact).toLong()
                }

                if (result != -1L) {
                    // 2. SINCRONIZACIÓN CON BACKEND (Para la Web)
                    enviarAlBackend(newContact)

                    Toast.makeText(requireContext(), "Guardado correctamente", Toast.LENGTH_SHORT).show()
                    (targetFragment as? ContactoEmergenciaFragment)?.cargarContactos()
                    dismiss()
                } else {
                    Toast.makeText(requireContext(), "Error al guardar en el teléfono", Toast.LENGTH_SHORT).show()
                }
            }

            builder.setView(view)
            return builder.create()
        }

        private fun enviarAlBackend(contacto: Contact) {
            // Obtenemos los datos de sesión (asegúrate de guardarlos en el Login)
            val sharedPref = requireContext().getSharedPreferences("nuna_prefs", Context.MODE_PRIVATE)
            val token = sharedPref.getString("auth_token", null)
            val userId = sharedPref.getInt("user_id", -1)
            val userEmail = sharedPref.getString("user_email", "user@nuna.com")

            if (token == null || userId == -1) {
                Log.e("SYNC", "No hay sesión activa para sincronizar")
                return
            }

            // Mapeamos los campos al modelo que el Backend de Soporte entiende
            val request = ContactoSupportRequest(
                name = contacto.name,
                email = userEmail ?: "emergencia@nuna.com",
                title = contacto.line,       // La línea telefónica aparece como "Título" en la Web
                message = contacto.phone,    // El número de teléfono aparece como "Mensaje" en la Web
                user_id = userId
            )

            lifecycleScope.launch {
                try {
                    val response = RetrofitInstance.api.postContactoEmergencia("Bearer $token", request)
                    if (response.isSuccessful) {
                        Log.d("SYNC", "Contacto sincronizado con la Web: ${response.body()?.message}")
                    } else {
                        Log.e("SYNC", "Error servidor: ${response.errorBody()?.string()}")
                    }
                } catch (e: Exception) {
                    Log.e("SYNC", "Error de red: ${e.message}. Se guardó solo local.")
                }
            }
        }

        override fun onStart() {
            super.onStart()
            dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }
}