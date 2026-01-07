package com.olsc.droidgit.ui.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

import com.olsc.droidgit.R;

/**
 * 检测MIUI并引导用户至白名单设置的辅助类
 */
public class MIUIHelper {
    private static final String TAG = "MIUIHelper";
    
    /**
     * 检查设备是否运行MIUI
     */
    public static boolean isMIUI() {
        return !getProperty("ro.miui.ui.version.name", "").isEmpty();
    }
    
    /**
     * 获取系统属性值
     */
    private static String getProperty(String key, String defaultValue) {
        try {
            Class<?> spClass = Class.forName("android.os.SystemProperties");
            java.lang.reflect.Method method = spClass.getMethod("get", String.class, String.class);
            return (String) method.invoke(null, key, defaultValue);
        } catch (Exception e) {
            Log.w(TAG, "Unable to read system property " + key, e);
            return defaultValue;
        }
    }
    
    /**
     * 显示对话框引导用户至MIUI白名单设置
     */
    public static void showMIUIWhitelistDialog(Context context) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.miui_dialog_title)
                .setMessage(R.string.miui_dialog_message)
                .setPositiveButton(R.string.miui_dialog_button_settings, (dialog, which) -> {
                    openMIUISettings(context);
                })
                .setNegativeButton(R.string.miui_dialog_button_later, null)
                .setNeutralButton(R.string.miui_dialog_button_dont_show, (dialog, which) -> {
                    // 保存偏好设置以不再显示
                    context.getSharedPreferences("miui_settings", Context.MODE_PRIVATE)
                            .edit()
                            .putBoolean("dont_show_miui_dialog", true)
                            .apply();
                })
                .show();
    }
    
    /**
     * 检查是否应显示MIUI对话框
     */
    public static boolean shouldShowMIUIDialog(Context context) {
        if (!isMIUI()) {
            return false;
        }
        
        boolean dontShow = context.getSharedPreferences("miui_settings", Context.MODE_PRIVATE)
                .getBoolean("dont_show_miui_dialog", false);
        
        return !dontShow;
    }
    
    /**
     * 打开MIUI应用设置页面
     */
    private static void openMIUISettings(Context context) {
        try {
            // 尝试打开MIUI的应用信息页面
            Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
            intent.setClassName("com.miui.securitycenter",
                    "com.miui.permcenter.permissions.PermissionsEditorActivity");
            intent.putExtra("extra_pkgname", context.getPackageName());
            context.startActivity(intent);
        } catch (Exception e) {
            Log.w(TAG, "Failed to open MIUI settings", e);
            // 回退到标准应用设置
            try {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(android.net.Uri.parse("package:" + context.getPackageName()));
                context.startActivity(intent);
            } catch (Exception ex) {
                Log.e(TAG, "Failed to open app settings", ex);
            }
        }
    }
}
