package com.torrezpillcokevin.nuna

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.telephony.SmsManager
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.os.Handler // Agrega esta línea al inicio de tu archivo
import android.widget.*

// Estructura de datos para Contactos
data class Contact(val name: String, val phone: String, val line: String)

class MainActivity : AppCompatActivity() {

    private val REQUEST_CALL_PERMISSION = 1
    private val REQUEST_RECORD_AUDIO_PERMISSION = 2
    private val phoneNumber = "76446793"
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var audioFilePath: String? = null
    private var isRecording = false
    private var isProcessing = false

    private lateinit var camaraIcono: ImageView
    private lateinit var messageIcono: ImageView
    private lateinit var imageViewCaptura: ImageView
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_SEND_SMS = 2
    private val REQUEST_STORAGE_PERMISSION = 3
    private val predefinedNumber = "59176446793"
    private val messageText = "Ayuda!!!"
    private lateinit var photoUri: Uri


    private lateinit var nameEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var lineEditText: EditText
    private lateinit var addContactButton: Button
    private lateinit var contactListView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private val contactList = mutableListOf<Contact>()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val loginButton: Button = findViewById(R.id.loginButton)
        val registroButton: Button = findViewById(R.id.registerButton)



        messageIcono = findViewById(R.id.messageIcono)
        camaraIcono = findViewById(R.id.camaraIcono)
        imageViewCaptura = findViewById(R.id.imageViewCaptura)

