package com.torrezpillcokevin.nuna

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.torrezpillcokevin.nuna.data.RetrofitInstance
import java.io.ByteArrayOutputStream

class RegistroActivity : AppCompatActivity() {

    private lateinit var viewModel: RegistroViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_registro)

        val apiService = RetrofitInstance.api
        viewModel = ViewModelProvider(this, RegistroViewModelFactory(apiService))[RegistroViewModel::class.java]

        setupObservers()

        val nameEdit = findViewById<EditText>(R.id.nameEditText)
        val phoneEdit = findViewById<EditText>(R.id.phoneEditText)
        val emailEdit = findViewById<EditText>(R.id.emailEditText)
        val passwordEdit = findViewById<EditText>(R.id.passwordEditText)
        val confirmEdit = findViewById<EditText>(R.id.repeatPasswordEditText)
        val registerBtn = findViewById<Button>(R.id.registerButton)

        registerBtn.setOnClickListener {
            val name = nameEdit.text.toString().trim()
            val phone = phoneEdit.text.toString().trim()
            val email = emailEdit.text.toString().trim()
            val pass = passwordEdit.text.toString()
            val confirm = confirmEdit.text.toString()

            if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass != confirm) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Convertir ic_logo a ByteArray
            val drawable = ContextCompat.getDrawable(this, R.drawable.ic_logo)
            val bitmap = (drawable as BitmapDrawable).bitmap
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val avatarBytes = stream.toByteArray()

            viewModel.registrarUsuario(name, email, phone, pass, avatarBytes)
        }

        findViewById<ImageButton>(R.id.backButton2).setOnClickListener { finish() }
    }

    private fun setupObservers() {
        viewModel.registroEstado.observe(this) { resultado ->
            when (resultado) {
                is RegistroViewModel.ResultadoRegistro.Cargando -> {
                    // Aquí puedes mostrar un ProgressBar si tienes uno
                }
                is RegistroViewModel.ResultadoRegistro.Exito -> {
                    Toast.makeText(this, resultado.mensaje, Toast.LENGTH_SHORT).show()
                    finish()
                }
                is RegistroViewModel.ResultadoRegistro.Error -> {
                    Toast.makeText(this, resultado.error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}