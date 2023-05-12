package me.z5882852.totalpoints.logger;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;


public class PointsLoggerManager{
    private JavaPlugin plugin;
    private FileConfiguration config;
    private boolean enableMySQL;
    private boolean enableLoggerMySQL;
    private boolean enableStackTrace;
    private Logger logger;
    private FileHandler fileHandler;
    private Connection conn;
    private String table;


    public PointsLoggerManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.logger = Logger.getLogger(PointsLoggerManager.class.getName());
        this.enableMySQL = config.getBoolean("mysql.enable", false);
        if (config.getString("logger.type", "local").equals("mysql")) {
            enableLoggerMySQL = true;
        }
        if (!enableMySQL && enableLoggerMySQL) {
            plugin.getLogger().warning("您在全局中未开启mysql存储，日志存储自动设置为local");
            config.set("logger.type", "local");
            enableLoggerMySQL = false;
        }
        this.enableStackTrace = config.getBoolean("logger.enable_stackTrace", false);
        if (enableLoggerMySQL) {
            try {
                String username = config.getString("mysql.user");
                String password = config.getString("mysql.password");
                table = config.getString("logger.logger_table", "PointsChange");
                String url = "jdbc:mysql://" + config.getString("mysql.host") + ":" + config.getString("mysql.port") + "/" + config.getString("mysql.database") + config.getString("mysql.params");
                this.conn = DriverManager.getConnection(url, username, password);
                // 检查是否存在表
                Statement statement = conn.createStatement();
                String sql = String.format("SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_NAME = '%s'", table);
                ResultSet resultSet = statement.executeQuery(sql);
                resultSet.next();
                int count = resultSet.getInt(1);
                // 如果表不存在，则创建一个新表
                if (count == 0) {
                    plugin.getLogger().info("检测到日志表不存在，正在创建新的表...");
                    sql = String.format("CREATE TABLE `%s` (", table) +
                            "  `id` int NOT NULL AUTO_INCREMENT," +
                            "  `date` datetime NOT NULL," +
                            "  `uuid` varchar (36) NOT NULL DEFAULT ''," +
                            "  `name` text NOT NULL," +
                            "  `type` text NOT NULL," +
                            "  `change` text NOT NULL," +
                            "  `StackTrace` text NOT NULL," +
                            "  PRIMARY KEY (`id`)" +
                            ") ENGINE = innodb";
                    statement.executeUpdate(sql);
                    plugin.getLogger().info("日志表创建成功!");
                }
                resultSet.close();
                statement.close();
            } catch (SQLException e) {
                plugin.getLogger().severe("数据库错误: " + e.getMessage());
                e.printStackTrace();
            }
        }else{
            try {
                String logFileName = new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".log";
                File logFile = new File(plugin.getDataFolder() + File.separator + "logs" + File.separator + logFileName);
                logFile.getParentFile().mkdirs();
                fileHandler = new FileHandler(logFile.getAbsolutePath(), true);
                fileHandler.setFormatter(new SimpleFormatter());
                logger.addHandler(fileHandler);
                logger.setLevel(Level.ALL);
            } catch (IOException e) {
                plugin.getLogger().severe("创建日志对象失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void log(OfflinePlayer offlinePlayer, int changePoints) {
        String stackTraceElementClassName = "null";
        String type = "获得";
        if (changePoints < 0) {
            type = "失去";
            changePoints = -changePoints;
        } else if (changePoints == 0) {
            type = "重置";
        }
        if (enableStackTrace) {
            stackTraceElementClassName = String.join(" \n", getStackTraceElementClassName());
        }
        // 格式[时间] [玩家UUID] [玩家名字] [变化类型] [变化数值] [StackTraceClassName_1, StackTraceClassName_2, ...]
        String message = String.format("[%s][%s][%s][%s][%d]\n[%s]\n", getCurrentTimeString(), offlinePlayer.getUniqueId().toString(), offlinePlayer.getName(), type, changePoints, stackTraceElementClassName);
        if (enableLoggerMySQL) {
            insertLog(offlinePlayer, type, String.valueOf(changePoints), stackTraceElementClassName);
        } else {
            logger.log(Level.INFO, message);
        }
    }

    public void insertLog(OfflinePlayer offlinePlayer, String type, String changePoints, String stackTraceElementClassName) {
        try {
            Statement statement = conn.createStatement();
            String sql = String.format("INSERT INTO `%s` (`date`, `uuid`, `name`, `type`, `change`, `StackTrace`) VALUES ('%s', '%s', '%s', '%s', '%s', '%s');", table, getCurrentTimeString(), offlinePlayer.getUniqueId().toString(), offlinePlayer.getName(), type, changePoints, stackTraceElementClassName);
            statement.executeUpdate(sql);
            statement.close();
            plugin.getLogger().info("记录数据成功。");
        } catch (SQLException e) {
            plugin.getLogger().severe("记录数据失败,请查看以下报错信息:");
            e.printStackTrace();
        }
    }

    public List<String> getStackTraceElementClassName() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        List<String> className = new ArrayList<>();
        for (StackTraceElement element : stackTrace) {
            className.add(element.getClassName());
        }
        return className;
    }

    public String getCurrentTimeString() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }

    public void close() {
        // 关闭日志文件
        if (fileHandler != null) {
            fileHandler.close();
        }
        if (conn == null) {
            return;
        }
        try {
            conn.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("无法关闭数据库连接:");
            e.printStackTrace();
        }
    }
}
