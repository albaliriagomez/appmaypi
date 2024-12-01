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

import android.text.InputType
import android.widget.*


import com.torrezpillcokevin.nuna.data.RetrofitInstance

class RegistroActivity : AppCompatActivity() {

    private var isPasswordVisible = false // Variable para la visibilidad de la contraseña

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        // Referencias a las vistas
        val nameEditText: EditText = findViewById(R.id.nameEditText)
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
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val repeatPassword = repeatPasswordEditText.text.toString()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || repeatPassword.isEmpty()) {
                Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT)
                    .show()
            } else if (password != repeatPassword) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                // Opcional: Puedes redirigir al usuario al Login después del registro
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }

        // Acción del enlace "¿Ya tienes cuenta? Inicia sesión"
        loginLinkTextView.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}
