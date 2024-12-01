package com.torrezpillcokevin.nuna

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.torrezpillcokevin.nuna.data.RetrofitInstance
import com.torrezpillcokevin.nuna.data.login
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private var isPasswordVisible = false // Variable para la visibilidad de la contraseña

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailEditText: EditText = findViewById(R.id.emailEditText)
        val passwordEditText: EditText = findViewById(R.id.passwordEditText)
        val togglePasswordVisibility: ImageView = findViewById(R.id.togglePasswordVisibility)
        val loginButton: Button = findViewById(R.id.loginButton)
        val registerLinkTextView: TextView = findViewById(R.id.registerLinkTextView)

        // Usuario y contraseña por defecto
        val defaultEmail = "erika@gmail.com"
        val defaultPassword = "123456"

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
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
            } else if (email == defaultEmail && password == defaultPassword) {
                Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                // Aquí puedes redirigir al usuario a otra actividad
                // startActivity(Intent(this, MainActivity::class.java))
            } else {
                Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
            }
        }

        // Acción del enlace "¿No tienes cuenta? Regístrese"
        registerLinkTextView.setOnClickListener {
            // Redirige a la pantalla de registro
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
        }
    }


    // Función para iniciar sesion
    private fun inicioSesion(login: login) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.postLogin(login)

                if (response.isSuccessful) {
                    val userData = response.body()

                    // Imprimir todos los datos recibidos en el log
                    Log.d("API_RESPONSE LOGIN", "Nombre: ${userData?.name}")
                    Log.d("API_RESPONSE LOGIN", "Correo: ${userData?.email}")
                    Log.d("API_RESPONSE LOGIN", "Avatar: ${userData?.avatar}")
                    Log.d("API_RESPONSE LOGIN", "Estado: ${userData?.status}")
                    Log.d("API_RESPONSE LOGIN", "Rol: ${userData?.role}")

                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this@LoginActivity, MainActivity2::class.java)
                        startActivity(intent)
                        finish() // Opcional: llama a finish() si no quieres volver a esta actividad
                    }
                } else {
                    Log.e("API_RESPONSE", "Error en la solicitud: ${response.errorBody()?.string()}")

                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, "Error al iniciar sesión: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("API_RESPONSE", "Excepción: ${e.message}")

                runOnUiThread {
                    Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}