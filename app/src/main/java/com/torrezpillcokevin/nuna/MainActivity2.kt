package com.torrezpillcokevin.nuna

import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.WindowManager
import androidx.annotation.RequiresApi
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.torrezpillcokevin.nuna.databinding.ActivityMain2Binding

class MainActivity2 : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMain2Binding

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hacer la barra de estado transparente
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)


        setSupportActionBar(binding.appBarMain.toolbar)


        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.nav_areaeducativa,
                R.id.nav_mapa,
                R.id.nav_recomend,
                R.id.nav_opciones,
                R.id.nav_chatbot,
                R.id.nav_preguntasfrecuentes,
                R.id.nav_guia,
                R.id.nav_soporte,
                R.id.nav_reportar,
                R.id.nav_contactoEmergencia,
                R.id.nav_codigoRastreo
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_activity2, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
    //1234

}