package com.torrezpillcokevin.nuna

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Obtiene el botón usando su ID y configura el listener de clic
        val navigateButton: Button = findViewById(R.id.button_navigate)
        navigateButton.setOnClickListener {
            // Inicia la segunda actividad
            val intent = Intent(this, SelectPerfilActivity::class.java)
            startActivity(intent)
        }

        //Kevin Torrez Pillco
        //123456789
        //asdasdasd
        //adasd
        //adasda
        ///adsasdasdas

        //ejnjnaskn
        //kckmsdckm
    }
}