package com.olsc.droidgit.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.olsc.droidgit.core.ServerService;
import com.olsc.droidgit.util.Constants;
import com.olsc.droidgit.util.NetworkUtils;

public class ConnectivityChangeBroadcastReceiver extends BroadcastReceiver {
    private final static String TAG = ConnectivityChangeBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(final Context context, Intent intent) {
        final String action = intent.getAction();

        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean autostartOnWifiOn = prefs.getBoolean(
                    Constants.Prefs.AUTO_START_ON_WIFI, 
                    Constants.Prefs.DEFAULT_AUTO_START_ON_WIFI);
            boolean autostopOnWifiOff = prefs.getBoolean(
                    Constants.Prefs.AUTO_STOP_ON_WIFI_OFF, 
                    Constants.Prefs.DEFAULT_AUTO_STOP_ON_WIFI_OFF);

            if (NetworkUtils.isWifiReady(context)) {
                if (autostartOnWifiOn && !NetworkUtils.isServiceRunning(context, ServerService.class)) {
                     Intent serviceIntent = new Intent(context, ServerService.class);
                     if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                         context.startForegroundService(serviceIntent);
                     } else {
                         context.startService(serviceIntent);
                     }
                     Log.i(TAG, "Auto-started Git HTTP Server");
                }
            } else {
                if (autostopOnWifiOff && NetworkUtils.isServiceRunning(context, ServerService.class)) {
                    context.stopService(new Intent(context, ServerService.class));
                    Log.i(TAG, "Auto-stopped Git HTTP Server");
                }
            }
        }
    }
}
