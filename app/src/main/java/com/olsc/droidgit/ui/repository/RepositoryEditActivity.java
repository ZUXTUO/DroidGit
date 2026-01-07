package com.olsc.droidgit.ui.repository;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.olsc.droidgit.R;
import com.olsc.droidgit.business.RepositoryManager;
import com.olsc.droidgit.data.model.GitRepository;
import com.olsc.droidgit.util.LocaleHelper;

public class RepositoryEditActivity extends AppCompatActivity {

    private static final String TAG = "RepositoryEditActivity";
    public static final String EXTRA_REPOSITORY_ID = "repositoryId";

    private EditText nameEditText;
    private EditText mappingEditText;
    private EditText descriptionEditText;
    private CheckBox activeCheckBox;

    private RepositoryManager repositoryManager;
    private GitRepository currentRepository;
    private int repositoryId = -1;

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme); 
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_repository_zen);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.add_repository_title);
        }

        repositoryManager = new RepositoryManager(this);

        nameEditText = findViewById(R.id.addRepositoryName);
        mappingEditText = findViewById(R.id.addRepositoryMapping);
        descriptionEditText = findViewById(R.id.addRepositoryDescription);
        activeCheckBox = findViewById(R.id.addRepositoryActivate);

        if (getIntent().hasExtra(EXTRA_REPOSITORY_ID)) {
            repositoryId = getIntent().getIntExtra(EXTRA_REPOSITORY_ID, -1);
            if (repositoryId != -1) {
                loadRepository();
            }
        }
    }

    private void loadRepository() {
        try {

            for (GitRepository repo : repositoryManager.getAllRepositories()) {
                if (repo.getId() == repositoryId) {
                    currentRepository = repo;
                    break;
                }
            }

            if (currentRepository != null) {
                 if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(R.string.activity_repository_details_label);
                }
                nameEditText.setText(currentRepository.getName());
                mappingEditText.setText(currentRepository.getMapping());
                descriptionEditText.setText(currentRepository.getDescription());
                activeCheckBox.setChecked(currentRepository.isActive());
                
                mappingEditText.setEnabled(false);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading repository", e);
            Toast.makeText(this, R.string.error_loading_repositories, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem saveItem = menu.add(0, 1, 0, R.string.save);
        saveItem.setIcon(R.drawable.ic_actionbar_accept);
        saveItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == 1) { // 保存
            saveRepository();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveRepository() {
        String name = nameEditText.getText().toString().trim();
        String mapping = mappingEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        boolean isActive = activeCheckBox.isChecked();

        if (TextUtils.isEmpty(name)) {
            nameEditText.setError(getString(R.string.error_field_required));
            return;
        }
        if (TextUtils.isEmpty(mapping)) {
            mappingEditText.setError(getString(R.string.error_field_required));
            return;
        }

        try {
            if (currentRepository != null) {
                // 更新
                currentRepository.setName(name);
                currentRepository.setDescription(description);
                currentRepository.setActive(isActive);
                // 映射未更新，因为它是禁用的
                
                repositoryManager.updateRepository(currentRepository);
                Toast.makeText(this, R.string.repository_updated, Toast.LENGTH_SHORT).show();
            } else {
                // 创建
                GitRepository newRepo = repositoryManager.createRepository(name, mapping, description);
                // createRepository默认active=true。使用manager的update或者我们应该假设它创建时是激活的。
                // 如果用户在创建期间取消选中了激活状态，我们会更新它。
                if (!isActive) {
                    newRepo.setActive(false);
                    repositoryManager.updateRepository(newRepo);
                }
                Toast.makeText(this, R.string.repository_created, Toast.LENGTH_SHORT).show();
            }
            
            // 返回结果OK
            setResult(RESULT_OK);
            finish();
            
        } catch (Exception e) {
            Log.e(TAG, "Error saving repository", e);
            Toast.makeText(this, getString(R.string.error_occurred, e.getMessage()), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (repositoryManager != null) {
            repositoryManager.close();
        }
    }
}