        camaraIcono.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                abrirCamara()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_IMAGE_CAPTURE)
            }
        }

        messageIcono.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                enviarSMS(predefinedNumber, messageText)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), REQUEST_SEND_SMS)
            }

            enviarMensajeWhatsApp()
            enviarMensajeTelegram()
        }


        val redButton = findViewById<ImageView>(R.id.redButton)

        redButton.setOnClickListener {
            if (isProcessing) {
                Toast.makeText(this, "Acciones ya en curso, por favor espera.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            var isProcessing = true

            // Ejecutar la grabación
            if (checkAudioPermissions()) {
                startRecording()
            } else {
                requestAudioPermissions()
                isProcessing = false
                return@setOnClickListener
            }

            // Enviar el SMS
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                enviarSMS(predefinedNumber, messageText)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), REQUEST_SEND_SMS)
                isProcessing = false
                return@setOnClickListener
            }

            enviarMensajeWhatsApp()

            enviarMensajeTelegram()

            makePhoneCall()

            abrirCamara()

            // Al finalizar todas las acciones, restablece la bandera
            Toast.makeText(this, "Acciones de emergencia ejecutadas", Toast.LENGTH_SHORT).show()
            isProcessing = false // Restablece la bandera al final
        }



        findViewById<ImageView>(R.id.phoneIcono).setOnClickListener {
            makePhoneCall()
        }

        findViewById<ImageView>(R.id.microphoneIcono).setOnClickListener {
            if (isRecording) {
                stopRecordingAndPlay()
            } else {
                if (checkAudioPermissions()) {
                    startRecording()
                } else {
                    requestAudioPermissions()
                }
            }
        }

        // Inicialización de vistas
        addContactButton = findViewById(R.id.addContactButton)
        contactListView = findViewById(R.id.contactListView)

        // Configurar el adaptador para mostrar los contactos en la lista
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        contactListView.adapter = adapter

        // Acción para agregar un contacto
        addContactButton.setOnClickListener {
            showAddContactDialog()

        }

        // Manejar clics en los elementos de la lista para editar o eliminar
        contactListView.setOnItemClickListener { _, _, position, _ ->
            val contact = contactList[position]
            showEditDeleteDialog(contact, position)
        }


        loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        registroButton.setOnClickListener {
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
        }



    }

    private fun abrirCamara() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_STORAGE_PERMISSION)
        } else {
            val photoFile = try {
                createImageFile()
            } catch (ex: IOException) {
                Toast.makeText(this, "Error al crear archivo de imagen", Toast.LENGTH_SHORT).show()
                null
            }

            photoFile?.also {
                photoUri = FileProvider.getUriForFile(this, "${packageName}.provider", it)
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                if (intent.resolveActivity(packageManager) != null) {
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
                } else {
                    Toast.makeText(this, "No se pudo abrir la cámara", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            // Mostrar la imagen capturada en el ImageView
            imageViewCaptura.setImageURI(photoUri)
        }
    }

    // Crear un archivo para guardar la imagen
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(null)
        return File.createTempFile("IMG_${timeStamp}_", ".jpg", storageDir)
    }

    // Permisos
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CALL_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    makePhoneCall()  // Realizar la llamada si se ha concedido el permiso
                } else {
                    Toast.makeText(this, "Permiso de llamada denegado", Toast.LENGTH_SHORT).show()
                }
            }

            REQUEST_IMAGE_CAPTURE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    abrirCamara()
                } else {
                    Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
                }
            }

            REQUEST_RECORD_AUDIO_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startRecording()  // Comenzar la grabación si se ha concedido el permiso
                } else {
                    Toast.makeText(this, "Permiso de grabación denegado", Toast.LENGTH_SHORT).show()
                }
            }

            REQUEST_SEND_SMS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enviarSMS(predefinedNumber, messageText)
                } else {
                    Toast.makeText(this, "Permiso de SMS denegado", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_STORAGE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    abrirCamara()
                } else {
                    Toast.makeText(this, "Permiso de almacenamiento denegado", Toast.LENGTH_SHORT).show()
                }
            }




        }
    }


    private fun enviarSMS(numero: String, mensaje: String) {
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(numero, null, mensaje, null, null)
            Toast.makeText(this, "Mensaje enviado automáticamente", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al enviar el mensaje", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }


    private fun enviarMensajeWhatsApp() {
        val whatsappUri = Uri.parse("https://api.whatsapp.com/send?phone=$predefinedNumber&text=${Uri.encode(messageText)}")
        val intent = Intent(Intent.ACTION_VIEW, whatsappUri)
        intent.setPackage("com.whatsapp")
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "WhatsApp no está instalado", Toast.LENGTH_SHORT).show()
        }
    }


    private fun enviarMensajeTelegram() {
        val telegramUri = Uri.parse("https://telegram.me/share/url?url=$messageText")
        val intent = Intent(Intent.ACTION_VIEW, telegramUri)
        intent.setPackage("org.telegram.messenger")
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Telegram no está instalado", Toast.LENGTH_SHORT).show()
        }
    }


    private fun makePhoneCall() {
        // Verificar si el permiso de llamada ha sido concedido
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            val callIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber"))
            startActivity(callIntent)
        } else {
            // Solicitar el permiso si no ha sido concedido
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), REQUEST_CALL_PERMISSION)
        }
    }

    private fun checkAudioPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestAudioPermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO_PERMISSION)
    }

    private fun startRecording() {
        audioFilePath = createTempFile("temp_audio", ".3gp", cacheDir).absolutePath
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(audioFilePath)
            prepare()
            start()
        }
        isRecording = true
        Toast.makeText(this, "Grabación iniciada", Toast.LENGTH_SHORT).show()

        // Crear un Handler para detener la grabación después de 5 segundos
        val handler = Handler()
        handler.postDelayed({
            if (isRecording) {
                stopRecordingAndPlay() // Detener la grabación y reproducir
            }
        }, 5000) // 5000 milisegundos = 5 segundos
    }


    private fun stopRecordingAndPlay() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        isRecording = false
        Toast.makeText(this, "Grabación finalizada", Toast.LENGTH_SHORT).show()

        // Reproducir el audio grabado
        mediaPlayer = MediaPlayer().apply {
            setDataSource(audioFilePath)
            prepare()
            start()
        }
        mediaPlayer?.setOnCompletionListener {
            Toast.makeText(this, "Reproducción finalizada", Toast.LENGTH_SHORT).show()
            it.release()
            mediaPlayer = null
        }
    }

    // Método para limpiar los campos del formulario
    private fun clearFields() {
        nameEditText.text.clear()
        phoneEditText.text.clear()
        lineEditText.text.clear()
    }

    // Mostrar un diálogo para editar o eliminar un contacto
    private fun showEditDeleteDialog(contact: Contact, position: Int) {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_delete, null)
        builder.setView(dialogView)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Hace que el fondo sea más elegante

        val contactName = dialogView.findViewById<TextView>(R.id.tvContactName)
        val btnEdit = dialogView.findViewById<ImageView>(R.id.btnEdit)
        val btnDelete = dialogView.findViewById<ImageView>(R.id.btnDelete)
        val btnCancel = dialogView.findViewById<ImageView>(R.id.btnCancel)

        contactName.text = "Opciones para: ${contact.name}" // Muestra el nombre del contacto

        btnEdit.setOnClickListener {
            showEditDialog(contact, position)
            dialog.dismiss()
        }

        btnDelete.setOnClickListener {
            contactList.removeAt(position) // Elimina el contacto de la lista de datos
            adapter.clear() // Limpia el adaptador
            adapter.addAll(contactList.map { "${it.name} | ${it.phone} | ${it.line}" }) // Vuelve a agregar todos los elementos
            adapter.notifyDataSetChanged() // Notifica los cambios
            Toast.makeText(this, "Contacto eliminado", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


    // Mostrar un diálogo para editar un contacto existente
    private fun showEditDialog(contact: Contact, position: Int) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_contact, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        val editName = dialogView.findViewById<EditText>(R.id.nameEditText)
        val editPhone = dialogView.findViewById<EditText>(R.id.phoneEditText)
        val lineSpinner = dialogView.findViewById<Spinner>(R.id.lineSpinner)
        val btnEdit = dialogView.findViewById<Button>(R.id.btnEdit)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

        // Prellenar los campos con la información actual del contacto
        editName.setText(contact.name)
        editPhone.setText(contact.phone)

        // Configurar Spinner con opciones (Entel, Tigo, Viva)
        val lines = listOf("Seleccione línea", "Entel", "Tigo", "Viva")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, lines)
        lineSpinner.adapter = spinnerAdapter

        // Seleccionar la línea actual del contacto en el Spinner
        val selectedIndex = lines.indexOf(contact.line)
        if (selectedIndex >= 0) {
            lineSpinner.setSelection(selectedIndex)
        }

        // Acción del botón Guardar
        btnEdit.setOnClickListener {
            val selectedLine = lineSpinner.selectedItem.toString()
            if (selectedLine != "Seleccione línea") {
                // Actualizar contacto en la lista
                contactList[position] = Contact(
                    editName.text.toString(),
                    editPhone.text.toString(),
                    selectedLine
                )

                // Se notifica al adaptador sobre el cambio
                adapter.clear()
                adapter.addAll(contactList.map { "${it.name} | ${it.phone} | ${it.line}" })
                adapter.notifyDataSetChanged()

                Toast.makeText(this, "Contacto actualizado", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Debe seleccionar una línea válida", Toast.LENGTH_SHORT).show()
            }
        }

        // Acción del botón Cancelar
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }



    private fun showAddContactDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_contact, null)

        val nameEditText = dialogView.findViewById<EditText>(R.id.nameEditText)
        val phoneEditText = dialogView.findViewById<EditText>(R.id.phoneEditText)
        val lineSpinner = dialogView.findViewById<Spinner>(R.id.lineSpinner)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

        // Lista con la opción inicial "Seleccione línea"
        val operators = arrayOf("Seleccione línea", "Entel", "Tigo", "Viva")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, operators)
        lineSpinner.adapter = adapter

        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnSave.setOnClickListener {
            val name = nameEditText.text.toString()
            val phone = phoneEditText.text.toString()
            val line = lineSpinner.selectedItem.toString()

            if (name.isNotEmpty() && phone.isNotEmpty() && line != "Seleccione línea") {
                addContact(Contact(name, phone, line))
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos y selecciona una línea", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }



    // Función para añadir un contacto a la lista
    private fun addContact(contact: Contact) {
        contactList.add(contact)
        adapter.add("${contact.name} | ${contact.phone} | ${contact.line}")
        adapter.notifyDataSetChanged()
    }



}