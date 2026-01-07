package com.olsc.droidgit.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.olsc.droidgit.R;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(com.olsc.droidgit.util.LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_splash);
        
        // 延迟跳转
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, 2000);
    }
}
