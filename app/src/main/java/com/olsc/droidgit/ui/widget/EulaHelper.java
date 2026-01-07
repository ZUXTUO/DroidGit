package com.olsc.droidgit.ui.widget;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

import com.olsc.droidgit.R;

public class EulaHelper {

    public interface OnRequirementAcceptedListener {
        void onAccepted();
    }

    private static final String EULA_PREFIX = "eula_";
    private final Activity mActivity;
    private OnRequirementAcceptedListener mListener;

    public EulaHelper(Activity context) {
        mActivity = context;
    }

    public void setOnRequirementAcceptedListener(OnRequirementAcceptedListener listener) {
        mListener = listener;
    }

    public static boolean isAccepted(Activity activity) {
        PackageInfo pi = null;
        try {
            pi = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        String eulaKey = EULA_PREFIX + pi.versionCode;
        return PreferenceManager.getDefaultSharedPreferences(activity).getBoolean(eulaKey, false);
    }

    private PackageInfo getPackageInfo() {
        PackageInfo pi = null;
        try {
            pi = mActivity.getPackageManager().getPackageInfo(
                    mActivity.getPackageName(), PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return pi;
    }

    public AlertDialog show() {
        PackageInfo versionInfo = getPackageInfo();
        if (versionInfo == null) return null;

        final String eulaKey = EULA_PREFIX + versionInfo.versionCode;
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
        
        boolean hasBeenShown = prefs.getBoolean(eulaKey, false);
        if (!hasBeenShown) {
            String title = mActivity.getString(R.string.app_name) + " v" + versionInfo.versionName;
            String message = mActivity.getString(R.string.updates) + "\n\n" + mActivity.getString(R.string.eula);

            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok, new Dialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean(eulaKey, true);
                            editor.apply();
                            dialogInterface.dismiss();
                            if (mListener != null) {
                                mListener.onAccepted();
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new Dialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mActivity.finish();
                        }
                    });
            
            AlertDialog alert = builder.create();
            alert.show();
            return alert;
        }
        return null;
    }
}
