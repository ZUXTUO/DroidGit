package com.olsc.droidgit.data.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * 用户数据模型
 * HTTP Git服务器使用HTTP基本认证
 */
@DatabaseTable(tableName = "users")
public class GitUser implements Serializable {
    private static final long serialVersionUID = 1L;

    @DatabaseField(columnName = "id", generatedId = true)
    private int id;

    @DatabaseField(columnName = "username", canBeNull = false, unique = true)
    private String username;

    @DatabaseField(columnName = "password", canBeNull = false)
    private String password;

    @DatabaseField(columnName = "fullname")
    private String fullname;

    @DatabaseField(columnName = "email", unique = true)
    private String email;

    @DatabaseField(columnName = "active", defaultValue = "true")
    private boolean active;

    @DatabaseField(columnName = "create_date")
    private long createDate;

    public GitUser() {
        this.active = true;
        this.createDate = System.currentTimeMillis();
    }

    public GitUser(String username, String password, String fullname, String email) {
        this();
        this.username = username;
        this.password = password;
        this.fullname = fullname;
        this.email = email;
    }

    // Getter和Setter方法

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    @Override
    public String toString() {
        return "GitUser{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", fullname='" + fullname + '\'' +
                ", email='" + email + '\'' +
                ", active=" + active +
                '}';
    }
}
