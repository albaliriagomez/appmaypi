package com.torrezpillcokevin.nuna.ui.chatbot

import android.graphics.Color
import android.graphics.Rect
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.ai.client.generativeai.BuildConfig
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.ai.client.generativeai.GenerativeModel
import com.torrezpillcokevin.nuna.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ChatBotFragment : Fragment() {

    private lateinit var scrollView: ScrollView
    private lateinit var messagesContainer: LinearLayout
    private lateinit var inputMessage: EditText
    private lateinit var buttonSend: ImageView

    // Vistas a ocultar
    private lateinit var initialImage: ImageView  // Imagen inicial
    private lateinit var initialCard: CardView  // Tarjeta o CardView inicial
    private lateinit var secondCard: CardView  // Segunda tarjeta
    private lateinit var thirdCard: CardView  // Tercera tarjeta


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

        // Configuración del botón de enviar mensaje
        buttonSend.setOnClickListener {
            val messageText = inputMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                // Limpiar el contenedor de mensajes antes de agregar uno nuevo
               // clearMessagesContainer()

                // Hacer invisibles las imágenes o tarjetas iniciales
                initialImage.visibility = View.GONE
                initialCard.visibility = View.GONE
                secondCard.visibility = View.GONE  // Ocultar segunda tarjeta
                thirdCard.visibility = View.GONE  // Ocultar tercera tarjeta

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

    private fun clearMessagesContainer() {
        messagesContainer.removeAllViews()
    }

    private fun addMessageToContainer(message: String, isUserMessage: Boolean) {
        val messageView = TextView(context).apply {
            text = message
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(8, 8, 8, 8)
                gravity = if (isUserMessage) Gravity.END else Gravity.START
            }
            background = ContextCompat.getDrawable(
                requireContext(),
                if (isUserMessage) R.drawable.user_message_background else R.drawable.response_message_background
            )
            setPadding(16, 8, 16, 8)
            setTextColor(if (isUserMessage) Color.WHITE else Color.BLACK)
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
