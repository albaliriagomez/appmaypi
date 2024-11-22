package com.torrezpillcokevin.nuna.ui.opciones

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.telephony.SmsManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.torrezpillcokevin.nuna.R

class OpcionesFragment : Fragment() {

    private val viewModel: OpcionesViewModel by viewModels()

    //sms
    private val REQUEST_SEND_SMS = 2
    private lateinit var messageIcono: ImageView
    private lateinit var CallIcono: ImageView
    private val messageText = "PRUEBAA!!!"
    //call
    private val REQUEST_CALL_PERMISSION = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_opciones, container, false)

        // Configura el botón
        val guardarButton: Button = view.findViewById(R.id.configurarButton)
        messageIcono = view.findViewById(R.id.messageIcono) // sms botton
        CallIcono = view.findViewById(R.id.phoneIcono)

        messageIcono.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                // Recupera el número de celular desde SharedPreferences
                val numeroCelular = viewModel.getNumeroCelular() // Método para obtener el número
                if (numeroCelular != null) {
                    enviarSMS(numeroCelular, messageText) // Usa el número recuperado
                } else {
                    // Manejo de error si el número no está disponible
                    Toast.makeText(requireContext(), "Número de celular no guardado", Toast.LENGTH_SHORT).show()
                }
            } else {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.SEND_SMS), REQUEST_SEND_SMS)
            }
        }

        //llamada
        CallIcono.setOnClickListener{
            makePhoneCall()
        }


        guardarButton.setOnClickListener {
            showMyDialog()  // Muestra el modal al hacer clic en el botón
        }

        return view
    }

    private fun enviarSMS(numero: String, mensaje: String) {
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(numero, null, mensaje, null, null)
            Toast.makeText(requireActivity(), "Mensaje enviado automáticamente", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireActivity(), "Error al enviar el mensaje", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun makePhoneCall() {
        val numeroCelular = viewModel.getNumeroCelular() // Método para obtener el número
        // Verificar si el permiso de llamada ha sido concedido
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            val callIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$numeroCelular"))
            startActivity(callIntent)
        } else {
            // Solicitar el permiso si no ha sido concedido
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CALL_PHONE), REQUEST_CALL_PERMISSION)
        }
    }




    // Permisos
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CALL_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    makePhoneCall()  // Realizar la llamada si se ha concedido el permiso
                } else {
                    Toast.makeText(requireActivity(), "Permiso de llamada denegado", Toast.LENGTH_SHORT).show()
                }
            }

            /*REQUEST_IMAGE_CAPTURE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    abrirCamara()
                } else {
                    Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
                }
            }*/

           /* REQUEST_RECORD_AUDIO_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startRecording()  // Comenzar la grabación si se ha concedido el permiso
                } else {
                    Toast.makeText(this, "Permiso de grabación denegado", Toast.LENGTH_SHORT).show()
                }
            }*/

            REQUEST_SEND_SMS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Aquí puedes agregar el código que deseas ejecutar si el permiso es concedido
                } else {
                    Toast.makeText(requireActivity(), "Permiso de SMS denegado", Toast.LENGTH_SHORT).show()
                }
            }

            /* REQUEST_STORAGE_PERMISSION -> {
                 if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                     abrirCamara()
                 } else {
                     Toast.makeText(this, "Permiso de almacenamiento denegado", Toast.LENGTH_SHORT).show()
                 }
             }*/
        }
    }

    private fun showMyDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.fragment_configuracion_modal, null)
        builder.setView(dialogView)

        val numeroCelularEditText: EditText = dialogView.findViewById(R.id.numeroCelular)
        val guardarButton: Button = dialogView.findViewById(R.id.GuardarButton)

        // Recuperar el número de celular guardado en SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("mi_prefs", Context.MODE_PRIVATE)
        val numeroCelularGuardado = sharedPreferences.getString("numero_celular", "")

        // Mostrar el número de celular guardado en el EditText
        numeroCelularEditText.setText(numeroCelularGuardado)

        val dialog = builder.create()

        guardarButton.setOnClickListener {
            val numeroCelular = numeroCelularEditText.text.toString()

            // Guardar datos en SharedPreferences directamente desde el Fragmento
            with(sharedPreferences.edit()) {
                putString("numero_celular", numeroCelular)
                apply()
            }

            dialog.dismiss()  // Cerrar el diálogo
        }

        dialog.show()
    }


}


