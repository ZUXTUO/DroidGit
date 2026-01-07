package com.olsc.droidgit.util;

import android.os.Environment;

/**
 * 应用常量定义
 * 整合了所有分散的常量，使用更清晰的命名
 */
public final class Constants {

    private Constants() {
        // 防止实例化
    }

    /**
     * Broadcast Actions - 广播动作
     */
    public static final class Action {
        public static final String GIT_SERVER_STARTED = "com.olsc.droidgit.GIT_SERVER_STARTED";
        public static final String GIT_SERVER_STOPPED = "com.olsc.droidgit.GIT_SERVER_STOPPED";
        public static final String RESTART_SERVICE = "com.olsc.droidgit.RESTART_SERVICE";
        
        // Activity启动动作
        public static final String START_HOME = "com.olsc.droidgit.START_HOME";
        public static final String START_REPOSITORY_MANAGER = "com.olsc.droidgit.START_REPOSITORY_MANAGER";
        public static final String START_SETTINGS = "com.olsc.droidgit.START_SETTINGS";
        
        private Action() {}
    }

    /**
     * SharedPreferences Keys - 配置项键值
     */
    public static final class Prefs {
        // HTTP服务器端口
        public static final String HTTP_PORT = "http_port";
        public static final int DEFAULT_HTTP_PORT = 8080;
        
        // Git仓库目录
        public static final String GIT_ROOT_DIR = "git_repositories_dir";
        public static final String DEFAULT_GIT_ROOT_DIR = 
            Environment.getExternalStorageDirectory().getPath() + "/DroidGit/repositories";
        
        // 服务设置
        public static final String SHOW_NOTIFICATION = "show_notification";
        public static final boolean DEFAULT_SHOW_NOTIFICATION = true;
        
        public static final String AUTO_START = "auto_start";
        public static final boolean DEFAULT_AUTO_START = false;
        
        // WiFi相关
        public static final String AUTO_START_ON_WIFI = "auto_start_on_wifi";
        public static final boolean DEFAULT_AUTO_START_ON_WIFI = false;
        
        public static final String AUTO_STOP_ON_WIFI_OFF = "auto_stop_on_wifi_off";
        public static final boolean DEFAULT_AUTO_STOP_ON_WIFI_OFF = false;
        
        // 语言设置
        public static final String LANGUAGE = "language";
        public static final String DEFAULT_LANGUAGE = "system";
        
        // EULA
        public static final String EULA_ACCEPTED = "eula_accepted";
        public static final boolean DEFAULT_EULA_ACCEPTED = false;
        
        private Prefs() {}
    }

    /**
     * 通知ID
     */
    public static final class Notification {
        public static final int GIT_SERVER_RUNNING = 1001;
        public static final String CHANNEL_ID = "git_server_channel";
        public static final String CHANNEL_NAME = "Git Server Service";
        
        private Notification() {}
    }

    /**
     * 权限请求代码
     */
    public static final class Permission {
        public static final int STORAGE_PERMISSION = 100;
        public static final int POST_NOTIFICATIONS = 101;
        
        private Permission() {}
    }

    /**
     * Git相关常量
     */
    public static final class Git {
        // Git协议相关
        public static final String SERVICE_UPLOAD_PACK = "git-upload-pack";
        public static final String SERVICE_RECEIVE_PACK = "git-receive-pack";
        
        // 路径模式
        public static final String PATH_INFO_REFS = "/info/refs";
        public static final String PATH_UPLOAD_PACK = "/git-upload-pack";
        public static final String PATH_RECEIVE_PACK = "/git-receive-pack";
        
        // 仓库文件扩展名
        public static final String REPO_EXTENSION = ".git";
        
        private Git() {}
    }

    /**
     * 数据库相关
     */
    public static final class Database {
        public static final String NAME = "droidgit.db";
        public static final int VERSION = 1;
        
        // 表名
        public static final String TABLE_REPOSITORIES = "repositories";
        public static final String TABLE_USERS = "users";
        public static final String TABLE_PERMISSIONS = "permissions";
        
        private Database() {}
    }
}
