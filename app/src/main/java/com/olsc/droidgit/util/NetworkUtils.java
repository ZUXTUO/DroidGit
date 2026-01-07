package com.olsc.droidgit.util;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.olsc.droidgit.R;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;

/**
 * 网络和系统工具类
 * 提供网络状态检测、IP获取、通知管理等功能
 */
public final class NetworkUtils {

    private static final String TAG = "NetworkUtils";

    private NetworkUtils() {
        // 防止实例化
    }

    /**
     * 检查WiFi是否已连接且可用
     */
    public static boolean isWifiReady(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        // 检查WiFi连接
        if (info != null && info.isConnected()) {
            String ip = getWifiIpAddress(context);
            if (ip != null && !ip.equals("0.0.0.0") && !ip.isEmpty()) {
                return true;
            }
        }

        // 检查热点/以太网/其他接口
        String ip = getIpAddress();
        return ip != null && !ip.isEmpty() && !ip.equals("0.0.0.0");
    }

    /**
     * 检查WiFi是否已连接（不检查IP）
     */
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return info != null && info.isConnected();
    }

    /**
     * 获取当前WiFi的SSID
     */
    public static String getWifiSSID(Context context) {
        WifiManager wifiManager = (WifiManager) context
                .getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) return "";

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo == null) return "";

        String ssid = wifiInfo.getSSID();
        if (ssid == null) return "";

        // 移除引号
        if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
            ssid = ssid.substring(1, ssid.length() - 1);
        }

        // 处理未知SSID（Android 10+需要位置权限）
        if ("<unknown ssid>".equals(ssid)) {
            return context.getString(R.string.unknown_ssid);
        }

        return ssid;
    }

    /**
     * 获取WiFi的IP地址
     */
    public static String getWifiIpAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context
                .getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) return null;

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();

        if (ipAddress != 0) {
            return formatIpAddress(ipAddress);
        }

        // 回退到遍历网络接口
        return getIpAddress();
    }

    /**
     * 获取设备的IP地址（支持WiFi、热点、以太网等）
     */
    public static String getIpAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            String candidateIp = null;

            while (interfaces.hasMoreElements()) {
                NetworkInterface intf = interfaces.nextElement();
                String name = intf.getName();
                Enumeration<InetAddress> addresses = intf.getInetAddresses();

                while (addresses.hasMoreElements()) {
                    InetAddress inetAddress = addresses.nextElement();

                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        String ip = inetAddress.getHostAddress();
                        Log.d(TAG, "Found IP: " + ip + " on interface: " + name);

                        // 优先返回WiFi/热点/以太网接口
                        if (name.startsWith("wlan") || name.startsWith("ap") ||
                                name.startsWith("eth") || name.startsWith("rndis")) {
                            return ip;
                        }

                        // 保存候选IP（如移动数据）
                        candidateIp = ip;
                    }
                }
            }

            return candidateIp;

        } catch (SocketException e) {
            Log.e(TAG, "Error getting IP address", e);
        }

        return null;
    }

    /**
     * 格式化IP地址（从int转换为字符串）
     */
    private static String formatIpAddress(int ipAddress) {
        return String.format("%d.%d.%d.%d",
                (ipAddress & 0xff),
                (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff),
                (ipAddress >> 24 & 0xff));
    }

    /**
     * 生成SHA-1哈希
     */
    public static String generateSha1(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(data.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "SHA-1 algorithm not found", e);
            return "";
        }
    }

    /**
     * 检查Git服务是否正在运行
     * @param context 上下文
     * @param serviceClass 服务类
     */
    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        if (serviceClass.getName().equals(com.olsc.droidgit.core.ServerService.class.getName())) {
            return com.olsc.droidgit.core.ServerService.isRunning();
        }
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager == null) return false;

        for (ActivityManager.RunningServiceInfo service : 
                manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }

        return false;
    }

    /**
     * 创建服务运行通知
     */
    public static Notification createServiceNotification(Context context, int port) {
        // 创建通知渠道
        NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel channel = new NotificationChannel(
                Constants.Notification.CHANNEL_ID,
                Constants.Notification.CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
        );
        nm.createNotificationChannel(channel);

        // 点击通知打开主界面
        Intent notificationIntent = new Intent(context, 
                com.olsc.droidgit.ui.home.MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | 
                Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(
                context, 1, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 重启服务动作
        Intent restartIntent = new Intent(context, 
                com.olsc.droidgit.core.ServerService.class);
        restartIntent.setAction(Constants.Action.RESTART_SERVICE);

        PendingIntent restartPendingIntent = PendingIntent.getService(
                context, 2, restartIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String ipAddress = getWifiIpAddress(context);
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = "0.0.0.0";
        }

        String contentText = "http://" + ipAddress + ":" + port;

        return new NotificationCompat.Builder(context, Constants.Notification.CHANNEL_ID)
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_stat_notification)
                .setContentIntent(contentIntent)
                .setOngoing(true)
                .setWhen(System.currentTimeMillis())
                .addAction(R.drawable.ic_stat_notification,
                        context.getString(R.string.notification_action_restart),
                        restartPendingIntent)
                .build();
    }

    /**
     * 取消服务通知
     */
    public static void cancelServiceNotification(Context context) {
        NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.cancel(Constants.Notification.GIT_SERVER_RUNNING);
        }
    }
}
