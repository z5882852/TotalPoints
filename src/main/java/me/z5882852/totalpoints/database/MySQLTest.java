package me.z5882852.totalpoints.database;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;

public class MySQLTest {
    private JavaPlugin plugin;
    private FileConfiguration config;
    private Connection conn;
    private String table;


    public MySQLTest(JavaPlugin plugin) {
        this.plugin = plugin;
        config = plugin.getConfig();

        try {
            plugin.getLogger().info("数据库连接测试...");
            String username = config.getString("mysql.user");
            String password = config.getString("mysql.password");
            table = config.getString("mysql.table");
            String url = "jdbc:mysql://" + config.getString("mysql.host") + ":" + config.getString("mysql.port") + "/" + config.getString("mysql.database") + config.getString("mysql.params");
            this.conn = DriverManager.getConnection(url, username, password);
            plugin.getLogger().info("数据库连接成功!");
            // 检查是否存在表
            Statement statement = conn.createStatement();
            String sql = String.format("SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_NAME = '%s'", table);
            ResultSet resultSet = statement.executeQuery(sql);
            resultSet.next();
            int count = resultSet.getInt(1);
            // 如果表不存在，则创建一个新表
            if (count == 0) {
                plugin.getLogger().info("检测到表不存在，正在创建新的表...");
                sql = String.format("CREATE TABLE `%s` (", table) +
                        "  `id` int NOT NULL AUTO_INCREMENT," +
                        "  `uuid` varchar (36) NOT NULL DEFAULT ''," +
                        "  `name` text NOT NULL," +
                        "  `total` double NOT NULL DEFAULT 0," +
                        "  `reward` int NOT NULL DEFAULT 0," +
                        "  PRIMARY KEY (`id`)" +
                        ") ENGINE = innodb";
                statement.executeUpdate(sql);
                plugin.getLogger().info("数据表创建成功!");
            }
            resultSet.close();
            statement.close();
            conn.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("无法连接数据库: " + e.getMessage());
            plugin.getLogger().severe("停用插件: TotalPoints");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
    }


}
