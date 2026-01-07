package com.olsc.droidgit.core;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.olsc.droidgit.R;
import com.olsc.droidgit.data.database.DatabaseManager;
import com.olsc.droidgit.util.Constants;
import com.olsc.droidgit.util.NetworkUtils;

import java.io.IOException;

/**
 * Git HTTP服务器前台服务
 * 负责启动和管理HTTP Git服务器，确保服务持续运行
 */
public class ServerService extends Service {
    private static final String TAG = ServerService.class.getSimpleName();

    private GitServer gitServer;
    private int httpPort = Constants.Prefs.DEFAULT_HTTP_PORT;
    private static boolean isRunning = false;
    private PowerManager.WakeLock wakeLock;

    public static boolean isRunning() {
        return isRunning;
    }

    public ServerService() {
        Log.i(TAG, "Construct ServerService!");
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "Service onCreate!");
        super.onCreate();

        // 获取WakeLock确保服务不被系统杀死
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DroidGit::GitServerWakeLock");
        wakeLock.acquire();
        isRunning = true;
        Log.i(TAG, "WakeLock acquired");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "Service BIND!");
        return null;
    }

    @Override
    public void onDestroy() {
        try {
            // 停止HTTP Git服务器
            if (gitServer != null) {
                gitServer.stop();
                Log.i(TAG, "HTTP Git Server stopped!");
            }

            // 发送服务停止广播
            Intent intentStop = new Intent(Constants.Action.GIT_SERVER_STOPPED);
            intentStop.setPackage(getPackageName());
            sendBroadcast(intentStop);

            // 取消通知
            NetworkUtils.cancelServiceNotification(this);

        } catch (Exception e) {
            Log.e(TAG, "Problem when stopping server.", e);
        } finally {
            isRunning = false;
            // 释放WakeLock
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
                Log.i(TAG, "WakeLock released");
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 处理重启请求
        if (intent != null && Constants.Action.RESTART_SERVICE.equals(intent.getAction())) {
            Log.i(TAG, "Service restart requested by user from notification");
        }

        // 读取配置
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean showNotification = prefs.getBoolean(
                Constants.Prefs.SHOW_NOTIFICATION,
                Constants.Prefs.DEFAULT_SHOW_NOTIFICATION);

        // 创建通知
        Notification notification = NetworkUtils.createServiceNotification(this, httpPort);

        if (Build.VERSION.SDK_INT >= 34) {
            try {
                int typeSpecialUse = ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE;
                startForeground(1, notification, typeSpecialUse);
                Log.i(TAG, "Foreground service started with SPECIAL_USE type");
            } catch (Exception e) {
                Log.e(TAG, "Failed to start foreground with type, falling back.", e);
                startForeground(1, notification);
            }
        } else {
            startForeground(1, notification);
        }

        if (!showNotification) {
            Log.i(TAG, "User prefers no notification, but foreground service requires it. Relying on system settings.");
        }

        // 读取HTTP端口配置
        try {
            httpPort = Integer.parseInt(prefs.getString(Constants.Prefs.HTTP_PORT, String.valueOf(Constants.Prefs.DEFAULT_HTTP_PORT)));
        } catch (ClassCastException e) {
            // 当意外存储为int类型时的回退方案
            try {
                httpPort = prefs.getInt(Constants.Prefs.HTTP_PORT, Constants.Prefs.DEFAULT_HTTP_PORT);
            } catch (Exception ex) {
                httpPort = Constants.Prefs.DEFAULT_HTTP_PORT;
            }
        } catch (NumberFormatException e) {
            httpPort = Constants.Prefs.DEFAULT_HTTP_PORT;
        }

        // 检查服务器是否已经在运行
        if (gitServer != null && gitServer.isAlive()) {
            Log.i(TAG, "Server already running, ignoring start command.");
            return START_STICKY;
        }

        // 在后台线程启动HTTP Git服务器
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    gitServer = new GitServer(ServerService.this, httpPort);
                    gitServer.start();
                    Log.i(TAG, "HTTP Git Server started on port " + httpPort);

                    // 发送服务启动广播通知
                    Intent broadcast = new Intent(Constants.Action.GIT_SERVER_STARTED);
                    broadcast.setPackage(getPackageName());
                    sendBroadcast(broadcast);

                    // 获取并记录当前网络IP地址
                    String currentIp = NetworkUtils.getIpAddress();
                    if (currentIp == null)
                        currentIp = "0.0.0.0";

                    Log.i(TAG, "Git HTTP Server ready!");
                    Log.i(TAG,
                            "Clone repositories using: git clone http://" + currentIp + ":" + httpPort + "/<repo>.git");

                } catch (IOException e) {
                    Log.e(TAG, "Problem when starting HTTP server.", e);
                    stopSelf();
                }
            }
        }).start();

        return START_STICKY;
    }
}
