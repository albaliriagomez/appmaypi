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
import com.torrezpillcokevin.nuna.data.ApiService


import com.torrezpillcokevin.nuna.data.RetrofitInstance
import kotlinx.coroutines.withContext

class RegistroActivity : AppCompatActivity() {

    private var isPasswordVisible = false // Variable para la visibilidad de la contraseña
    private lateinit var apiService: ApiService // Importa tu servicio de Retrofit

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_registro)

        //atrasButtonView
        val backButton: ImageButton = findViewById(R.id.backButton2)
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                // Configura flags para evitar acumulación de actividades
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)

        }

        // Inicializa Retrofit (asumiendo que ya tienes una instancia creada en otro lugar)
        apiService = RetrofitInstance.api // Asegúrate de que esta referencia es correcta

            // Referencias a las vistas
        val nameEditText: EditText = findViewById(R.id.nameEditText)
        val numberphone:EditText=findViewById(R.id.phoneEditText)
        val emailEditText: EditText = findViewById(R.id.emailEditText)
        val passwordEditText: EditText = findViewById(R.id.passwordEditText)

        val repeatPasswordEditText: EditText = findViewById(R.id.repeatPasswordEditText)
        val registerButton: Button = findViewById(R.id.registerButton)
        val togglePasswordVisibility: ImageView = findViewById(R.id.togglePasswordVisibility)
        val loginLinkTextView: TextView = findViewById(R.id.loginLinkTextView)

        // Acción para alternar visibilidad de la contraseña
        togglePasswordVisibility.setOnClickListener {
            if (isPasswordVisible) {
                // Enmascarar contraseña
                passwordEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                repeatPasswordEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                togglePasswordVisibility.setImageResource(R.drawable.ic_visibility_off)
            } else {
                // Mostrar contraseña
                passwordEditText.inputType = InputType.TYPE_CLASS_TEXT
                repeatPasswordEditText.inputType = InputType.TYPE_CLASS_TEXT
                togglePasswordVisibility.setImageResource(R.drawable.ic_visibility_off)
            }
            // Mueve el cursor al final del texto
            passwordEditText.setSelection(passwordEditText.text.length)
            isPasswordVisible = !isPasswordVisible
        }

        // Acción del botón de registro
        registerButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val phone = numberphone.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val repeatPassword = repeatPasswordEditText.text.toString()

            if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty() || repeatPassword.isEmpty()) {
                Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT)
                    .show()
            } else if (password != repeatPassword) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            } else {
                registerUser(name, phone, email, password)
            }
        }

        // Acción del enlace "¿Ya tienes cuenta? Inicia sesión"
        loginLinkTextView.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left) // Animaciones personalizadas
        }
    }

    // Método para registrar usuario usando la instancia de Retrofit ya creada
    private fun registerUser(name: String, phone: String, email: String, password: String) {
        // Asegúrate de agregar un valor para "avatar" y "role"
        val user = User(
            name = name,
            password = password,
            email = email,
            avatar = "", // O algún valor predeterminado si es requerido
            status = "active",
            role = "user", // Cambia esto según el rol adecuado
            numero = phone.toInt() // Convertir teléfono a número
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.postUsers(user)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(applicationContext, "Registro exitoso", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(applicationContext, LoginActivity::class.java))
                    } else {
                        // Imprime el error para ver más detalles
                        val errorResponse = response.errorBody()?.string() ?: "Error desconocido"
                        //Log.e("Register", "Error: $errorResponse")
                        Toast.makeText(applicationContext, "Error en el registro: $errorResponse", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

}
