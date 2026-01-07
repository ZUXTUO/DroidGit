package com.olsc.droidgit.business;

import android.content.Context;
import android.util.Log;

import com.olsc.droidgit.data.database.DatabaseManager;
import com.olsc.droidgit.data.model.GitUser;
import com.olsc.droidgit.util.NetworkUtils;

import java.sql.SQLException;
import java.util.List;

/**
 * 用户业务管理器
 * 处理用户的创建、认证、查询等业务逻辑
 * 使用HTTP基本认证，无需SSH公钥
 */
public class UserManager {
    private static final String TAG = "UserManager";

    private final Context context;
    private final DatabaseManager dbManager;

    public UserManager(Context context) {
        this.context = context;
        this.dbManager = DatabaseManager.getInstance(context);
    }

    /**
     * 创建新用户
     * @param username 用户名
     * @param password 密码（明文，将被加密）
     * @param fullname 全名
     * @param email 邮箱
     * @return 创建的用户对象
     */
    public GitUser createUser(String username, String password, String fullname, String email)
            throws UserException {
        
        // 验证参数
        if (username == null || username.trim().isEmpty()) {
            throw new UserException("Username cannot be empty");
        }
        if (password == null || password.isEmpty()) {
            throw new UserException("Password cannot be empty");
        }

        try {
            // 检查用户名是否已存在
            List<GitUser> existing = dbManager.getUserDao()
                    .queryForEq("username", username);
            if (!existing.isEmpty()) {
                throw new UserException("Username already exists: " + username);
            }

            // 加密密码
            String hashedPassword = NetworkUtils.generateSha1(password);

            // 创建用户
            GitUser user = new GitUser(username, hashedPassword, fullname, email);
            dbManager.getUserDao().create(user);

            Log.i(TAG, "Created user: " + username);
            return user;

        } catch (SQLException e) {
            throw new UserException("Database error while creating user", e);
        }
    }

    /**
     * 验证用户凭证（HTTP基本认证）
     * @param username 用户名
     * @param password 密码（明文）
     * @return 如果认证成功返回true
     */
    public boolean authenticate(String username, String password) {
        if (username == null || password == null) {
            return false;
        }

        try {
            List<GitUser> users = dbManager.getUserDao()
                    .queryForEq("username", username);
            
            if (users.isEmpty()) {
                return false;
            }

            GitUser user = users.get(0);
            if (!user.isActive()) {
                return false;
            }

            // 比较加密后的密码
            String hashedPassword = NetworkUtils.generateSha1(password);
            boolean authenticated = hashedPassword.equals(user.getPassword());

            if (authenticated) {
                Log.i(TAG, "User authenticated: " + username);
            } else {
                Log.w(TAG, "Authentication failed for user: " + username);
            }

            return authenticated;

        } catch (SQLException e) {
            Log.e(TAG, "Database error during authentication", e);
            return false;
        }
    }

    /**
     * 获取用户
     */
    public GitUser getUser(String username) throws UserException {
        try {
            List<GitUser> users = dbManager.getUserDao()
                    .queryForEq("username", username);
            return users.isEmpty() ? null : users.get(0);
        } catch (SQLException e) {
            throw new UserException("Database error while fetching user", e);
        }
    }

    /**
     * 获取所有用户
     */
    public List<GitUser> getAllUsers() throws UserException {
        try {
            return dbManager.getUserDao().queryForAll();
        } catch (SQLException e) {
            throw new UserException("Database error while fetching users", e);
        }
    }

    /**
     * 更新用户信息
     */
    public void updateUser(int userId, String fullname, String email) throws UserException {
        try {
            GitUser user = dbManager.getUserDao().queryForId(userId);
            if (user == null) {
                throw new UserException("User not found: " + userId);
            }

            user.setFullname(fullname);
            user.setEmail(email);
            dbManager.getUserDao().update(user);

            Log.i(TAG, "Updated user: " + user.getUsername());

        } catch (SQLException e) {
            throw new UserException("Database error while updating user", e);
        }
    }

    /**
     * 修改密码
     */
    public void changePassword(int userId, String newPassword) throws UserException {
        if (newPassword == null || newPassword.isEmpty()) {
            throw new UserException("Password cannot be empty");
        }

        try {
            GitUser user = dbManager.getUserDao().queryForId(userId);
            if (user == null) {
                throw new UserException("User not found: " + userId);
            }

            String hashedPassword = NetworkUtils.generateSha1(newPassword);
            user.setPassword(hashedPassword);
            dbManager.getUserDao().update(user);

            Log.i(TAG, "Password changed for user: " + user.getUsername());

        } catch (SQLException e) {
            throw new UserException("Database error while changing password", e);
        }
    }

    /**
     * 删除用户
     */
    public void deleteUser(int userId) throws UserException {
        try {
            GitUser user = dbManager.getUserDao().queryForId(userId);
            if (user == null) {
                throw new UserException("User not found: " + userId);
            }

            dbManager.getUserDao().deleteById(userId);
            Log.i(TAG, "Deleted user: " + user.getUsername());

        } catch (SQLException e) {
            throw new UserException("Database error while deleting user", e);
        }
    }

    /**
     * 释放资源
     */
    public void close() {
    }

    /**
     * 用户操作异常
     */
    public static class UserException extends Exception {
        public UserException(String message) {
            super(message);
        }

        public UserException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
