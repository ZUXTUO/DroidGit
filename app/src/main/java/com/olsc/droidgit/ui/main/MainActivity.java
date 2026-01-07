package com.olsc.droidgit.ui.main;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.olsc.droidgit.R;
import com.olsc.droidgit.core.ServerService;
import com.olsc.droidgit.util.Constants;
import com.olsc.droidgit.util.NetworkUtils;

/**
 * 主Activity
 */
public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";

    // 用户界面组件
    private Button btnToggleServer;
    private TextView tvServerStatus;
    private TextView tvServerUrl;
    private TextView tvNetworkStatus;
    private android.widget.ImageView imgWireless;

    // 状态
    private boolean isServerRunning = false;

    // 广播接收器
    private final BroadcastReceiver serverStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Constants.Action.GIT_SERVER_STARTED.equals(action)) {
                onServerStarted();
            } else if (Constants.Action.GIT_SERVER_STOPPED.equals(action)) {
                onServerStopped();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_home_dashboard);

        initializeViews();
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.Action.GIT_SERVER_STARTED);
        filter.addAction(Constants.Action.GIT_SERVER_STOPPED);
        registerReceiver(serverStatusReceiver, filter);

        // 更新UI状态
        updateUIState();
    }

    @Override
    protected void onPause() {
        super.onPause();
        
        // 注销广播接收器
        try {
            unregisterReceiver(serverStatusReceiver);
        } catch (IllegalArgumentException e) {
            // 已经注销
        }
    }

    /**
     * 初始化视图
     */
    private void initializeViews() {
        btnToggleServer = findViewById(R.id.homeBtnStartStop);
        tvServerStatus = findViewById(R.id.homeWifiSSID); // 临时/替代使用SSID视图显示状态
        tvServerUrl = findViewById(R.id.homeServerInfoTextView);
        tvNetworkStatus = findViewById(R.id.homeWifiStatus);
        imgWireless = findViewById(R.id.homeWirelessImage);
    }

    /**
     * 设置点击监听器
     */
    private void setupClickListeners() {
        btnToggleServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleServer();
            }
        });
    }

    /**
     * 切换服务器状态
     */
    private void toggleServer() {
        if (isServerRunning) {
            stopServer();
        } else {
            startServer();
        }
    }

    /**
     * 启动服务器
     */
    private void startServer() {
        Log.i(TAG, "Starting HTTP Git Server");
        
        Intent serviceIntent = new Intent(this, ServerService.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    /**
     * 停止服务器
     */
    private void stopServer() {
        Log.i(TAG, "Stopping HTTP Git Server");
        
        Intent serviceIntent = new Intent(this, ServerService.class);
        stopService(serviceIntent);
    }

    /**
     * 服务器启动回调
     */
    private void onServerStarted() {
        Log.i(TAG, "Server started notification received");
        isServerRunning = true;
        updateUIState();
    }

    /**
     * 服务器停止回调
     */
    private void onServerStopped() {
        Log.i(TAG, "Server stopped notification received");
        isServerRunning = false;
        updateUIState();
    }

    /**
     * 更新UI状态
     */
    private void updateUIState() {
        // 检查服务是否正在运行
        isServerRunning = NetworkUtils.isServiceRunning(this, ServerService.class);

        // 更新按钮
        if (isServerRunning) {
            btnToggleServer.setText(R.string.home_stop);
            btnToggleServer.setBackgroundResource(R.drawable.blue_btn_selector);
            tvServerStatus.setText(R.string.notification_title); // "Git HTTP Server is running"
            tvServerStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            btnToggleServer.setText(R.string.home_start);
            btnToggleServer.setBackgroundResource(R.drawable.blue_btn_selector);
            tvServerStatus.setText(R.string.server_stopped);
            tvServerStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }

        // 更新网络状态和服务器URL
        updateNetworkInfo();
    }

    /**
     * 更新网络信息
     */
    private void updateNetworkInfo() {
        boolean wifiReady = NetworkUtils.isWifiReady(this);
        String ipAddress = NetworkUtils.getIpAddress();
        
        if (imgWireless != null) {
            imgWireless.setImageResource(wifiReady ? R.drawable.ic_wireless_enabled : R.drawable.ic_wireless_disabled);
        }

        if (wifiReady && ipAddress != null) {
            tvNetworkStatus.setText(R.string.network_ready);
            tvNetworkStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));

            // 显示服务器URL
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            int httpPort = prefs.getInt(Constants.Prefs.HTTP_PORT, Constants.Prefs.DEFAULT_HTTP_PORT);
            
            String serverUrl = getString(R.string.server_info_format, ipAddress, String.valueOf(httpPort));
            tvServerUrl.setText(serverUrl);
            tvServerUrl.setVisibility(View.VISIBLE);
        } else {
            tvNetworkStatus.setText(R.string.wifi_not_connected);
            tvNetworkStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            tvServerUrl.setVisibility(View.GONE); // Or Invisible based on layout
        }
    }

    /**
     * 打开Web控制台
     */
    public void openWebConsole(View view) {
        String ipAddress = NetworkUtils.getIpAddress();
        if (ipAddress != null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            int httpPort = prefs.getInt(Constants.Prefs.HTTP_PORT, Constants.Prefs.DEFAULT_HTTP_PORT);
            
            String url = "http://" + ipAddress + ":" + httpPort;
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, 
                    android.net.Uri.parse(url));
            startActivity(browserIntent);
        }
    }

    /**
     * 打开设置
     */
    public void openSettings(View view) {
        // TODO: 实现设置Activity
        Log.i(TAG, "Settings not implemented yet");
    }
}
