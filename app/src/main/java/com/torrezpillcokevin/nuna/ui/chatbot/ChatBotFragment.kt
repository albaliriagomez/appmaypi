package com.torrezpillcokevin.nuna.ui.chatbot

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.ai.client.generativeai.GenerativeModel
import com.torrezpillcokevin.nuna.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class ChatBotFragment : Fragment() {

    private lateinit var scrollView: ScrollView
    private lateinit var messagesContainer: LinearLayout
    private lateinit var inputMessage: EditText
    private lateinit var buttonSend: ImageView
    private lateinit var buttonMic: ImageView

    // Vistas a ocultar
    private lateinit var initialImage: ImageView
    private lateinit var initialCard: CardView
    private lateinit var secondCard: CardView
    private lateinit var thirdCard: CardView

    // Para reconocimiento de voz
    private lateinit var speechRecognizer: SpeechRecognizer
    private var isListening = false

    // Solicitar permisos
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startSpeechRecognition()
        } else {
            Toast.makeText(
                requireContext(),
                "Se necesita permiso de micrófono para transcribir",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_chat_bot, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        messagesContainer = view.findViewById(R.id.messagesContainer)
        inputMessage = view.findViewById(R.id.inputMessage)
        buttonSend = view.findViewById(R.id.buttonSend)
        scrollView = view.findViewById(R.id.scrollView)

        // Imagen y tarjetas iniciales
        initialImage = view.findViewById(R.id.imageSearchGuide)
        initialCard = view.findViewById(R.id.cardSearchGuide)
        secondCard = view.findViewById(R.id.cardEmotionalSupport)
        thirdCard = view.findViewById(R.id.cardSecond)

        // Botón de micrófono
        buttonMic = view.findViewById(R.id.buttonMic)
        buttonMic.setOnClickListener {
            checkMicrophonePermission()
        }

        // Inicializar reconocimiento de voz
        initSpeechRecognizer()

        // Configuración del botón de enviar mensaje
        buttonSend.setOnClickListener {
            val messageText = inputMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                // Hacer invisibles las imágenes o tarjetas iniciales
                initialImage.visibility = View.GONE
                initialCard.visibility = View.GONE
                secondCard.visibility = View.GONE
                thirdCard.visibility = View.GONE

                addMessageToContainer(messageText, isUserMessage = true)
                inputMessage.text?.clear()
                scrollToBottom()
                fetchGeminiResponse(messageText)
            }
        }

        // Desplazarse automáticamente cuando el teclado está visible
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            val rect = Rect()
            view.getWindowVisibleDisplayFrame(rect)
            val screenHeight = view.rootView.height
            val keypadHeight = screenHeight - rect.bottom

            if (keypadHeight > screenHeight * 0.15) {
                scrollToBottom()
            }
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(listener)

        // Limpia el listener en onDestroyView
        view.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }

    private fun initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(requireContext())) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())
            speechRecognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    // Cuando está listo para escuchar
                    isListening = true
                    // Cambiar el icono del micrófono a un estado activo
                    buttonMic.setImageResource(R.drawable.mic_active)
                    Toast.makeText(requireContext(), "Escuchando...", Toast.LENGTH_SHORT).show()
                }

                override fun onBeginningOfSpeech() {
                    // Cuando comienza a hablar el usuario
                }

                override fun onRmsChanged(rmsdB: Float) {
                    // Cambios en el nivel de sonido
                }

                override fun onBufferReceived(buffer: ByteArray?) {
                    // Buffer de audio recibido
                }

                override fun onEndOfSpeech() {
                    // Cuando termina de hablar el usuario
                    isListening = false
                    // Restaurar el icono del micrófono
                    buttonMic.setImageResource(R.drawable.mic_24px)
                }

                override fun onError(error: Int) {
                    // Error en el reconocimiento
                    isListening = false
                    buttonMic.setImageResource(R.drawable.mic_24px)

                    val errorMessage = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Error de audio"
                        SpeechRecognizer.ERROR_CLIENT -> "Error de cliente"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permisos insuficientes"
                        SpeechRecognizer.ERROR_NETWORK -> "Error de red"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Tiempo de espera de red agotado"
                        SpeechRecognizer.ERROR_NO_MATCH -> "No se encontró coincidencia"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Reconocedor ocupado"
                        SpeechRecognizer.ERROR_SERVER -> "Error del servidor"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No se detectó voz"
                        else -> "Error desconocido"
                    }

                    Log.e("ChatBotFragment", "Error en reconocimiento de voz: $errorMessage")
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                }

                override fun onResults(results: Bundle?) {
                    // Resultados del reconocimiento
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val recognizedText = matches[0]
                        inputMessage.setText(recognizedText)
                        // Opcional: enviar automáticamente el mensaje
                        // buttonSend.performClick()
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    // Resultados parciales
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val recognizedText = matches[0]
                        inputMessage.setText(recognizedText)
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {
                    // Otros eventos
                }
            })
        } else {
            Toast.makeText(
                requireContext(),
                "El reconocimiento de voz no está disponible en este dispositivo",
                Toast.LENGTH_SHORT
            ).show()
            buttonMic.isEnabled = false
        }
    }

    private fun checkMicrophonePermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Ya tenemos permiso
                if (isListening) {
                    stopSpeechRecognition()
                } else {
                    startSpeechRecognition()
                }
            }
            shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                // Explicar al usuario por qué necesitamos el permiso
                Toast.makeText(
                    requireContext(),
                    "Se necesita permiso de micrófono para transcribir voz a texto",
                    Toast.LENGTH_LONG
                ).show()
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
            else -> {
                // Solicitar el permiso
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    private fun startSpeechRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla ahora...")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        try {
            speechRecognizer.startListening(intent)
        } catch (e: Exception) {
            Log.e("ChatBotFragment", "Error al iniciar reconocimiento de voz", e)
            Toast.makeText(
                requireContext(),
                "Error al iniciar reconocimiento de voz",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun stopSpeechRecognition() {
        if (isListening) {
            speechRecognizer.stopListening()
            isListening = false
            buttonMic.setImageResource(R.drawable.mic_24px)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::speechRecognizer.isInitialized) {
            speechRecognizer.destroy()
        }
    }

    private fun clearMessagesContainer() {
        messagesContainer.removeAllViews()
    }

    private fun addMessageToContainer(message: String, isUserMessage: Boolean) {
        val context = requireContext()

        val messageView = TextView(context).apply {
            text = message
            setPadding(32, 24, 32, 24)
            textSize = 16f
            maxWidth = 700
            setTextColor(if (isUserMessage) Color.WHITE else Color.BLACK)
            background = ContextCompat.getDrawable(
                context,
                if (isUserMessage) R.drawable.user_message_background
                else R.drawable.response_message_background
            )

            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 8, 16, 8)
                gravity = if (isUserMessage) Gravity.END else Gravity.START
            }

            this.layoutParams = layoutParams
        }

        messagesContainer.addView(messageView)
    }

    private fun scrollToBottom() {
        scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
    }

    private fun fetchGeminiResponse(userMessage: String) {
        // Inicializa el modelo generativo Gemini
        val generativeModel = GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = "AIzaSyAN1t0IA7WLrCf8Tgx_dKJOKxrM6ttjZ8I"
        )

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Imprimir el mensaje que el usuario envía
                Log.d("ChatBotFragment", "Mensaje del usuario: $userMessage")

                // Llamar al modelo generativo
                val response = generativeModel.generateContent(userMessage)

                // Imprimir la respuesta del modelo
                Log.d("ChatBotFragment", "Respuesta de Gemini: ${response.text}")

                withContext(Dispatchers.Main) {
                    addMessageToContainer(response.text.toString(), isUserMessage = false)
                    scrollToBottom()
                }
            } catch (e: Exception) {
                Log.e("ChatBotFragment", "Error al obtener respuesta de Gemini", e)
                withContext(Dispatchers.Main) {
                    addMessageToContainer("Error al obtener respuesta: ${e.message}", isUserMessage = false)
                    scrollToBottom()
                }
            }
        }
    }
}
