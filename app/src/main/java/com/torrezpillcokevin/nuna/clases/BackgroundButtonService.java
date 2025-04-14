package com.torrezpillcokevin.nuna.clases;

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
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.torrezpillcokevin.nuna.MainActivity;
import com.torrezpillcokevin.nuna.R;

public class BackgroundButtonService extends Service {
    private static final String TAG = "BackgroundService";
    private static final String ACTION_VOLUME_CHANGED = "android.media.VOLUME_CHANGED_ACTION";
    private static final String CHANNEL_ID = "background_service_channel";
    private static final String CHANNEL_NAME = "Background Service";

    private static final long VOLUME_LONG_PRESS_THRESHOLD = 3000; // Tiempo en milisegundos para detectar una pulsación larga (2 segundos)

    private long volumeUpPressStartTime = 0;
    private boolean isVolumeUpPressed = false;

    private AudioManager audioManager;
    private int maxVolume;
    private boolean isMaxVolumeDetected = false;
    private boolean isUserPressingVolumeUp = false;// Nueva variable para controlar si el volumen ya está al máximo



    private final BroadcastReceiver buttonReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) return;

            Log.d(TAG, "Evento recibido: " + intent.getAction());

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
        // Solo hacemos algo si el volumen está al máximo
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (currentVolume == maxVolume) {
            if (!isMaxVolumeDetected) {
                isMaxVolumeDetected = true;
                Log.d(TAG, "Volumen al máximo detectado.");
            }

            // Verificar si el botón de volumen está siendo presionado (es decir, detectamos un cambio de volumen)
            if (isVolumeUpPressed()) {
                if (!isUserPressingVolumeUp) {
                    isUserPressingVolumeUp = true;
                    volumeUpPressStartTime = System.currentTimeMillis();
                    Log.d(TAG, "Usuario comenzó a presionar el botón de volumen.");
                }

                // Verificar si el usuario ha mantenido presionado el volumen por más de 2 segundos
                long currentTime = System.currentTimeMillis();
                if (currentTime - volumeUpPressStartTime >= VOLUME_LONG_PRESS_THRESHOLD) {
                    if (!isVolumeUpPressed) {
                        isVolumeUpPressed = true;
                        triggerAlert();
                    }
                }
            } else {
                // Si el volumen ya está al máximo pero el usuario ha dejado de presionar el botón, reiniciar
                if (isUserPressingVolumeUp) {
                    isUserPressingVolumeUp = false;
                    volumeUpPressStartTime = 0;
                    Log.d(TAG, "Usuario dejó de presionar el botón de volumen.");
                }
            }
        } else {
            // Si el volumen no está al máximo, reiniciar el estado
            if (isMaxVolumeDetected) {
                isMaxVolumeDetected = false;
                Log.d(TAG, "Volumen bajado, desactivando detección.");
            }
        }
    }
    private boolean isVolumeUpPressed() {
        // Lógica para detectar si el volumen está siendo subido
        return true; // Asumimos que se está presionando el volumen hacia arriba
    }

    private void triggerAlert() {
        Log.d(TAG, "¡Alerta! Botón de volumen hacia arriba presionado durante más de 2 segundos con volumen al máximo.");
        showMessage("¡Alerta! El botón de volumen se ha presionado durante más de 2 segundos con el volumen al máximo.");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Servicio creado correctamente");
        createNotificationChannelIfNeeded();// Crear el canal de notificación si es necesario
        startForegroundService();// Iniciar el servicio en primer plano rápidamente
        // Inicializa AudioManager
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager == null) {
            Log.e(TAG, "AudioManager no se pudo obtener");
        }

        // Verifica el volumen máximo
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
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

    // Resiliencia: reinicia el servicio si es eliminado
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartServiceIntent = new Intent(getApplicationContext(), BackgroundButtonService.class);
        restartServiceIntent.setPackage(getPackageName());
        startService(restartServiceIntent);
        super.onTaskRemoved(rootIntent);
    }
}
