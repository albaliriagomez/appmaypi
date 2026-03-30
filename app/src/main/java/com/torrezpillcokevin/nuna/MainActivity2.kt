package com.torrezpillcokevin.nuna

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.NavigationUI
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.torrezpillcokevin.nuna.databinding.ActivityMain2Binding

class MainActivity2 : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMain2Binding

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.nav_chatbot,
                R.id.nav_preguntasfrecuentes,
                R.id.nav_guia,
                R.id.nav_soporte,
                R.id.nav_contactoEmergencia,
                R.id.nav_muro,
                R.id.nav_DesaparecidoFragment,
                R.id.nav_evaluacion,
                R.id.nav_red_juridica,
                R.id.nav_politica,
                R.id.nav_terminos
            ),
            drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)

        // ✅ Resalta el item activo en el drawer al navegar
        navController.addOnDestinationChangedListener { _, destination, _ ->
            navView.setCheckedItem(destination.id)
        }

        // ✅ UN SOLO listener que maneja TODO
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_cerrarsesion -> {
                    cerrarSesion()
                }
                R.id.nav_evaluacion -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    startActivity(Intent(this, EvaluacionActivity::class.java))
                }
                R.id.nav_red_juridica -> {           // ← NUEVO (antes era nav_politica)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    startActivity(Intent(this, PoliticaActivity::class.java))
                }
                else -> {
                    try {
                        val handled = NavigationUI.onNavDestinationSelected(menuItem, navController)
                        if (!handled) navController.navigate(menuItem.itemId)
                    } catch (e: Exception) {
                        Log.e("NAV_ERROR", "No se pudo navegar a ${menuItem.itemId}", e)
                    }
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
            }
            true
        }
    }

    private fun cerrarSesion() {
        try {
            // Intento 1: limpiar EncryptedSharedPreferences
            val masterKey = MasterKey.Builder(this)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val sharedPreferences = EncryptedSharedPreferences.create(
                this,
                "SECURE_APP_PREFS",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            sharedPreferences.edit().clear().apply()

        } catch (e: Exception) {
            Log.e("CERRAR_SESION", "Error limpiando EncryptedSharedPreferences: ${e.message}", e)
            // Fallback: borrar el archivo de preferencias directamente
            try {
                getSharedPreferences("SECURE_APP_PREFS", MODE_PRIVATE)
                    .edit().clear().apply()
            } catch (e2: Exception) {
                Log.e("CERRAR_SESION", "Error en fallback: ${e2.message}", e2)
            }
        } finally {
            // Siempre redirigir al login, sin importar si limpió bien o no
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_activity2, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}