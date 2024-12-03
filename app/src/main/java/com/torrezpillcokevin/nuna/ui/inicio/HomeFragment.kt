package com.torrezpillcokevin.nuna.ui.inicio

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.telephony.SmsManager
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.torrezpillcokevin.nuna.R
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment(), TextToSpeech.OnInitListener {

    private lateinit var camaraIcono: ImageView
    private lateinit var messageIcono: ImageView
    private lateinit var microphoneIcono: ImageView
    private lateinit var phoneIcono: ImageView
    private lateinit var redButton: ImageView
    private lateinit var textToSpeech: TextToSpeech

    private val predefinedNumber = "59176446793"
    private val messageText = "¡Ayuda! Emergencia detectada."
    private lateinit var photoUri: Uri
    private var mediaRecorder: MediaRecorder? = null
    private var audioFilePath: String? = null
    private var isRecording = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        camaraIcono = view.findViewById(R.id.camaraIcono)
        messageIcono = view.findViewById(R.id.messageIcono)
        microphoneIcono = view.findViewById(R.id.microphoneIcono)
        phoneIcono = view.findViewById(R.id.phoneIcono)
        redButton = view.findViewById(R.id.redButton)

        camaraIcono.setOnClickListener { abrirCamara() }
        messageIcono.setOnClickListener { enviarSMS(predefinedNumber, messageText) }
        microphoneIcono.setOnClickListener { grabarAudio() }
        phoneIcono.setOnClickListener { realizarLlamada() }

        redButton.setOnClickListener {
            abrirCamara()
            enviarSMS(predefinedNumber, messageText)
            grabarAudio()
            realizarLlamada()
            Toast.makeText(requireContext(), "Todas las acciones ejecutadas", Toast.LENGTH_SHORT).show()
        }

        textToSpeech = TextToSpeech(requireContext(), this)

        return view
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech.language = Locale("es", "ES")
        } else {
            Log.e("HomeFragment", "TextToSpeech no se inicializó correctamente")
        }
    }

    private fun abrirCamara() {
        val photoFile = try {
            createImageFile()
        } catch (e: IOException) {
            Toast.makeText(requireContext(), "Error al crear el archivo de imagen", Toast.LENGTH_SHORT).show()
            null
        }

        photoFile?.also {
            photoUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", it)
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            startActivity(intent)
        }
    }

    private fun enviarSMS(numero: String, mensaje: String) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(numero, null, mensaje, null, null)
            Toast.makeText(requireContext(), "Mensaje enviado", Toast.LENGTH_SHORT).show()
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.SEND_SMS), 1)
        }
    }

    private fun grabarAudio() {
        if (isRecording) {
            detenerGrabacion()
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                iniciarGrabacion()
            } else {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.RECORD_AUDIO), 2)
            }
        }
    }

    private fun iniciarGrabacion() {
        audioFilePath = createTempFile("temp_audio", ".3gp", requireContext().cacheDir).absolutePath
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
    }

    private fun detenerGrabacion() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        isRecording = false
        Toast.makeText(requireContext(), "Grabación finalizada", Toast.LENGTH_SHORT).show()
    }

    private fun realizarLlamada() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$predefinedNumber"))
            startActivity(intent)
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CALL_PHONE), 3)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().getExternalFilesDir(null)
        return File.createTempFile("IMG_$timeStamp", ".jpg", storageDir)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        textToSpeech.shutdown()
    }
}
