package com.torrezpillcokevin.nuna.ui.asistente

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import androidx.fragment.app.Fragment

import com.google.gson.Gson
import com.torrezpillcokevin.nuna.R


import java.io.IOException

import java.util.Locale

data class Asistente(
    val mensaje: String,
    val width: String,
    val height: String, // Corregido de "heignt" a "height"
    val base64: String
)

class HomeFragment : Fragment(), TextToSpeech.OnInitListener {
    private lateinit var imageView: ImageView
    private lateinit var textToSpeech: TextToSpeech // Agregar TextToSpeech

    val texto = "Hola, bienvenido. ¿En qué te puedo ayudar hoy?"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        imageView = view.findViewById(R.id.image_view)

        try {
            val json = loadJSONFromAsset(requireContext(), "robotfondoblanco.json")
            json?.let {
                val asistente = Gson().fromJson(it, Asistente::class.java)
                asistente.base64?.let { base64 ->
                    setImageFromBase64(base64, imageView)
                    startEyeAnimation(imageView)
                }
            }
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error al cargar el JSON o la imagen", e)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Inicializar TextToSpeech
        textToSpeech = TextToSpeech(requireContext(), this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Configurar el idioma a español
            val result = textToSpeech.setLanguage(Locale("es", "ES"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("HomeFragment", "El idioma no es soportado")
            } else {
                // Obtener la lista de voces disponibles
                val voices = textToSpeech.voices
                var selectedVoice: Voice? = null

                for (voice in voices) {
                    Log.d("HomeFragment", "Voz disponible: ${voice.name}, ${voice.locale}")

                    // Asegúrate de que la voz es en español y buscar una que típicamente sea masculina
                    if (voice.locale == Locale("es", "ES") && voice.name.contains("Male", ignoreCase = true)) {
                        selectedVoice = voice
                        break
                    }
                }

                // Si se encontró una voz masculina, establecerla
                if (selectedVoice != null) {
                    textToSpeech.voice = selectedVoice
                    Log.d("HomeFragment", "Voz seleccionada: ${selectedVoice.name}")
                } else {
                    Log.e("HomeFragment", "No se encontró una voz masculina en español.")
                }

                // Hablar el texto de bienvenida
                speak(texto)
            }
        } else {
            Log.e("HomeFragment", "Inicialización de TextToSpeech fallida")
        }
    }

    private fun speak(text: String) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Liberar TextToSpeech al cerrar el fragmento
        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }

    private fun loadJSONFromAsset(context: Context, fileName: String): String? {
        return try {
            val inputStream = context.assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charsets.UTF_8)
        } catch (e: IOException) {
            Log.e("HomeFragment", "Error al leer el archivo JSON", e)
            null
        }
    }

    private fun setImageFromBase64(base64: String, imageView: ImageView) {
        try {
            val base64String = base64.substringAfter(",") // Elimina el prefijo si está presente
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            imageView.setImageBitmap(bitmap)
        } catch (e: IllegalArgumentException) {
            Log.e("HomeFragment", "Error al decodificar la imagen en Base64", e)
        }
    }

    private fun startEyeAnimation(imageView: ImageView) {
        val eyeAnimation = ObjectAnimator.ofFloat(imageView, "translationY", -10f, 10f)
        eyeAnimation.duration = 500
        eyeAnimation.repeatCount = ValueAnimator.INFINITE
        eyeAnimation.repeatMode = ValueAnimator.REVERSE
        eyeAnimation.start()
    }
}