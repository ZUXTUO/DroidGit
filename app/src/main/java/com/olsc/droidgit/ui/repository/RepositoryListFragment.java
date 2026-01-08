package com.olsc.droidgit.ui.repository;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.olsc.droidgit.R;
import com.olsc.droidgit.business.RepositoryManager;
import com.olsc.droidgit.data.model.GitRepository;

import java.util.List;

public class RepositoryListFragment extends Fragment
        implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private static final String TAG = "RepositoryListFragment";

    private ListView repositoriesListView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView noRepositoriesTextView;
    private RepositoryListAdapter adapter;
    private RepositoryManager repositoryManager;
    private ActionMode actionMode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_repositories_zen, container, false);

        repositoriesListView = view.findViewById(R.id.repositoriesListView);
        noRepositoriesTextView = view.findViewById(R.id.repositoriesNoRepositoriesTextView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        swipeRefreshLayout.setOnRefreshListener(this::loadRepositories);

        repositoriesListView.setOnItemClickListener(this);
        repositoriesListView.setOnItemLongClickListener(this);

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        repositoryManager = new RepositoryManager(context);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRepositories();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (repositoryManager != null) {
            repositoryManager.close();
        }
    }

    private void loadRepositories() {
        try {
            List<GitRepository> repositories = repositoryManager.getAllRepositories();
            if (adapter == null) {
                adapter = new RepositoryListAdapter(requireContext(), R.layout.item_repository_zen, repositories);
                repositoriesListView.setAdapter(adapter);
            } else {
                adapter.setItems(repositories);
            }

            if (repositories.isEmpty()) {
                repositoriesListView.setVisibility(View.GONE);
                noRepositoriesTextView.setVisibility(View.VISIBLE);
            } else {
                repositoriesListView.setVisibility(View.VISIBLE);
                noRepositoriesTextView.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading repositories", e);
            Toast.makeText(getContext(), R.string.error_loading_repositories, Toast.LENGTH_SHORT).show();
        } finally {
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        GitRepository repository = adapter.getItem(position);
        if (repository != null) {
            openRepositoryEdit(repository.getId());
        }
    }

    private void openRepositoryEdit(int repositoryId) {
        Intent intent = new Intent(getActivity(), RepositoryEditActivity.class);
        intent.putExtra("repositoryId", repositoryId);
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (actionMode != null) {
            return false;
        }

        actionMode = repositoriesListView.startActionMode(new ActionModeCallback(position), ActionMode.TYPE_PRIMARY);
        return true;
    }

    private class ActionModeCallback implements ActionMode.Callback {
        private final int position;
        private GitRepository repository;

        public ActionModeCallback(int position) {
            this.position = position;
            this.repository = adapter.getItem(position);
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            if (repository == null)
                return false;

            mode.setTitle(repository.getName());

            // ID 1: Delete
            menu.add(0, 1, 0, R.string.delete).setIcon(R.drawable.ic_actionbar_delete)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

            // ID 2/3: Activate/Deactivate (Public/Private)
            if (!repository.isActive()) {
                menu.add(0, 2, 0, R.string.activate);
            } else {
                menu.add(0, 3, 0, R.string.deactivate);
            }

            // ID 5/6: Archive/Unarchive
            if (repository.isArchived()) {
                menu.add(0, 6, 0, R.string.unarchive_action);
            } else {
                menu.add(0, 5, 0, R.string.archive_action);
            }

            // ID 4: Edit
            menu.add(0, 4, 0, R.string.edit).setIcon(R.drawable.ic_actionbar_edit)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case 1: // 删除
                    confirmDelete(repository);
                    break;
                case 2: // 激活 (Public)
                    toggleActive(repository, true);
                    break;
                case 3: // 停用 (Private)
                    toggleActive(repository, false);
                    break;
                case 4: // 编辑
                    openRepositoryEdit(repository.getId());
                    break;
                case 5: // 归档
                    toggleArchive(repository, true);
                    break;
                case 6: // 取消归档
                    toggleArchive(repository, false);
                    break;
            }
            mode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
        }
    }

    private void confirmDelete(GitRepository repository) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_delete_repository_title)
                .setMessage(getString(R.string.dialog_delete_repository_message, repository.getName()))
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    try {
                        repositoryManager.deleteRepository(repository.getId());
                        loadRepositories();
                    } catch (Exception e) {
                        Log.e(TAG, "Error deleting repository", e);
                        Toast.makeText(getContext(), R.string.error_deleting_repository, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private void toggleActive(GitRepository repository, boolean active) {
        try {
            repository.setActive(active);
            repositoryManager.updateRepository(repository);
            loadRepositories();
        } catch (Exception e) {
            Log.e(TAG, "Error updating repository", e);
            Toast.makeText(getContext(), R.string.error_updating_repository, Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleArchive(GitRepository repository, boolean archived) {
        try {
            repository.setArchived(archived);
            repositoryManager.updateRepository(repository);
            loadRepositories();

            String message = archived ? getString(R.string.repository_archived_toast)
                    : getString(R.string.repository_unarchived_toast);
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error updating repository archive status", e);
            Toast.makeText(getContext(), R.string.error_updating_repository, Toast.LENGTH_SHORT).show();
        }
    }
}
