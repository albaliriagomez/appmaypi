package com.torrezpillcokevin.nuna

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
        // ✅ Factory ahora recibe application también
        viewModel = ViewModelProvider(
            this,
            RegistroViewModelFactory(application, apiService)
        )[RegistroViewModel::class.java]

        setupObservers()

        // ✅ Campos separados: nombre, apellido, segundo apellido
        val nombreEdit        = findViewById<EditText>(R.id.nameEditText)
        val apellidoEdit      = findViewById<EditText>(R.id.apellidoEditText)
        val segundoApellidoEdit = findViewById<EditText>(R.id.segundoApellidoEditText)
        val phoneEdit         = findViewById<EditText>(R.id.phoneEditText)
        val emailEdit         = findViewById<EditText>(R.id.emailEditText)
        val passwordEdit      = findViewById<EditText>(R.id.passwordEditText)
        val confirmEdit       = findViewById<EditText>(R.id.repeatPasswordEditText)
        val registerBtn       = findViewById<Button>(R.id.registerButton)

        registerBtn.setOnClickListener {
            val nombre         = nombreEdit.text.toString().trim()
            val apellido       = apellidoEdit.text.toString().trim()
            val segundoApellido = segundoApellidoEdit.text.toString().trim()
            val phone          = phoneEdit.text.toString().trim()
            val email          = emailEdit.text.toString().trim()
            val pass           = passwordEdit.text.toString()
            val confirm        = confirmEdit.text.toString()

            // Validaciones
            if (nombre.isEmpty() || apellido.isEmpty() || phone.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (pass != confirm) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (pass.length < 6) {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Avatar por defecto = logo de la app
            val drawable = ContextCompat.getDrawable(this, R.drawable.ic_logo)
            val bitmap = (drawable as BitmapDrawable).bitmap
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
            val avatarBytes = stream.toByteArray()

            viewModel.registrarUsuario(
                nombre         = nombre,
                apellido       = apellido,
                segundoApellido = segundoApellido.ifEmpty { " " },
                email          = email,
                phone          = phone,
                pass           = pass,
                avatarBytes    = avatarBytes
            )
        }

        findViewById<ImageButton>(R.id.backButton2).setOnClickListener { finish() }
        findViewById<TextView>(R.id.loginLinkTextView).setOnClickListener { finish() }
    }

    private fun setupObservers() {
        viewModel.registroEstado.observe(this) { resultado ->
            when (resultado) {
                is RegistroViewModel.ResultadoRegistro.Cargando -> {
                    Toast.makeText(this, "Registrando...", Toast.LENGTH_SHORT).show()
                }
                is RegistroViewModel.ResultadoRegistro.Exito -> {
                    Toast.makeText(this, resultado.mensaje, Toast.LENGTH_SHORT).show()
                    finish()
                }
                is RegistroViewModel.ResultadoRegistro.Error -> {
                    Toast.makeText(this, "Error: ${resultado.error}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}