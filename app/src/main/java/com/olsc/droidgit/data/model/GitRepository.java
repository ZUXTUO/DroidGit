package com.olsc.droidgit.data.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Git仓库数据模型
 * 简化的数据模型
 */
@DatabaseTable(tableName = "repositories")
public class GitRepository implements Serializable {
    private static final long serialVersionUID = 1L;

    @DatabaseField(columnName = "id", generatedId = true)
    private int id;

    @DatabaseField(columnName = "name", canBeNull = false)
    private String name;

    @DatabaseField(columnName = "mapping", canBeNull = false, unique = true)
    private String mapping;

    @DatabaseField(columnName = "description")
    private String description;

    @DatabaseField(columnName = "active", defaultValue = "true")
    private boolean active;

    @DatabaseField(columnName = "create_date")
    private long createDate;

    @ForeignCollectionField(eager = false)
    private Collection<RepositoryPermission> permissions;

    public GitRepository() {
        this.active = true;
        this.createDate = System.currentTimeMillis();
    }

    public GitRepository(String name, String mapping, String description) {
        this();
        this.name = name;
        this.mapping = mapping;
        this.description = description;
    }

    // Getter和Setter方法

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMapping() {
        return mapping;
    }

    public void setMapping(String mapping) {
        this.mapping = mapping;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public Collection<RepositoryPermission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Collection<RepositoryPermission> permissions) {
        this.permissions = permissions;
    }

    /**
     * 获取权限列表（转换为List）
     */
    public List<RepositoryPermission> getPermissionsList() {
        if (permissions == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(permissions);
    }

    @DatabaseField(columnName = "archived", defaultValue = "false")
    private boolean archived;

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public boolean isArchived() {
        return archived;
    }

    @Override
    public String toString() {
        return "GitRepository{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", mapping='" + mapping + '\'' +
                ", active=" + active +
                ", archived=" + archived +
                '}';
    }
}
