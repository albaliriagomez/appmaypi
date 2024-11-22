package com.torrezpillcokevin.nuna

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SelectPerfilActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_select_perfil)
        // Obtiene el botón usando su ID y configura el listener de clic
        val navigateButton: Button = findViewById(R.id.buttonAdultos)
        navigateButton.setOnClickListener {
            // Inicia la segunda actividad
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        // Obtiene el botón usando su ID y configura el listener de clic
        val navigateButton2: Button = findViewById(R.id.buttonInfantes)
        navigateButton2.setOnClickListener {
            // Inicia la segunda actividad
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}