package com.torrezpillcokevin.nuna

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.telephony.SmsManager
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.gms.location.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.torrezpillcokevin.nuna.clases.BackgroundButtonService
import com.torrezpillcokevin.nuna.clases.ContactAdapter
import com.torrezpillcokevin.nuna.dbSqlite.DatabaseHelper
import com.torrezpillcokevin.nuna.models.Contact

class MainActivity : AppCompatActivity() {

    private val REQUEST_CALL_PERMISSION = 1
    private val REQUEST_RECORD_AUDIO_PERMISSION = 2
    private val REQUEST_LOCATION_PERMISSION = 3
    private var mediaRecorder: MediaRecorder? = null
    private var audioFilePath: String? = null
    private var isRecording = false
    private var isProcessing = false

    private lateinit var camaraIcono: ImageView
    private lateinit var messageIcono: ImageView
    private val REQUEST_IMAGE_CAPTURE = 103
    private val REQUEST_SEND_SMS = 101
    private var photoUri: Uri? = null
    private lateinit var addContactButton: Button

    // Location services
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    //base de datos sqlite
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var contactAdapter: ContactAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_main)

        // Inicializar location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //SQLite
        dbHelper = DatabaseHelper(this)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        loadContacts()

        val loginButton: Button = findViewById(R.id.loginButton)
        val registroButton: Button = findViewById(R.id.registerButton)

        messageIcono = findViewById(R.id.messageIcono)
        camaraIcono = findViewById(R.id.camaraIcono)

        camaraIcono.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                abrirCamara()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_IMAGE_CAPTURE)
            }
        }

        messageIcono.setOnClickListener {
            sendEmergencyLocationSMS(this)
        }

        val redButton = findViewById<ImageView>(R.id.redButton)
        redButton.setOnClickListener {
            if (isProcessing) {
                Toast.makeText(this, "Acciones ya en curso, por favor espera.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            var isProcessing = true

            // Ejecutar la grabación
            if (checkAudioPermissions()) {
                startRecording()
            } else {
                requestAudioPermissions()
                isProcessing = false
                return@setOnClickListener
            }

            // Enviar ubicación por SMS
            sendEmergencyLocationSMS(this)

            // Al finalizar todas las acciones, restablece la bandera
            Toast.makeText(this, "Acciones de emergencia ejecutadas", Toast.LENGTH_SHORT).show()
            isProcessing = false
        }

        findViewById<ImageView>(R.id.phoneIcono).setOnClickListener {
            makePhoneCall(this)
        }

        findViewById<ImageView>(R.id.microphoneIcono).setOnClickListener {
            if (isRecording) {
                //stopRecordingAndPlay()
            } else {
                if (checkAudioPermissions()) {
                    startRecording()
                } else {
                    requestAudioPermissions()
                }
            }
        }

        // Inicialización de vistas
        addContactButton = findViewById(R.id.addContactButton)
        addContactButton.setOnClickListener {
            showAddContactDialog()
        }

        val settingsButton = findViewById<ImageButton>(R.id.settingsButton)
        settingsButton.setOnClickListener {
            showEmergencyConfigDialog(this)
        }

        loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        registroButton.setOnClickListener {
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            REQUEST_LOCATION_PERMISSION
        )
    }

    @SuppressLint("MissingPermission")
    private fun sendEmergencyLocationSMS(context: Context) {
        val sharedPreferences = context.getSharedPreferences("EmergencyPrefs", Context.MODE_PRIVATE)
        val emergencyNumbers = sharedPreferences.getStringSet("emergency_sms_phones", emptySet())

        if (emergencyNumbers.isNullOrEmpty()) {
            Toast.makeText(context, "No hay contactos de emergencia configurados", Toast.LENGTH_SHORT).show()
            return
        }

        // Verificar permisos de SMS
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            if (context is Activity) {
                ActivityCompat.requestPermissions(context, arrayOf(Manifest.permission.SEND_SMS), REQUEST_SEND_SMS)
            }
            return
        }

        // Verificar permisos de ubicación
        if (!checkLocationPermissions()) {
            requestLocationPermissions()
            return
        }

        Toast.makeText(context, "Obteniendo ubicación...", Toast.LENGTH_SHORT).show()

        // Primero intentar obtener la última ubicación conocida
        fusedLocationClient.lastLocation.addOnSuccessListener { lastLocation ->
            if (lastLocation != null) {
                sendLocationSMS(context, lastLocation.latitude, lastLocation.longitude, emergencyNumbers)
            } else {
                // Si no hay última ubicación, solicitar nueva
                requestNewLocation(context, emergencyNumbers)
            }
        }.addOnFailureListener {
            // Si falla obtener la última ubicación, solicitar nueva
            requestNewLocation(context, emergencyNumbers)
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocation(context: Context, emergencyNumbers: Set<String>) {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(1000)
            .setMaxUpdateDelayMillis(10000)
            .setMaxUpdates(1) // Solo una actualización
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                fusedLocationClient.removeLocationUpdates(this)

                val location = locationResult.lastLocation
                if (location != null) {
                    sendLocationSMS(context, location.latitude, location.longitude, emergencyNumbers)
                } else {
                    // Como último recurso, enviar mensaje sin ubicación
                    sendEmergencyMessageWithoutLocation(context, emergencyNumbers)
                }
            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                if (!locationAvailability.isLocationAvailable) {
                    fusedLocationClient.removeLocationUpdates(this)
                    // Enviar mensaje sin ubicación si no está disponible
                    sendEmergencyMessageWithoutLocation(context, emergencyNumbers)
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

            // Timeout después de 8 segundos
            Handler(Looper.getMainLooper()).postDelayed({
                fusedLocationClient.removeLocationUpdates(locationCallback)
                sendEmergencyMessageWithoutLocation(context, emergencyNumbers)
            }, 8000)
        } catch (e: SecurityException) {
            Log.e("LOCATION_ERROR", "Security exception", e)
            sendEmergencyMessageWithoutLocation(context, emergencyNumbers)
        }
    }

    private fun sendLocationSMS(context: Context, latitude: Double, longitude: Double, emergencyNumbers: Set<String>) {
        val googleMapsUrl = "https://maps.google.com/?q=$latitude,$longitude"
        val message = " EMERGENCIA \nNecesito ayuda urgente.\n Mi ubicación:\n$googleMapsUrl"

        try {
            val smsManager = SmsManager.getDefault()
            emergencyNumbers.forEach { numero ->
                val parts = smsManager.divideMessage(message)
                if (parts.size == 1) {
                    smsManager.sendTextMessage(numero, null, message, null, null)
                } else {
                    smsManager.sendMultipartTextMessage(numero, null, parts, null, null)
                }
            }
            Toast.makeText(context, " Ubicación de emergencia enviada", Toast.LENGTH_SHORT).show()
            Log.d("EMERGENCY_SMS", "Location sent: $latitude, $longitude")
        } catch (e: Exception) {
            Toast.makeText(context, "Error al enviar la ubicación", Toast.LENGTH_SHORT).show()
            Log.e("SMS_ERROR", "Error sending location SMS", e)
        }
    }

    private fun sendEmergencyMessageWithoutLocation(context: Context, emergencyNumbers: Set<String>) {
        val message = " EMERGENCIA \nNecesito ayuda urgente.\n⚠️ No se pudo obtener mi ubicación exacta. Por favor contactar inmediatamente."

        try {
            val smsManager = SmsManager.getDefault()
            emergencyNumbers.forEach { numero ->
                val parts = smsManager.divideMessage(message)
                if (parts.size == 1) {
                    smsManager.sendTextMessage(numero, null, message, null, null)
                } else {
                    smsManager.sendMultipartTextMessage(numero, null, parts, null, null)
                }
            }
            Toast.makeText(context, "⚠️ Mensaje de emergencia enviado (sin ubicación)", Toast.LENGTH_LONG).show()
            Log.d("EMERGENCY_SMS", "Emergency message sent without location")
        } catch (e: Exception) {
            Toast.makeText(context, "Error al enviar mensaje de emergencia", Toast.LENGTH_SHORT).show()
            Log.e("SMS_ERROR", "Error sending emergency SMS", e)
        }
    }

    private fun abrirCamara() {
        val resolver = contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        if (uri != null) {
            photoUri = uri
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, uri)
            }

            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
            } else {
                Toast.makeText(this, "No se pudo abrir la cámara", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Error al guardar la imagen", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            photoUri?.let { uri ->
                Toast.makeText(this, "Imagen guardada en la galería", Toast.LENGTH_SHORT).show()
            } ?: run {
                Toast.makeText(this, "No se pudo guardar la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_RECORD_AUDIO_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startRecording()
                } else {
                    Toast.makeText(this, "Permiso de grabación denegado", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permiso de ubicación concedido", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_SEND_SMS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    sendEmergencyLocationSMS(this)
                } else {
                    Toast.makeText(this, "Permiso para enviar SMS denegado", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun makePhoneCall(context: Context) {
        val sharedPreferences = context.getSharedPreferences("EmergencyPrefs", Context.MODE_PRIVATE)
        val emergencyPhoneNumber = sharedPreferences.getString("emergency_call_phone", null)

        if (emergencyPhoneNumber.isNullOrEmpty()) {
            Toast.makeText(context, "No hay un contacto de emergencia configurado", Toast.LENGTH_SHORT).show()
            return
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            val callIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$emergencyPhoneNumber"))
            context.startActivity(callIntent)
        } else {
            if (context is Activity) {
                ActivityCompat.requestPermissions(context, arrayOf(Manifest.permission.CALL_PHONE), REQUEST_CALL_PERMISSION)
            } else {
                Toast.makeText(context, "No se puede solicitar el permiso en este contexto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkAudioPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestAudioPermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO_PERMISSION)
    }

    private fun startRecording() {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val audioFile = File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "AUDIO_$timeStamp.3gp")
        audioFilePath = audioFile.absolutePath

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(audioFilePath)
            prepare()
            start()
        }
        isRecording = true
        Toast.makeText(this, "Grabación iniciada", Toast.LENGTH_SHORT).show()

        Handler(Looper.getMainLooper()).postDelayed({
            if (isRecording) {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
                mediaRecorder = null
                isRecording = false
                Toast.makeText(this, "Grabación finalizada", Toast.LENGTH_SHORT).show()
                Log.d("AUDIO_RECORD", "Archivo guardado en: $audioFilePath")
            }
        }, 5000)
    }

    // ... resto de métodos sin cambios (showAddContactDialog, loadContacts, editContact, deleteContact, showEmergencyConfigDialog, etc.)

    private fun showAddContactDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_contact, null)

        val nameEditText = dialogView.findViewById<EditText>(R.id.nameEditText)
        val phoneEditText = dialogView.findViewById<EditText>(R.id.phoneEditText)
        val lineSpinner = dialogView.findViewById<Spinner>(R.id.lineSpinner)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

        val operators = arrayOf("Seleccione línea", "Entel", "Tigo", "Viva")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, operators)
        lineSpinner.adapter = adapter

        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        btnSave.setOnClickListener {
            val name = nameEditText.text.toString()
            val phone = phoneEditText.text.toString()
            val line = lineSpinner.selectedItem.toString()

            if (name.isNotEmpty() && phone.isNotEmpty() && line != "Seleccione línea") {
                val contact = Contact(name = name, phone = phone, line = line)
                val result = dbHelper.addContact(contact)

                if (result != -1L) {
                    loadContacts()
                    Toast.makeText(this, "Contacto guardado", Toast.LENGTH_SHORT).show()

                    val contacts = dbHelper.getAllContacts()
                    if (contacts.size == 1) {
                        val sharedPreferences = getSharedPreferences("EmergencyPrefs", Context.MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("emergency_call_phone", phone)
                        editor.putStringSet("emergency_sms_phones", setOf(phone))
                        editor.apply()
                    }

                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos y selecciona una línea", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun loadContacts() {
        val contacts = dbHelper.getAllContacts()
        contactAdapter = ContactAdapter(contacts, ::editContact, ::deleteContact)
        recyclerView.adapter = contactAdapter
    }

    private fun editContact(contact: Contact) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_contact, null)

        val nameEditText = dialogView.findViewById<EditText>(R.id.nameEditText)
        val phoneEditText = dialogView.findViewById<EditText>(R.id.phoneEditText)
        val lineSpinner = dialogView.findViewById<Spinner>(R.id.lineSpinner)
        val btnSave = dialogView.findViewById<Button>(R.id.btnEdit)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

        nameEditText.setText(contact.name)
        phoneEditText.setText(contact.phone)

        val operators = arrayOf("Seleccione línea", "Entel", "Tigo", "Viva")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, operators)
        lineSpinner.adapter = adapter

        val position = operators.indexOf(contact.line)
        if (position >= 0) lineSpinner.setSelection(position)

        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        btnSave.setOnClickListener {
            val newName = nameEditText.text.toString()
            val newPhone = phoneEditText.text.toString()
            val newLine = lineSpinner.selectedItem.toString()

            if (newName.isNotEmpty() && newPhone.isNotEmpty() && newLine != "Seleccione línea") {
                val updatedContact = Contact(contact.id, newName, newPhone, newLine)
                dbHelper.updateContact(updatedContact)
                loadContacts()
                dialog.dismiss()
                Toast.makeText(this, "Contacto actualizado", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun deleteContact(contact: Contact) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_delete_contact, null)
        val dialog = AlertDialog.Builder(this)
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
                loadContacts()
                Toast.makeText(this, "${contact.name} eliminado", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    fun showEmergencyConfigDialog(context: Context) {
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
}