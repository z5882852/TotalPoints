package me.z5882852.totalpoints.database;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLManager {
    private JavaPlugin plugin;
    private FileConfiguration config;
    private Connection conn;

    public MySQLManager(JavaPlugin plugin) {
        this.plugin = plugin;
        config = plugin.getConfig();
        try {
            String username = config.getString("mysql.user");
            String password = config.getString("mysql.password");
            String url = "jdbc:mysql://" + config.getString("mysql.host") + ":" + config.getString("mysql.port") + "/" + config.getString("mysql.database") + config.getString("mysql.params");
            this.conn = DriverManager.getConnection(url, username, password);
            plugin.getLogger().info("数据库连接成功!");
        } catch (SQLException e) {
            plugin.getLogger().severe("无法连接数据库: " + e.getMessage());
            plugin.getLogger().severe("停用插件: TotalPoints");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
    }
}
