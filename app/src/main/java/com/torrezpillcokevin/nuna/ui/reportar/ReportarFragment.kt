package com.torrezpillcokevin.nuna.ui.reportar

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.gson.Gson
import com.mapbox.geojson.Point
import com.torrezpillcokevin.nuna.LoginActivity
import com.torrezpillcokevin.nuna.R
import com.torrezpillcokevin.nuna.data.Report
import com.torrezpillcokevin.nuna.data.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ReportarFragment : Fragment() {

    //variables para subir la foto
    private lateinit var imageView: ImageView
    private lateinit var selectImageLauncher: ActivityResultLauncher<Intent>
    //para enviar
    private lateinit var dateField: TextInputEditText
    private lateinit var descriptionField: TextInputEditText
    private lateinit var nombreField: TextInputEditText
    private lateinit var emailField: TextInputEditText
    private lateinit var telefonoField: TextInputEditText

    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null
    private var imagenSeleccionadaPart: MultipartBody.Part? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_reportar, container, false)

        // Establecer los valores iniciales vacíos (si es necesario)
        nombreField = view.findViewById(R.id.nombreField)
        emailField = view.findViewById(R.id.emailField)
        telefonoField = view.findViewById(R.id.telefonoField)
        descriptionField = view.findViewById(R.id.descriptionField)

        cargarDatosUsuario()

        // Configurar el campo de fecha y hora
        dateField = view.findViewById(R.id.dateField)
        dateField.setOnClickListener {
            showDateTimePicker()
        }

        imageView = view.findViewById(R.id.imageView)
        val selectImageButton = view.findViewById<Button>(R.id.selectImage)
        selectImageButton.setOnClickListener {
            seleccionarImagen()
        }

        //captura la imagen seleccionada
        selectImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val imageUri: Uri? = data?.data
                if (imageUri != null) {
                    procesarImagenSeleccionada(imageUri)
                }
            }
        }

        view.findViewById<Button>(R.id.btnImageLocation)?.setOnClickListener {
            mostrarMapDialog()
        }

        val sendReport: Button = view.findViewById(R.id.sendButton)
        sendReport.setOnClickListener {
            val imageFile = File(requireContext().cacheDir, "imagen_seleccionada.jpg")  // Asegúrate de que 'imagen_seleccionada.jpg' sea la imagen correcta
            if (imageFile.exists()) {
                enviarReporteConImagen(imageFile)
            } else {
                Toast.makeText(requireContext(), "Por favor selecciona una imagen primero", Toast.LENGTH_SHORT).show()
            }
        }


        return view
    }

    private fun seleccionarImagen() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        selectImageLauncher.launch(intent)
    }

    private fun procesarImagenSeleccionada(uri: Uri) {
        val context = requireContext()
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "imagen_seleccionada.jpg")

        inputStream?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        imageView.setImageURI(uri) // Para mostrar la imagen seleccionada
    }

    @SuppressLint("SuspiciousIndentation")
    private fun enviarReporteConImagen(imageFile: File) {
        val token = getToken()
        if (token == null) {
            Toast.makeText(requireContext(), "Error: Sesión no válida", Toast.LENGTH_SHORT).show()
            redirectToLogin()
            return
        }

        val nombre = nombreField.text.toString()
        val email = emailField.text.toString()
        val telefono = telefonoField.text.toString().toIntOrNull() ?: 0
        val fecha = dateField.text.toString()
        val ubicacionStr = "${selectedLatitude},${selectedLongitude}"
        val descripcion = descriptionField.text.toString()

        // Codifica imagen a Base64
        val base64Image = Base64.encodeToString(imageFile.readBytes(), Base64.NO_WRAP)

        // Construye el objeto Report
        val report = Report(
            nombre = nombre,
            email = email,
            telefono = telefono,
            fecha_avistamiento = fecha,
            ubicacion_avistamiento = ubicacionStr,
            descripcion = descripcion,
            imagen = base64Image
        )

        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.createReport("Bearer $token", report)

                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Reporte enviado con éxito", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    } else {
                    Toast.makeText(requireContext(), "Error al enviar el reporte: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error de red: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }





    private fun mostrarMapDialog() {
        val bottomSheetFragment = MapBottomSheetFragment()
        // Configurar el listener antes de mostrar el diálogo
        bottomSheetFragment.setLocationListener(object : MapBottomSheetFragment.OnLocationSelectedListener {
            override fun onLocationSelected(point: Point) {
                Log.d("UbicacionOBTENIDAAAAAA", "Lat: ${point.latitude()}, Lng: ${point.longitude()}")
                selectedLatitude = point.latitude()
                selectedLongitude= point.longitude()
            }
        })
        bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
    }

    private fun cargarDatosUsuario() {
        val userId = getUserId()!!

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Hacer la solicitud a la API para obtener los datos del usuario
                val response = RetrofitInstance.api.getUser(userId)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        response.body()?.let { usuario ->
                            // Llenar los campos con los datos obtenidos de la API
                            nombreField.setText(usuario.name) // Cambié 'nombre' por 'name'
                            emailField.setText(usuario.email)
                            telefonoField.setText(usuario.numero.toString()) // Cambié 'telefono' por 'numero'

                            // Log para confirmar que se cargaron los datos correctamente
                            Log.d("CargarDatosUsuario", "Usuario cargado: ${usuario.name}, ${usuario.email}, ${usuario.numero}")
                        }
                    } else {
                        Toast.makeText(requireContext(), "Error al cargar datos", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Mostrar un mensaje de error en caso de excepción
                    Log.e("CargarDatosUsuario", "Error de conexión: ${e.message}")
                    Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show()
                }
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
            Log.d("TOKEN_DEBUG", "Token recuperado: ${token?.take(5)}...")  // Muestra solo los primeros 5 caracteres por seguridad
            token
        } catch (e: Exception) {
            Log.e("SECURE_STORAGE", "Error al recuperar el token", e)
            null
        }
    }

    private fun getUserId(): Int? {
        val encryptedPrefs = EncryptedSharedPreferences.create(
            requireContext(),
            "SECURE_APP_PREFS",
            MasterKey.Builder(requireContext()).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val userId = encryptedPrefs.getInt("USER_ID", -1).takeIf { it != -1 }

        // Log para verificar si se obtuvo correctamente el userId
        if (userId != null) {
           // Log.d("GetUserId", "User ID obtenido: $userId")
        } else {
            Log.d("GetUserId", "No se encontró User ID, valor por defecto (-1)")
        }

        return userId
    }

    private fun redirectToLogin() {
        startActivity(Intent(requireContext(), LoginActivity::class.java))
        activity?.finish()
    }



    private fun showDateTimePicker() {
        // Crear selector de fecha
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Seleccionar fecha")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { selectedDate ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selectedDate

            // Mostrar el selector de hora con Material Time Picker
            val timePicker = MaterialTimePicker.Builder()
                .setTitleText("Seleccionar hora")
                .setTimeFormat(TimeFormat.CLOCK_24H) // Formato de 24 horas
                .build()

            timePicker.addOnPositiveButtonClickListener {
                // Obtener la hora seleccionada
                val hour = timePicker.hour
                val minute = timePicker.minute
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)

                // Formatear y mostrar fecha + hora
                val formattedDateTime = SimpleDateFormat(
                    "dd/MM/yyyy HH:mm", Locale.getDefault()
                ).format(calendar.time)

                dateField.setText(formattedDateTime)
            }

            timePicker.show(parentFragmentManager, "TIME_PICKER")
        }

        datePicker.show(parentFragmentManager, "DATE_PICKER")
    }
}