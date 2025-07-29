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
import com.torrezpillcokevin.nuna.dbSqlite.DatabaseHelper
import com.torrezpillcokevin.nuna.models.Contact
import kotlinx.coroutines.launch
import java.io.File

class ContactoEmergenciaFragment : Fragment() {

    private lateinit var tableLayout: TableLayout
    private lateinit var dbHelper: DatabaseHelper

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
                    dbHelper.deleteContact(contacto.id)
                    cargarContactos()
                    Toast.makeText(requireContext(), "Eliminado ${contacto.name}", Toast.LENGTH_SHORT).show()
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

            view.findViewById<Button>(R.id.buttonCancel).setOnClickListener {
                dismiss()
            }

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

                val result = if (contactToEdit == null) {
                    dbHelper.addContact(newContact)
                } else {
                    dbHelper.updateContact(newContact).toLong()
                }

                if (result != -1L) {
                    Toast.makeText(requireContext(), "Contacto guardado", Toast.LENGTH_SHORT).show()
                    (targetFragment as? ContactoEmergenciaFragment)?.cargarContactos()
                    dismiss()
                } else {
                    Toast.makeText(requireContext(), "Error al guardar", Toast.LENGTH_SHORT).show()
                }
            }

            builder.setView(view)
            return builder.create()
        }

        override fun onStart() {
            super.onStart()
            dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }
}
