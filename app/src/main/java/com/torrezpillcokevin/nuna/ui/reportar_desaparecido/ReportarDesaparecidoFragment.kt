package com.torrezpillcokevin.nuna.ui.reportar_desaparecido


import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.mapbox.geojson.Point
import com.torrezpillcokevin.nuna.R
import com.torrezpillcokevin.nuna.data.ReporteDesaparecido
import com.torrezpillcokevin.nuna.data.RetrofitInstance
import com.torrezpillcokevin.nuna.ui.reportar_desaparecido.MapBottomSheetFragment
import java.util.Calendar

class ReportarDesaparecidoFragment : Fragment() {

    private lateinit var viewModel: ReportarDesaparecidoViewModel

    private lateinit var nombreEditText: EditText
    private lateinit var apellidoEditText: EditText
    private lateinit var descripcionEditText: EditText
    private lateinit var fechaNacimientoEditText: EditText
    private lateinit var fechaDesaparicionEditText: EditText
    private lateinit var lugarDesaparicionEditText: EditText
    private lateinit var nombreReportanteEditText: EditText
    private lateinit var telefonoReportanteEditText: EditText
    private lateinit var coordenadasEditText: EditText
    private lateinit var edadEditText: EditText
    private lateinit var caracteristicasEditText: EditText
    private lateinit var enviarButton: Button

    private lateinit var fotoPerfilImageView: ImageView
    private lateinit var fotoSucesoImageView: ImageView

    private var fotoPerfilUri: Uri? = null
    private var fotoSucesoUri: Uri? = null
    private lateinit var generoSpinner: Spinner

    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null

    private lateinit var btnSeleccionarFotoPerfil: Button
    private lateinit var btnSeleccionarFotoSuceso: Button

    private var seleccionandoFotoPerfil = false

    private val navController by lazy { findNavController() }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = inflater.inflate(R.layout.fragment_reportar_desaparecido, container, false)

        val apiService = RetrofitInstance.api
        val factory = ReportarDesaparecidoViewModelFactory(requireActivity().application, apiService)
        viewModel = ViewModelProvider(this, factory)[ReportarDesaparecidoViewModel::class.java]

        nombreEditText = view.findViewById(R.id.etNomCom)
        apellidoEditText = view.findViewById(R.id.etAp)
        generoSpinner = view.findViewById(R.id.spGenero)
        edadEditText = view.findViewById(R.id.etEdad)
        fechaNacimientoEditText = view.findViewById(R.id.etFechaNacimiento)
        fechaDesaparicionEditText = view.findViewById(R.id.etFechaDesaparicion)
        lugarDesaparicionEditText = view.findViewById(R.id.etLugarDesaparicion)
        caracteristicasEditText = view.findViewById(R.id.etCaracteristicas)
        fotoPerfilImageView = view.findViewById(R.id.imgFotoPerfil)
        fotoSucesoImageView = view.findViewById(R.id.imgFotoSuceso)
        coordenadasEditText = view.findViewById(R.id.etCoordenadas)
        descripcionEditText = view.findViewById(R.id.etDescripcion)
        nombreReportanteEditText = view.findViewById(R.id.etNomRepor)
        telefonoReportanteEditText = view.findViewById(R.id.etTelefonoReportante)
        enviarButton = view.findViewById(R.id.btnEnviarReporte)
        btnSeleccionarFotoPerfil = view.findViewById(R.id.btnSeleccionarFotoPerfil)
        btnSeleccionarFotoSuceso = view.findViewById(R.id.btnSeleccionarFotoSuceso)


