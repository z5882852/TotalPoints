package me.z5882852.totalpoints.database;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.sql.*;

public class MySQLManager {
    private JavaPlugin plugin;
    private FileConfiguration config;
    private Connection conn;
    private String table;
    private String pointName;


    public MySQLManager(JavaPlugin plugin) {
        this.plugin = plugin;
        config = plugin.getConfig();
        try {
            String username = config.getString("mysql.user");
            String password = config.getString("mysql.password");
            table = config.getString("mysql.table");
            pointName = config.getString("name", "点券");
            String url = "jdbc:mysql://" + config.getString("mysql.host") + ":" + config.getString("mysql.port") + "/" + config.getString("mysql.database") + config.getString("mysql.params");
            this.conn = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            plugin.getLogger().severe("无法连接数据库: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void insertPlayerData(String playerUUID, String playerName) {
        try {
            Statement statement = conn.createStatement();
            String sql = String.format("INSERT INTO `%s` (`uuid`, `name`, `total`, `reward`) VALUES ('%s', '%s', '0', '0');", table, playerUUID, playerName);
            statement.executeUpdate(sql);
            statement.close();
            plugin.getLogger().info("创建玩家数据成功。");
        } catch (SQLException e) {
            plugin.getLogger().severe("创建玩家数据失败,请查看以下报错信息:");
            e.printStackTrace();
        }
    }

    public String getPlayerName(String playerUUID) {
        String playerName = null;
        try {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(String.format("SELECT `name` FROM `%s` WHERE `uuid` = '%s'", table, playerUUID));
            if (rs.next()) {
                playerName = rs.getString("name");
            }
            rs.close();
            statement.close();
            return playerName;
        } catch (SQLException e) {
            plugin.getLogger().severe("无法获取玩家名称,请查看以下报错信息:");
            e.printStackTrace();
            return playerName;
        }
    }

    public int getPlayerTotal(String playerUUID) {
        int totalPoints = 0;
        try {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(String.format("SELECT `total` FROM `%s` WHERE `uuid` = '%s'", table, playerUUID));
            if (rs.next()) {
                totalPoints = rs.getInt("total");
            }
            rs.close();
            statement.close();
            return totalPoints;
        } catch (SQLException e) {
            plugin.getLogger().severe(String.format("无法获取玩家累计%s,请查看以下报错信息:", pointName));
            e.printStackTrace();
            return totalPoints;
        }
    }

    public int getPlayerReward(String playerUUID) {
        int totalReward = 0;
        try {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(String.format("SELECT `reward` FROM `%s` WHERE `uuid` = '%s'", table, playerUUID));
            if (rs.next()) {
                totalReward = rs.getInt("reward");
            }
            rs.close();
            statement.close();
            return totalReward;
        } catch (SQLException e) {
            plugin.getLogger().severe("无法获取玩家已获得奖励,请查看以下报错信息:");
            e.printStackTrace();
            return totalReward;
        }
    }

    public void setPlayerTotal(String playerUUID, int totalPoints) {
        try {
            Statement stmt = conn.createStatement();
            String sql = String.format("UPDATE `%s` SET `total`='%d' WHERE `uuid` = '%s';", table, totalPoints, playerUUID);
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe(String.format("无法更新玩家累计%s,请查看以下报错信息:", pointName));
        }
    }

    public void setPlayerReward(String playerUUID, int RewardId) {
        try {
            Statement stmt = conn.createStatement();
            String sql = String.format("UPDATE `%s` SET `reward`='%d' WHERE `uuid` = '%s';", table, RewardId, playerUUID);
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe(String.format("无法更新玩家累计%s,请查看以下报错信息:", pointName));
        }
    }

    public void closeConn() {
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
