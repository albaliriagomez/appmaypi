package com.torrezpillcokevin.nuna.clases;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyDeviceAdminReceiver extends DeviceAdminReceiver {
    private static final String TAG = "DeviceAdminReceiver";

    @Override
    public void onEnabled(Context context, Intent intent) {
        Log.d(TAG, "Administrador de dispositivo activado");
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        Log.d(TAG, "Administrador de dispositivo desactivado");
    }
}
