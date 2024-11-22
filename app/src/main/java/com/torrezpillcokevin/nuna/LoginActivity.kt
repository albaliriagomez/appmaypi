package com.torrezpillcokevin.nuna

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.torrezpillcokevin.nuna.data.RetrofitInstance
import com.torrezpillcokevin.nuna.data.login
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        val buttonSignIn = findViewById<Button>(R.id.signInButton)
        val buttonSignUp = findViewById<Button>(R.id.signUpButton)
        //inputs
        val correoEditText = findViewById<EditText>(R.id.correo)
        val contasenaEditText = findViewById<EditText>(R.id.contrasena)

        // Configurar el comportamiento del botón "Iniciar Sesion"
        buttonSignIn.setOnClickListener {
            val corre = correoEditText.text.toString()
            val contra = contasenaEditText.text.toString()

            // Valida los campos (opcionalmente)
            if (corre.isEmpty() || contra.isEmpty()) {
                Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Crear el objeto User y enviarlo a través de la API
            val Login = login(email = corre, password = contra)

            // Llamar a la función 
            inicioSesion(Login)           

        }

        // Configurar el comportamiento del botón "Registrarse"
        buttonSignUp.setOnClickListener {
            // Enviar el perfil a RegistroActivity al seleccionar "Sign Up"
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