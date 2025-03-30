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
import com.google.gson.Gson
import com.torrezpillcokevin.nuna.data.ApiService
import com.torrezpillcokevin.nuna.data.Login
import com.torrezpillcokevin.nuna.data.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

        val usernameEditText: EditText = findViewById(R.id.usernameEditText)
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
        /*loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
            } else {
                val loginData = Login(username, password)
                inicioSesion(loginData)
            }
        }*/

        loginButton.setOnClickListener {
            val email = usernameEditText.text.toString()
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
            //Log.d("LOGIN_REQUEST", "Username: ${login.username}, Password: ${login.password}")  // Verifica los datos enviados
            //Log.d("LOGIN_REQUEST", "URL: ${RetrofitInstance.api}")  // Endpoint usado
           // Log.d("LOGIN_REQUEST", "Content-Type: application/x-www-form-urlencoded")  // Tipo de contenido correcto

            try {
                // Enviar la solicitud POST con Retrofit, ya no es necesario el JSON
                val response = RetrofitInstance.api.postLogin(login.username, login.password)

                Log.d("API_RESPONSE", "Código de estado de la respuesta: ${response.code()}")

                if (response.isSuccessful) {
                    val authResponse = response.body()

                    if (authResponse != null) {
                        Log.d("API_RESPONSE LOGIN", "Token recibido: ${authResponse.access_token}")
                        Log.d("API_RESPONSE LOGIN", "User ID: ${authResponse.user_id}")
                        Log.d("API_RESPONSE LOGIN", "Email: ${authResponse.email}")

                       // Accede a SharedPreferences
                        val sharedPreferences = getSharedPreferences("APP_PREFS", MODE_PRIVATE)

                      // Guardar los datos de forma persistente
                        sharedPreferences.edit().apply {
                            putString("JWT_TOKEN", authResponse.access_token) // Guardar el token
                            putInt("USER_ID", authResponse.user_id) // Guardar el user_id
                            putString("EMAIL", authResponse.email) // Guardar el email
                            apply() // Se aplica los cambios de forma asíncrona
                        }

                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@LoginActivity, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@LoginActivity, MainActivity2::class.java)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        Log.e("API_RESPONSE", "Respuesta nula del servidor.")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@LoginActivity, "Error: respuesta nula", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = errorBody ?: "Error desconocido"

                    Log.e("API_RESPONSE", "Error en la solicitud: $errorMessage")
                    Log.e("API_RESPONSE", "Headers: ${response.headers()}")

                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@LoginActivity, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("API_RESPONSE", "Excepción: ${e.message}")

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }





}