package com.olsc.droidgit.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;

import java.util.Locale;

/**
 * 语言助手类
 * 用于动态切换应用语言
 */
public class LocaleHelper {

    public static Context onAttach(Context context) {
        String lang = getPersistedData(context, "system");
        return setLocale(context, lang);
    }

    public static Context setLocale(Context context, String language) {
        persist(context, language);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return updateResources(context, language);
        }

        return updateResourcesLegacy(context, language);
    }

    private static String getPersistedData(Context context, String defaultLanguage) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(Constants.Prefs.LANGUAGE, defaultLanguage);
    }

    private static void persist(Context context, String language) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.Prefs.LANGUAGE, language);
        editor.apply();
    }

    @TargetApi(Build.VERSION_CODES.N)
    private static Context updateResources(Context context, String language) {
        Locale locale = getLocale(language);
        Locale.setDefault(locale);

        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);

        return context.createConfigurationContext(configuration);
    }

    @SuppressWarnings("deprecation")
    private static Context updateResourcesLegacy(Context context, String language) {
        Locale locale = getLocale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLayoutDirection(locale);
        }

        resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        return context;
    }

    private static Locale getLocale(String language) {
        if ("system".equals(language) || "default".equals(language)) {
            // 获取系统真实的语言设置，而不是应用当前的 Context 语言
            Configuration sysConfig = Resources.getSystem().getConfiguration();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                return sysConfig.getLocales().get(0);
            } else {
                return sysConfig.locale;
            }
        }
        
        if (language.contains("-")) {
            String[] parts = language.split("-");
            if (parts.length > 1) {
                return new Locale(parts[0], parts[1]);
            }
        }
        return new Locale(language);
    }
}
