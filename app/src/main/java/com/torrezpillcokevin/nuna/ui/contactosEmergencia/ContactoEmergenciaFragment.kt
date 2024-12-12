package com.torrezpillcokevin.nuna.ui.contactosEmergencia

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.torrezpillcokevin.nuna.R

class ContactoEmergenciaFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_contacto_emergencia, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val spinner: Spinner = view.findViewById(R.id.spinner_linea_telefonica)
        val addContactButton: Button = view.findViewById(R.id.addContactButton)
        val contactTable: TableLayout = view.findViewById(R.id.contactTable)

        // Configurar el Spinner con las líneas telefónicas
        val lineasTelefonicas = resources.getStringArray(R.array.lineas_telefonicas)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, lineasTelefonicas)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        addContactButton.setOnClickListener {
            val name = view.findViewById<EditText>(R.id.nameEditText).text.toString()
            val phone = view.findViewById<EditText>(R.id.phoneEditText).text.toString()
            val line = spinner.selectedItem.toString()

            if (name.isBlank() || phone.isBlank() || line.isBlank()) {
                Toast.makeText(requireContext(), "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newRow = TableRow(context)
            val nameTextView = TextView(context).apply { text = name }
            val phoneTextView = TextView(context).apply { text = phone }
            val lineTextView = TextView(context).apply { text = line }

            val actionsLayout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            val editButton = Button(context).apply {
                text = "Editar"
                setOnClickListener {
                    view.findViewById<EditText>(R.id.nameEditText).setText(name)
                    view.findViewById<EditText>(R.id.phoneEditText).setText(phone)
                    spinner.setSelection(adapter.getPosition(line))
                    contactTable.removeView(newRow)
                }
            }

            val deleteButton = Button(context).apply {
                text = "Eliminar"
                setOnClickListener {
                    contactTable.removeView(newRow)
                }
            }

            actionsLayout.addView(editButton)
            actionsLayout.addView(deleteButton)

            newRow.addView(nameTextView)
            newRow.addView(phoneTextView)
            newRow.addView(lineTextView)
            newRow.addView(actionsLayout)

            contactTable.addView(newRow)
        }
    }
}