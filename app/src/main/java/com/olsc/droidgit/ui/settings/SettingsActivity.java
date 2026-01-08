package com.olsc.droidgit.ui.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.olsc.droidgit.R;
import com.olsc.droidgit.util.LocaleHelper;
import com.olsc.droidgit.ui.home.MainActivity;
import com.olsc.droidgit.util.Constants;

/**
 * 设置页面 Activity
 * 承载设置 Fragment，处理语言切换等逻辑
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_zen);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.activity_settings_label);
        }

        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new SettingsFragment())
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences_http);

            updateSummaries();
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
            updateSummaries();
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            updateSummaries();

            if (Constants.Prefs.LANGUAGE.equals(key)) {
                if (getActivity() != null) {
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    getActivity().finish();
                }
            }
        }

        private void updateSummaries() {
            EditTextPreference portPref = (EditTextPreference) findPreference(Constants.Prefs.HTTP_PORT);
            if (portPref != null) {
                portPref.setSummary(portPref.getText());
                portPref.setTitle(R.string.settings_http_server_port);
            }

            EditTextPreference dirPref = (EditTextPreference) findPreference(Constants.Prefs.GIT_ROOT_DIR);
            if (dirPref != null) {
                dirPref.setSummary(dirPref.getText());
            }

            ListPreference langPref = (ListPreference) findPreference(Constants.Prefs.LANGUAGE);
            if (langPref != null) {
                langPref.setSummary(langPref.getEntry());
            }
        }
    }
}
