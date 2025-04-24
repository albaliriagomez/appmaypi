package com.torrezpillcokevin.nuna

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.torrezpillcokevin.nuna.data.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import android.text.InputType
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import com.torrezpillcokevin.nuna.data.ApiService
import com.torrezpillcokevin.nuna.data.RetrofitInstance
import com.torrezpillcokevin.nuna.data.UserRequest
import kotlinx.coroutines.withContext
import java.util.UUID

class RegistroActivity : AppCompatActivity() {

    private lateinit var viewModel: RegistroViewModel
    private var isPasswordVisible = false

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_registro)

        // Inicializa el ViewModel con Factory
        val apiService = RetrofitInstance.api
        viewModel = ViewModelProvider(
            this,
            RegistroViewModelFactory(apiService)
        )[RegistroViewModel::class.java]

        // Observa el estado del registro
        viewModel.registroEstado.observe(this) { resultado ->
            when (resultado) {
                is RegistroViewModel.ResultadoRegistro.Exito -> {
                    Toast.makeText(this, resultado.mensaje, Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                }
                is RegistroViewModel.ResultadoRegistro.Error -> {
                    Toast.makeText(this, resultado.error, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Botón atrás
        findViewById<ImageButton>(R.id.backButton2).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        // Referencias a las vistas
        val nameEditText = findViewById<EditText>(R.id.nameEditText)
        val phoneEditText = findViewById<EditText>(R.id.phoneEditText)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val repeatPasswordEditText = findViewById<EditText>(R.id.repeatPasswordEditText)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val loginLinkTextView = findViewById<TextView>(R.id.loginLinkTextView)
        val togglePasswordVisibility = findViewById<ImageView?>(R.id.togglePasswordVisibility)

        togglePasswordVisibility?.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            val inputType = if (isPasswordVisible) {
                InputType.TYPE_CLASS_TEXT
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            passwordEditText.inputType = inputType
            repeatPasswordEditText.inputType = inputType
            togglePasswordVisibility.setImageResource(
                if (isPasswordVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility_off
            )
            passwordEditText.setSelection(passwordEditText.text.length)
            repeatPasswordEditText.setSelection(repeatPasswordEditText.text.length)
        }

        // Acción del botón registrar
        registerButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val phone = phoneEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val repeatPassword = repeatPasswordEditText.text.toString()

            if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty() || repeatPassword.isEmpty()) {
                Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
            } else if (password != repeatPassword) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            } else {
                val userRequest = UserRequest(
                    codigoPersona = UUID.randomUUID().toString(), // Ajusta si necesitas generarlo
                    email = email,
                    avatarImagen = "-",
                    status = "1",
                    role = "3",
                    password = password,
                    numero = phone,
                    tokenFirebase = "-",
                    lineaTelefonica = phone,
                    username = name,
                    name = name
                )
                viewModel.registrarUsuario(userRequest)
            }
        }

        // Acción para ir a LoginActivity
        loginLinkTextView.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }
}
