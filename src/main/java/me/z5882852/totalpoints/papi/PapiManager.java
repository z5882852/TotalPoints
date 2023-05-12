package me.z5882852.totalpoints.papi;

import me.z5882852.totalpoints.database.MySQLManager;
import me.z5882852.totalpoints.yaml.YamlStorageManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class PapiManager {
    private JavaPlugin plugin;
    private FileConfiguration config;
    private OfflinePlayer offlinePlayer;
    private boolean enableMySQL;
    private MySQLManager mySQLManager = null;
    private YamlStorageManager yamlStorageManager = null;
    private String playerUUID;

    public PapiManager(JavaPlugin plugin, OfflinePlayer offlinePlayer) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.offlinePlayer = offlinePlayer;
        this.enableMySQL = config.getBoolean("mysql.enable", false);
        this.playerUUID = offlinePlayer.getUniqueId().toString();
        if (enableMySQL) {
            mySQLManager = new MySQLManager(plugin);
        } else {
            yamlStorageManager = new YamlStorageManager(plugin);
        }
    }

    public int getPlayerTotalPoints() {
        int totalPoints = 0;
        if (enableMySQL) {
            totalPoints = mySQLManager.getPlayerTotal(playerUUID);
        } else {
            totalPoints = yamlStorageManager.getPlayerTotal(playerUUID);
        }
        return totalPoints;
    }

    public int getPlayerReward() {
        int rewardId = 0;
        if (enableMySQL) {
            rewardId = mySQLManager.getPlayerReward(playerUUID);
        } else {
            rewardId = yamlStorageManager.getPlayerReward(playerUUID);
        }
        return rewardId;
    }

    public boolean getGroupStatus(int GroupId) {
        return GroupId <= getPlayerReward();
    }

    public String getGroupName(int GroupId) {
        return config.getString("groups." + GroupId + ".name");
    }

    public String getGroupTotal(int GroupId) {
        return config.getString("groups." + GroupId + ".total");
    }

    public String getGroupPrompt(int GroupId) {
        return config.getString("groups." + GroupId + ".prompt");
    }

    public void close() {
        if (enableMySQL) {
            mySQLManager.closeConn();
        } else {
            yamlStorageManager.close();
        }
    }
}
