package com.olsc.droidgit.ui.repository;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.olsc.droidgit.R;
import com.olsc.droidgit.ui.home.MainActivity;
import com.olsc.droidgit.util.LocaleHelper;

public class RepositoryListActivity extends AppCompatActivity {

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
            getSupportActionBar().setTitle(R.string.activity_setup_label);
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new RepositoryListFragment())
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem addMenuItem = menu.add(R.string.add_repository_title);
        addMenuItem.setIcon(R.drawable.ic_actionbar_add_repository);
        addMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        addMenuItem.setOnMenuItemClickListener(item -> {
            Intent intent = new Intent(this, RepositoryEditActivity.class);
            startActivity(intent);
            return true;
        });
        return true;
    }
}