        // Configurar Spinner de género
        val opcionesGenero = listOf("Masculino", "Femenino")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, opcionesGenero)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        generoSpinner.adapter = adapter

        // Setear listeners para mostrar DatePicker al tocar los EditText de fecha
        fechaNacimientoEditText.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener {
                showDatePickerDialog(this)
            }
        }

        fechaDesaparicionEditText.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener {
                showDatePickerDialog(this)
            }
        }

        lugarDesaparicionEditText.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener {
                showMapDialog(this)
            }
        }
        coordenadasEditText.apply {
            isFocusable = false
            isClickable = false
            isCursorVisible = false
            keyListener = null
        }

        btnSeleccionarFotoPerfil.setOnClickListener {
            seleccionandoFotoPerfil = true
            seleccionarImagenLauncher.launch("image/*")
        }

        btnSeleccionarFotoSuceso.setOnClickListener {
            seleccionandoFotoPerfil = false
            seleccionarImagenLauncher.launch("image/*")
        }


        enviarButton.setOnClickListener {
            // Validar que las fotos existan
            if (fotoPerfilUri == null || fotoSucesoUri == null) {
                Toast.makeText(requireContext(), "Debes seleccionar ambas fotos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.d("ReporteDesaparecido", "Iniciando proceso de envío (Multipart)")

            // Llamamos al ViewModel pasando las URIs y los textos de los EditText
            viewModel.reportarDesaparecido(
                requireContext(),
                nombreEditText.text.toString(),
                apellidoEditText.text.toString(),
                edadEditText.text.toString(),
                generoSpinner.selectedItem.toString(),
                descripcionEditText.text.toString(),
                fechaNacimientoEditText.text.toString(),
                fechaDesaparicionEditText.text.toString(),
                lugarDesaparicionEditText.text.toString(),
                caracteristicasEditText.text.toString(),
                nombreReportanteEditText.text.toString(),
                telefonoReportanteEditText.text.toString(),
                fotoPerfilUri!!,
                fotoSucesoUri!!
            )

            // Observar el resultado
            viewModel.reporteExitoso.observe(viewLifecycleOwner) { exito ->
                if (exito) {
                    Toast.makeText(requireContext(), "Reporte enviado con éxito", Toast.LENGTH_SHORT).show()
                    navController.navigate(R.id.nav_home)
                } else {
                    Toast.makeText(requireContext(), "Error al enviar el reporte", Toast.LENGTH_SHORT).show()
                }
            }
        }

        limpiarCampos()

        return view
    }



    private val seleccionarImagenLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                if (seleccionandoFotoPerfil) {
                    fotoPerfilImageView.setImageURI(it)
                    fotoPerfilUri = it // Guardamos la Uri real
                } else {
                    fotoSucesoImageView.setImageURI(it)
                    fotoSucesoUri = it // Guardamos la Uri real
                }
            }
        }

    private fun showMapDialog(targetEditText: EditText) {
        val bottomSheetFragment = MapBottomSheetFragment()

        bottomSheetFragment.setLocationListener(object : MapBottomSheetFragment.OnLocationSelectedListener {
            override fun onLocationSelected(point: Point) {
                selectedLatitude = point.latitude()
                selectedLongitude = point.longitude()

                // Mostrar texto al usuario
                targetEditText.setText("Ubicación seleccionada")

                // Guardar coordenadas en el campo invisible
                coordenadasEditText.setText("${selectedLatitude},${selectedLongitude}")
            }
        })

        bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
    }


    private fun showDatePickerDialog(targetEditText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val fechaSeleccionada = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                targetEditText.setText(fechaSeleccionada)
            }, year, month, day)

        datePickerDialog.show()
    }

    private fun limpiarCampos() {
        nombreEditText.text.clear()
        apellidoEditText.text.clear()
        generoSpinner.setSelection(0)
        descripcionEditText.text.clear()
        fechaNacimientoEditText.text.clear()
        fechaDesaparicionEditText.text.clear()
        lugarDesaparicionEditText.text.clear()
        nombreReportanteEditText.text.clear()
        telefonoReportanteEditText.text.clear()
        coordenadasEditText.text.clear()
        edadEditText.text.clear()
        caracteristicasEditText.text.clear()

        fotoPerfilUri = null
        fotoSucesoUri = null
        fotoPerfilImageView.setImageDrawable(null)
        fotoSucesoImageView.setImageDrawable(null)
    }
    private fun uriToBase64(uriString: String): String {
        if (uriString.isEmpty()) return ""
        return try {
            val uri = Uri.parse(uriString)
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            if (bytes != null) {
                android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
            } else ""
        } catch (e: Exception) {
            Log.e("Base64Error", "Error al convertir imagen: ${e.message}")
            ""
        }
    }
}
