package com.torrezpillcokevin.nuna

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.torrezpillcokevin.nuna.data.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import com.torrezpillcokevin.nuna.data.RetrofitInstance

class RegistroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro)

        // Referencias a los campos y el botón
        val nombreEditText = findViewById<EditText>(R.id.nombre)
        val apellidoEditText = findViewById<EditText>(R.id.apellido)
        val correoEditText = findViewById<EditText>(R.id.correo)
        val contrasenaEditText = findViewById<EditText>(R.id.contrasena)
        val btnRegistrar = findViewById<Button>(R.id.btnRegistrar)

        // Acción al presionar el botón de registro
        btnRegistrar.setOnClickListener {
            val nombre = nombreEditText.text.toString()
            val apellido = apellidoEditText.text.toString()
            val correo = correoEditText.text.toString()
            val contrasena = contrasenaEditText.text.toString()

            // Valida los campos (opcionalmente)
            if (nombre.isEmpty() || apellido.isEmpty() || correo.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Crear el objeto User y enviarlo a través de la API
            val newUser = User(
                name = nombre+" "+ apellido,
                password = contrasena,
                email = correo,
                avatar = "avatar_url", // Ajusta esto según tus requisitos
                status = "activo",
                role = "usuario",

            )

            // Llamar a la función de registro
            registerUser(newUser)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Función para registrar un usuario
    private fun registerUser(user: User) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Usa RetrofitInstance para acceder a ApiService
                val response = RetrofitInstance.api.postUsers(user)
                if (response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this@RegistroActivity, "Usuario registrado exitosamente", Toast.LENGTH_SHORT).show()
                        // Intent para navegar a la nueva actividad
                        val intent = Intent(this@RegistroActivity,LoginActivity::class.java)
                        startActivity(intent)
                        finish() // Opcional: Llama a finish() si no quieres volver a esta actividad
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@RegistroActivity, "Error al registrar usuario: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@RegistroActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
