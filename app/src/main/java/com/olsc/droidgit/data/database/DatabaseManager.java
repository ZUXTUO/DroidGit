package com.olsc.droidgit.data.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import com.olsc.droidgit.data.model.GitRepository;
import com.olsc.droidgit.data.model.GitUser;
import com.olsc.droidgit.data.model.RepositoryPermission;
import com.olsc.droidgit.util.Constants;

import java.sql.SQLException;

/**
 * 数据库管理器
 * 统一管理所有数据库操作，提供DAO访问接口
 */
public class DatabaseManager extends OrmLiteSqliteOpenHelper {
    private static final String TAG = "DatabaseManager";

    private static DatabaseManager instance;
    
    // DAO缓存
    private Dao<GitRepository, Integer> repositoryDao;
    private Dao<GitUser, Integer> userDao;
    private Dao<RepositoryPermission, Integer> permissionDao;

    public static synchronized DatabaseManager getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseManager(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseManager(Context context) {
        super(context, Constants.Database.NAME, null, Constants.Database.VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            Log.i(TAG, "Creating database tables");
            TableUtils.createTable(connectionSource, GitRepository.class);
            TableUtils.createTable(connectionSource, GitUser.class);
            TableUtils.createTable(connectionSource, RepositoryPermission.class);
            Log.i(TAG, "Database tables created successfully");
        } catch (SQLException e) {
            Log.e(TAG, "Failed to create database tables", e);
            throw new RuntimeException("Database creation failed", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource,
                          int oldVersion, int newVersion) {
        try {
            Log.i(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
            
            // 简单策略：删除旧表，创建新表
            // 注意：现在这个版本我还在测试，这样做会丢失所有数据，生产环境应该做数据迁移
            TableUtils.dropTable(connectionSource, RepositoryPermission.class, true);
            TableUtils.dropTable(connectionSource, GitUser.class, true);
            TableUtils.dropTable(connectionSource, GitRepository.class, true);
            
            onCreate(database, connectionSource);
            
            Log.i(TAG, "Database upgraded successfully");
        } catch (SQLException e) {
            Log.e(TAG, "Failed to upgrade database", e);
            throw new RuntimeException("Database upgrade failed", e);
        }
    }

    /**
     * 获取仓库DAO
     */
    public Dao<GitRepository, Integer> getRepositoryDao() {
        if (repositoryDao == null) {
            try {
                repositoryDao = getDao(GitRepository.class);
            } catch (SQLException e) {
                Log.e(TAG, "Failed to get RepositoryDao", e);
                throw new RuntimeException("Cannot access repository data", e);
            }
        }
        return repositoryDao;
    }

    /**
     * 获取用户DAO
     */
    public Dao<GitUser, Integer> getUserDao() {
        if (userDao == null) {
            try {
                userDao = getDao(GitUser.class);
            } catch (SQLException e) {
                Log.e(TAG, "Failed to get UserDao", e);
                throw new RuntimeException("Cannot access user data", e);
            }
        }
        return userDao;
    }

    /**
     * 获取权限DAO
     */
    public Dao<RepositoryPermission, Integer> getPermissionDao() {
        if (permissionDao == null) {
            try {
                permissionDao = getDao(RepositoryPermission.class);
            } catch (SQLException e) {
                Log.e(TAG, "Failed to get PermissionDao", e);
                throw new RuntimeException("Cannot access permission data", e);
            }
        }
        return permissionDao;
    }

    /**
     * 清理资源
     */
    @Override
    public void close() {
        super.close();
        repositoryDao = null;
        userDao = null;
        permissionDao = null;
    }
}
