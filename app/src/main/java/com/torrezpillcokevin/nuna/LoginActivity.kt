package com.torrezpillcokevin.nuna

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputEditText
import com.torrezpillcokevin.nuna.data.Login
import com.torrezpillcokevin.nuna.data.RetrofitInstance

class LoginActivity : AppCompatActivity() {

    private lateinit var viewModel: LoginViewModel

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val apiService = RetrofitInstance.api
        viewModel = ViewModelProvider(this, LoginViewModelFactory(application, apiService))[LoginViewModel::class.java]

        if (viewModel.isUserLoggedIn()) {
            IrAMain()
            return
        }

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_login)

        val usernameEditText = findViewById<TextInputEditText>(R.id.usernameEditText)
        val passwordEditText = findViewById<TextInputEditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerLink = findViewById<TextView>(R.id.registerLinkTextView)

        // Ahora sí lo encontrará porque lo pusimos en el XML arriba
        val progressBar = findViewById<ProgressBar>(R.id.loginProgressBar)

        viewModel.loginState.observe(this) { result ->
            when (result) {
                is LoginViewModel.ResultadoLogin.Cargando -> {
                    progressBar?.visibility = View.VISIBLE
                    loginButton.isEnabled = false
                }
                is LoginViewModel.ResultadoLogin.Exito -> {
                    progressBar?.visibility = View.GONE
                    loginButton.isEnabled = true
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                    IrAMain()
                }
                is LoginViewModel.ResultadoLogin.Error -> {
                    progressBar?.visibility = View.GONE
                    loginButton.isEnabled = true
                    Toast.makeText(this, result.error, Toast.LENGTH_LONG).show()
                }
            }
        }

        loginButton.setOnClickListener {
            val phone = usernameEditText.text.toString().trim()
            val pass = passwordEditText.text.toString().trim()

            if (phone.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Ingresa celular y contraseña", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.login(Login(phone, pass))
            }
        }

        registerLink.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        findViewById<ImageButton>(R.id.backButton).setOnClickListener { finish() }
    }

    private fun IrAMain() {
        val intent = Intent(this, MainActivity2::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}