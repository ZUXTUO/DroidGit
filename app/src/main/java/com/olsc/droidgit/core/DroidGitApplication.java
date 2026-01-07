package com.olsc.droidgit.core;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.olsc.droidgit.util.LocaleHelper;

public class DroidGitApplication extends Application {
    private final static String TAG = DroidGitApplication.class.getSimpleName();
    private static DroidGitApplication instance;

    public DroidGitApplication() {
        instance = this;
    }

    public static DroidGitApplication getInstance() {
        return instance;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "[App] Started!");
    }
}
