package com.torrezpillcokevin.nuna.ui.codigoRastreo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.torrezpillcokevin.nuna.R

class CodigoRastreoFragment : Fragment() {

    companion object {
        fun newInstance() = CodigoRastreoFragment()
    }

    private val viewModel: CodigoRastreoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Aquí puedes inicializar cualquier cosa que necesites para tu ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_codigo_rastreo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val autoCompleteStatus: AutoCompleteTextView = view.findViewById(R.id.autoCompleteStatus)
        val addCodeButton: Button = view.findViewById(R.id.addCodeButton)
        val codeTable: TableLayout = view.findViewById(R.id.codeTable)

        // Configurar el AutoCompleteTextView con las opciones de estado
        val statusOptions = resources.getStringArray(R.array.status_options)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, statusOptions)
        autoCompleteStatus.setAdapter(adapter)

        addCodeButton.setOnClickListener {
            // Lógica para agregar un nuevo código
            val code = view.findViewById<EditText>(R.id.codeEditText).text.toString()
            val status = autoCompleteStatus.text.toString()

            val newRow = TableRow(context)
            val codeTextView = TextView(context).apply { text = code }
            val statusTextView = TextView(context).apply { text = status }
            val actionsLayout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            val editButton = Button(context).apply {
                text = "Editar"
                setOnClickListener {
                    // Lógica para editar el código
                    view.findViewById<EditText>(R.id.codeEditText).setText(code)
                    autoCompleteStatus.setText(status)
                    codeTable.removeView(newRow)
                }
            }

            val deleteButton = Button(context).apply {
                text = "Eliminar"
                setOnClickListener {
                    // Lógica para eliminar el código
                    codeTable.removeView(newRow)
                }
            }

            actionsLayout.addView(editButton)
            actionsLayout.addView(deleteButton)

            newRow.addView(codeTextView)
            newRow.addView(statusTextView)
            newRow.addView(actionsLayout)

            codeTable.addView(newRow)
        }
    }
}