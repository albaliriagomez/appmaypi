package com.torrezpillcokevin.nuna

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.torrezpillcokevin.nuna.data.ApiService
import com.torrezpillcokevin.nuna.data.Login
import com.torrezpillcokevin.nuna.data.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private var isPasswordVisible = false // Variable para la visibilidad de la contraseña
    private lateinit var apiService: ApiService // Importa tu servicio de Retrofit

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_login)

        apiService = RetrofitInstance.api

        //atras
        val backButton: ImageButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                // Configura flags para evitar acumulación de actividades
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)

        }

        val emailEditText: EditText = findViewById(R.id.emailEditText)
        val passwordEditText: EditText = findViewById(R.id.passwordEditText)
        val togglePasswordVisibility: ImageView = findViewById(R.id.togglePasswordVisibility)
        val loginButton: Button = findViewById(R.id.loginButton)
        val registerLinkTextView: TextView = findViewById(R.id.registerLinkTextView)

        // Usuario y contraseña por defecto
        val defaultEmail = "123"
        val defaultPassword = "123"



        // Acción para alternar visibilidad de la contraseña
        togglePasswordVisibility.setOnClickListener {
            if (isPasswordVisible) {
                // Enmascarar contraseña
                passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                togglePasswordVisibility.setImageResource(R.drawable.ic_visibility_off)
            } else {
                // Mostrar contraseña
                passwordEditText.inputType = InputType.TYPE_CLASS_TEXT
                togglePasswordVisibility.setImageResource(R.drawable.ic_visibility_off)
            }
            // Mueve el cursor al final del texto
            passwordEditText.setSelection(passwordEditText.text.length)
            isPasswordVisible = !isPasswordVisible
        }


        // Acción del botón de inicio de sesión
       /* loginButton.setOnClickListener {
            val username = emailEditText.text.toString()  // Usamos 'username'
            val password = passwordEditText.text.toString()

            Log.d("LOGIN_DATA", "Username: $username, Password: $password")  // Agregar log aquí

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
            } else {
                val loginData = Login(username, password)  // Usamos 'username' aquí
                inicioSesion(loginData)
            }
        }*/
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
            } else if (email == defaultEmail && password == defaultPassword) {
                Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                // Aquí puedes redirigir al usuario a otra actividad
                startActivity(Intent(this, MainActivity2::class.java))
            } else {
                Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
            }
        }





        // Acción del enlace "¿No tienes cuenta? Regístrese"
        registerLinkTextView.setOnClickListener {
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left) // Animaciones personalizadas
        }
    }


    // Función para iniciar sesion
    private fun inicioSesion(login: Login) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("LOGIN_REQUEST", "Enviando solicitud de login: ${login.username}, ${login.password}")  // Log de datos enviados

            try {
                val response = RetrofitInstance.api.postLogin(login)

                Log.d("API_RESPONSE", "Código de estado de la respuesta: ${response.code()}")  // Log del código de estado HTTP

                if (response.isSuccessful) {
                    val authResponse = response.body()

                    if (authResponse != null) {
                        Log.d("API_RESPONSE LOGIN", "Token recibido: ${authResponse.access_token}")  // Log del token

                        val sharedPreferences = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                        sharedPreferences.edit().putString("JWT_TOKEN", authResponse.access_token).apply()

                        runOnUiThread {
                            Toast.makeText(this@LoginActivity, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@LoginActivity, MainActivity2::class.java)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@LoginActivity, "Error: respuesta nula", Toast.LENGTH_SHORT).show()
                        }
                        Log.e("API_RESPONSE", "Respuesta nula del servidor.")
                    }
                } else {
                    Log.e("API_RESPONSE", "Error en la solicitud: ${response.errorBody()?.string()}")  // Log de error si la respuesta no es exitosa

                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, "Error al iniciar sesión: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("API_RESPONSE", "Excepción: ${e.message}")  // Log de la excepción

                runOnUiThread {
                    Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


}