package com.torrezpillcokevin.nuna.clases;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class BackgroundButtonService extends Service {
    private static final String TAG = "BackgroundService";

    private final BroadcastReceiver buttonReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) return;

            Log.d(TAG, "Evento recibido: " + intent.getAction());

            switch (intent.getAction()) {
                case Intent.ACTION_SCREEN_ON:
                    showMessage("Pantalla Encendida");
                    break;
                case Intent.ACTION_SCREEN_OFF:
                    showMessage("Pantalla Apagada");
                    break;
                case "android.media.VOLUME_CHANGED_ACTION":
                    showMessage("Botón de volumen presionado");
                    break;

            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Servicio creado correctamente");
        registerReceivers();
        requestDeviceAdmin();  // ⬅️ Solicita permisos de administrador
        checkBatteryOptimizationPermission();  // ⬅️ Verifica permiso de optimización de batería
        startForegroundService();
    }

    private void registerReceivers() {
        Log.d(TAG, "Registrando BroadcastReceiver...");
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction("android.media.VOLUME_CHANGED_ACTION");
        registerReceiver(buttonReceiver, filter);
        Log.d(TAG, "BroadcastReceiver registrado correctamente");

    }

    @SuppressLint("ForegroundServiceType")
    private void startForegroundService() {
        Log.d(TAG, "Iniciando servicio en primer plano...");

        String channelId = "background_service_channel";
        String channelName = "Background Service";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
            Log.d(TAG, "Canal de notificación creado");
        }

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Servicio en segundo plano")
                .setContentText("Detectando botones de hardware")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        startForeground(1, notification);
        Log.d(TAG, "Servicio en primer plano iniciado con ID 1");
    }

    // 🚀 Evita que el sistema mate el servicio
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Evento detectado: " + message);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(buttonReceiver);
        Log.d(TAG, "Servicio destruido y receptor desregistrado");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // 📌 Solicita permisos de Administrador de Dispositivo
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

    // 📌 Verifica si el permiso de optimización de batería está activo y solicita confirmación al usuario
    private void checkBatteryOptimizationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = getPackageName();
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + packageName));

            if (!Settings.canDrawOverlays(this)) {
                // 🔥 Envía un Broadcast a MainActivity
                Intent broadcastIntent = new Intent("REQUEST_BATTERY_OPTIMIZATION");
                sendBroadcast(broadcastIntent);
            }
        }
    }

}
