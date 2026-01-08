package com.olsc.droidgit.ui.repository;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.olsc.droidgit.R;
import com.olsc.droidgit.data.model.GitRepository;

import java.util.List;

/**
 * 仓库列表适配器
 * 用于在列表中显示仓库信息，包括状态（归档、私有等）
 */
public class RepositoryListAdapter extends BaseAdapter {

    private final LayoutInflater inflater;
    private List<GitRepository> items;
    private final int itemResourceId;

    public RepositoryListAdapter(Context context, int resourceId, List<GitRepository> items) {
        this.itemResourceId = resourceId;
        this.inflater = LayoutInflater.from(context);
        this.items = items;
    }

    @Override
    public int getCount() {
        return items != null ? items.size() : 0;
    }

    @Override
    public GitRepository getItem(int position) {
        return items != null ? items.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            v = inflater.inflate(itemResourceId, parent, false);
        }

        GitRepository repository = getItem(position);
        if (repository != null) {
            TextView repositoryName = v.findViewById(R.id.repositoriesItemName);
            if (repositoryName != null) {
                StringBuilder displayName = new StringBuilder(repository.getName());
                Context context = parent.getContext();

                if (repository.isArchived()) {
                    displayName.append(" [").append(context.getString(R.string.archive_action)).append("]");
                }
                if (!repository.isActive()) {
                    displayName.append(" [").append(context.getString(R.string.deactivate)).append("]");
                }

                repositoryName.setText(displayName.toString());
            }
        }

        return v;
    }

    public void setItems(List<GitRepository> items) {
        this.items = items;
        notifyDataSetChanged();
    }
}
