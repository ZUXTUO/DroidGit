package com.olsc.droidgit.data.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * 仓库权限数据模型
 * 定义用户对仓库的访问权限
 */
@DatabaseTable(tableName = "permissions")
public class RepositoryPermission implements Serializable {
    private static final long serialVersionUID = 1L;

    @DatabaseField(columnName = "id", generatedId = true)
    private int id;

    @DatabaseField(columnName = "user_id", foreign = true, foreignAutoRefresh = true, 
            uniqueCombo = true, canBeNull = false)
    private GitUser user;

    @DatabaseField(columnName = "repository_id", foreign = true, foreignAutoRefresh = true, 
            uniqueCombo = true, canBeNull = false)
    private GitRepository repository;

    @DatabaseField(columnName = "read_only", defaultValue = "false")
    private boolean readOnly;

    public RepositoryPermission() {
    }

    public RepositoryPermission(GitUser user, GitRepository repository, boolean readOnly) {
        this.user = user;
        this.repository = repository;
        this.readOnly = readOnly;
    }

    // Getter和Setter方法

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public GitUser getUser() {
        return user;
    }

    public void setUser(GitUser user) {
        this.user = user;
    }

    public GitRepository getRepository() {
        return repository;
    }

    public void setRepository(GitRepository repository) {
        this.repository = repository;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override
    public String toString() {
        return "RepositoryPermission{" +
                "id=" + id +
                ", user=" + (user != null ? user.getUsername() : "null") +
                ", repository=" + (repository != null ? repository.getName() : "null") +
                ", readOnly=" + readOnly +
                '}';
    }
}
