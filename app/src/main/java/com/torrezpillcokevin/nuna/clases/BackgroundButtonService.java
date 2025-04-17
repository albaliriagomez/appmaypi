package com.torrezpillcokevin.nuna.clases;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.util.Log;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.torrezpillcokevin.nuna.MainActivity;

import com.torrezpillcokevin.nuna.R;

import java.util.Set;

public class BackgroundButtonService extends Service {
    private static final String TAG = "BackgroundService";
    private static final String ACTION_VOLUME_CHANGED = "android.media.VOLUME_CHANGED_ACTION";
    private static final String CHANNEL_ID = "background_service_channel";
    private static final String CHANNEL_NAME = "Background Service";

    private static final long VOLUME_LONG_PRESS_THRESHOLD = 3000; // Tiempo en milisegundos para detectar una pulsación larga (2 segundos)

    private AudioManager audioManager;
    private long volumeDownPressStartTime = 0;
    private boolean isVolumeDownPressed = false;
    private boolean isUserPressingVolumeDown = false;
    private boolean isMinVolumeDetected = false;
    private SharedPreferences sharedPreferences;

    //private static final int NOTIFICATION_ID = 102;


    private final BroadcastReceiver buttonReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) return;

            //Log.d(TAG, "Evento recibido: " + intent.getAction());

            switch (intent.getAction()) {
                case ACTION_VOLUME_CHANGED:
                    handleVolumeButtonPress();
                    break;
                case Intent.ACTION_SCREEN_ON:
                    showMessage("Pantalla Encendida");
                    break;
                case Intent.ACTION_SCREEN_OFF:
                    showMessage("Pantalla Apagada");
                    break;
            }

        }

    };

    private void handleVolumeButtonPress() {
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int minVolume = 0; // Volumen mínimo (silenciado)

        if (currentVolume == minVolume) {
            if (!isMinVolumeDetected) {
                isMinVolumeDetected = true;
                Log.d(TAG, "Volumen al mínimo detectado.");
            }

            if (isVolumeDownPressed()) {
                if (!isUserPressingVolumeDown) {
                    isUserPressingVolumeDown = true;
                    volumeDownPressStartTime = System.currentTimeMillis();
                    Log.d(TAG, "Usuario comenzó a presionar el botón de volumen hacia abajo.");
                    Alert();
                }

                long currentTime = System.currentTimeMillis();
                if (currentTime - volumeDownPressStartTime >= VOLUME_LONG_PRESS_THRESHOLD) {
                    if (!isVolumeDownPressed) {
                        isVolumeDownPressed = true;
                    }
                }
            } else {
                if (isUserPressingVolumeDown) {
                    isUserPressingVolumeDown = false;
                    volumeDownPressStartTime = 0;
                    Log.d(TAG, "Usuario dejó de presionar el botón de volumen hacia abajo.");
                }
            }
        } else {
            if (isMinVolumeDetected) {
                isMinVolumeDetected = false;
                isVolumeDownPressed = false;
                isUserPressingVolumeDown = false;
                volumeDownPressStartTime = 0;
                Log.d(TAG, "Volumen aumentado, desactivando detección.");
            }
        }
    }

    private boolean isVolumeDownPressed() {
        // Implementación básica - en producción necesitarías una solución más robusta
        return true;
    }

    private void Alert() {
        Log.d(TAG, "Alarma Activada");

        // String savedCallContact = sharedPreferences.getString("emergency_call_phone", null);
        Set<String> savedSMSContacts = sharedPreferences.getStringSet("emergency_sms_phones", null);
        //Log.d(TAG, "Número de llamada de emergencia guardado: " + savedCallContact);
        Log.d(TAG, "Número de SMS de emergencia guardado: " + savedSMSContacts);
        showEmergencyNotification();

        /*try {
            // Enviar SMS a los contactos configurados
            if (savedSMSContacts != null && !savedSMSContacts.isEmpty()) {
                sendSmsToContacts(savedSMSContacts);
                Log.d(TAG, "SMS Enviados a tus contactos");
                showEmergencyNotification();
            } else {
                Log.d(TAG, "No hay contactos configurados para enviar SMS");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error inesperado al activar la alerta", e);
        }*/
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Servicio creado correctamente");
        // Inicializa las nuevas dependencias
        sharedPreferences = getSharedPreferences("EmergencyPrefs", Context.MODE_PRIVATE);
        createNotificationChannelIfNeeded();// Crear el canal de notificación si es necesario
        startForegroundService();// Iniciar el servicio en primer plano rápidamente
        // Inicializa AudioManager
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager == null) {
            Log.e(TAG, "AudioManager no se pudo obtener");
        }
        registerReceivers();// Registrar BroadcastReceiver
        requestDeviceAdmin();// Solicitar permisos de administrador de dispositivo
    }


    private void registerReceivers() {
        Log.d(TAG, "Registrando BroadcastReceiver...");
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_VOLUME_CHANGED);
        registerReceiver(buttonReceiver, filter);
        Log.d(TAG, "BroadcastReceiver registrado correctamente");
    }

    @SuppressLint("ForegroundServiceType")
    private void startForegroundService() {
        Log.d(TAG, "Iniciando servicio en primer plano...");

        String channelId = CHANNEL_ID;

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Maypi está protegiéndote")
                .setContentText("Presiona para acceder a la app o solicitar ayuda.")
                .setSmallIcon(R.drawable.person)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText("Maypi está activo y listo para ayudarte en caso de emergencia."))
                .build();

        startForeground(1, notification);
        Log.d(TAG, "Servicio en primer plano iniciado con ID 1");
    }

    // Evita que el sistema mate el servicio
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void showMessage(String message) {
        Log.d(TAG, "Evento detectado: " + message);
        // Evita usar Toast en servicios, podrías optar por una notificación ligera si es necesario
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(buttonReceiver);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Receiver no estaba registrado: " + e.getMessage());
        }
        Log.d(TAG, "Servicio destruido y receptor desregistrado");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Solicita permisos de Administrador de Dispositivo
    private void requestDeviceAdmin() {
        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName adminComponent = new ComponentName(this, MyDeviceAdminReceiver.class);

        if (!dpm.isAdminActive(adminComponent)) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Este permiso es necesario para evitar que el servicio se detenga.");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    // Crea el canal de notificación si es necesario
    private void createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_LOW
                );
                manager.createNotificationChannel(channel);
                Log.d(TAG, "Canal de notificación creado");
            }
        }
    }

    private void showEmergencyNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String channelId = CHANNEL_ID;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.sms_24px) // Usa un ícono de alerta
                .setContentTitle("¡Sms Enviados!")
                .setContentText("Maypi ha enviado el SMS a tus contactos")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 300, 200, 300});

        notificationManager.notify(99, builder.build());
    }

    // Resiliencia: reinicia el servicio si es eliminado
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartServiceIntent = new Intent(getApplicationContext(), BackgroundButtonService.class);
        restartServiceIntent.setPackage(getPackageName());
        startService(restartServiceIntent);
        super.onTaskRemoved(rootIntent);
    }
}