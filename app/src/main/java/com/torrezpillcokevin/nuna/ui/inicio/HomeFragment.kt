package com.torrezpillcokevin.nuna.ui.inicio

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.telephony.SmsManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import com.torrezpillcokevin.nuna.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment(), TextToSpeech.OnInitListener {

    // Constantes de permisos
    private val REQUEST_CALL_PERMISSION = 1
    private val REQUEST_RECORD_AUDIO_PERMISSION = 2
    private val REQUEST_LOCATION_PERMISSION = 3
    private val REQUEST_SEND_SMS = 101
    private val REQUEST_IMAGE_CAPTURE = 103

    // Views
    private lateinit var camaraIcono: ImageView
    private lateinit var messageIcono: ImageView
    private lateinit var microphoneIcono: ImageView
    private lateinit var phoneIcono: ImageView
    private lateinit var redButton: ImageView

    // TextToSpeech
    private lateinit var textToSpeech: TextToSpeech

    // Variables para grabación de audio
    private var mediaRecorder: MediaRecorder? = null
    private var audioFilePath: String? = null
    private var isRecording = false
    private var isProcessing = false

    // Variables para cámara
    private var photoUri: Uri? = null

    // Location services
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Inicializar views
        camaraIcono = view.findViewById(R.id.camaraIcono)
        messageIcono = view.findViewById(R.id.messageIcono)
        microphoneIcono = view.findViewById(R.id.microphoneIcono)
        phoneIcono = view.findViewById(R.id.phoneIcono)
        redButton = view.findViewById(R.id.redButton)

        // Inicializar location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Configurar listeners
        setupClickListeners()

        // Inicializar TextToSpeech
        textToSpeech = TextToSpeech(requireContext(), this)

        return view
    }

    private fun setupClickListeners() {
        camaraIcono.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                abrirCamara()
            } else {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), REQUEST_IMAGE_CAPTURE)
            }
        }

        messageIcono.setOnClickListener {
            sendEmergencyLocationSMS()
        }

        microphoneIcono.setOnClickListener {
            if (isRecording) {
                // Detener grabación si está activa
            } else {
                if (checkAudioPermissions()) {
                    startRecording()
                } else {
                    requestAudioPermissions()
                }
            }
        }

        phoneIcono.setOnClickListener {
            makePhoneCall()
        }

        redButton.setOnClickListener {
            executeEmergencyActions()
        }
    }

    private fun executeEmergencyActions() {
        if (isProcessing) {
            Toast.makeText(requireContext(), "Acciones ya en curso, por favor espera.", Toast.LENGTH_SHORT).show()
            return
        }

        isProcessing = true

        // Ejecutar la grabación
        if (checkAudioPermissions()) {
            startRecording()
        } else {
            requestAudioPermissions()
            isProcessing = false
            return
        }

        // Enviar ubicación por SMS
        sendEmergencyLocationSMS()

        // Tomar foto (opcional, puedes comentar si no quieres que tome foto automáticamente)
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            abrirCamara()
        }

        Toast.makeText(requireContext(), "Acciones de emergencia ejecutadas", Toast.LENGTH_SHORT).show()
        isProcessing = false
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech.language = Locale("es", "ES")
        } else {
            Log.e("HomeFragment", "TextToSpeech no se inicializó correctamente")
        }
    }

    private fun abrirCamara() {
        val resolver = requireContext().contentResolver
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

            if (intent.resolveActivity(requireContext().packageManager) != null) {
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
            } else {
                Toast.makeText(requireContext(), "No se pudo abrir la cámara", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Error al guardar la imagen", Toast.LENGTH_SHORT).show()
        }
    }

    private fun makePhoneCall() {
        val sharedPreferences = requireContext().getSharedPreferences("EmergencyPrefs", Context.MODE_PRIVATE)
        val emergencyPhoneNumber = sharedPreferences.getString("emergency_call_phone", null)

        if (emergencyPhoneNumber.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "No hay un contacto de emergencia configurado", Toast.LENGTH_SHORT).show()
            return
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            val callIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$emergencyPhoneNumber"))
            startActivity(callIntent)
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CALL_PHONE), REQUEST_CALL_PERMISSION)
        }
    }

    private fun checkAudioPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestAudioPermissions() {
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO_PERMISSION)
    }

    private fun startRecording() {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val audioFile = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC), "AUDIO_$timeStamp.3gp")
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
        Toast.makeText(requireContext(), "Grabación iniciada", Toast.LENGTH_SHORT).show()

        // Detener automáticamente después de 5 segundos
        Handler(Looper.getMainLooper()).postDelayed({
            if (isRecording) {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
                mediaRecorder = null
                isRecording = false
                Toast.makeText(requireContext(), "Grabación finalizada", Toast.LENGTH_SHORT).show()
                Log.d("AUDIO_RECORD", "Archivo guardado en: $audioFilePath")
            }
        }, 5000)
    }

    private fun checkLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            REQUEST_LOCATION_PERMISSION
        )
    }

    @SuppressLint("MissingPermission")
    private fun sendEmergencyLocationSMS() {
        val sharedPreferences = requireContext().getSharedPreferences("EmergencyPrefs", Context.MODE_PRIVATE)
        val emergencyNumbers = sharedPreferences.getStringSet("emergency_sms_phones", emptySet())

        if (emergencyNumbers.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "No hay contactos de emergencia configurados", Toast.LENGTH_SHORT).show()
            return
        }

        // Verificar permisos de SMS
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.SEND_SMS), REQUEST_SEND_SMS)
            return
        }

        // Verificar permisos de ubicación
        if (!checkLocationPermissions()) {
            requestLocationPermissions()
            return
        }

        Toast.makeText(requireContext(), "Obteniendo ubicación...", Toast.LENGTH_SHORT).show()

        // Primero intentar obtener la última ubicación conocida
        fusedLocationClient.lastLocation.addOnSuccessListener { lastLocation ->
            if (lastLocation != null) {
                sendLocationSMS(lastLocation.latitude, lastLocation.longitude, emergencyNumbers)
            } else {
                // Si no hay última ubicación, solicitar nueva
                requestNewLocation(emergencyNumbers)
            }
        }.addOnFailureListener {
            // Si falla obtener la última ubicación, solicitar nueva
            requestNewLocation(emergencyNumbers)
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocation(emergencyNumbers: Set<String>) {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(1000)
            .setMaxUpdateDelayMillis(10000)
            .setMaxUpdates(1)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                fusedLocationClient.removeLocationUpdates(this)

                val location = locationResult.lastLocation
                if (location != null) {
                    sendLocationSMS(location.latitude, location.longitude, emergencyNumbers)
                } else {
                    sendEmergencyMessageWithoutLocation(emergencyNumbers)
                }
            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                if (!locationAvailability.isLocationAvailable) {
                    fusedLocationClient.removeLocationUpdates(this)
                    sendEmergencyMessageWithoutLocation(emergencyNumbers)
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

            // Timeout después de 8 segundos
            Handler(Looper.getMainLooper()).postDelayed({
                fusedLocationClient.removeLocationUpdates(locationCallback)
                sendEmergencyMessageWithoutLocation(emergencyNumbers)
            }, 8000)
        } catch (e: SecurityException) {
            Log.e("LOCATION_ERROR", "Security exception", e)
            sendEmergencyMessageWithoutLocation(emergencyNumbers)
        }
    }

    private fun sendLocationSMS(latitude: Double, longitude: Double, emergencyNumbers: Set<String>) {
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
            Toast.makeText(requireContext(), " Ubicación de emergencia enviada", Toast.LENGTH_SHORT).show()
            Log.d("EMERGENCY_SMS", "Location sent: $latitude, $longitude")
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error al enviar la ubicación", Toast.LENGTH_SHORT).show()
            Log.e("SMS_ERROR", "Error sending location SMS", e)
        }
    }

    private fun sendEmergencyMessageWithoutLocation(emergencyNumbers: Set<String>) {
        val message = "EMERGENCIA \nNecesito ayuda urgente.\n⚠ No se pudo obtener mi ubicación exacta. Por favor contactar inmediatamente."

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
            Toast.makeText(requireContext(), "⚠Mensaje de emergencia enviado (sin ubicación)", Toast.LENGTH_LONG).show()
            Log.d("EMERGENCY_SMS", "Emergency message sent without location")
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error al enviar mensaje de emergencia", Toast.LENGTH_SHORT).show()
            Log.e("SMS_ERROR", "Error sending emergency SMS", e)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            photoUri?.let { uri ->
                Toast.makeText(requireContext(), "Imagen guardada en la galería", Toast.LENGTH_SHORT).show()
            } ?: run {
                Toast.makeText(requireContext(), "No se pudo guardar la imagen", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(requireContext(), "Permiso de grabación denegado", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(requireContext(), "Permiso de ubicación concedido", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_SEND_SMS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    sendEmergencyLocationSMS()
                } else {
                    Toast.makeText(requireContext(), "Permiso para enviar SMS denegado", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_CALL_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    makePhoneCall()
                } else {
                    Toast.makeText(requireContext(), "Permiso para realizar llamadas denegado", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_IMAGE_CAPTURE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    abrirCamara()
                } else {
                    Toast.makeText(requireContext(), "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Cleanup
        mediaRecorder?.apply {
            if (isRecording) {
                stop()
            }
            release()
        }
        mediaRecorder = null

        textToSpeech.shutdown()

        // Remover location updates si están activos
        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}