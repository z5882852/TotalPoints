package me.z5882852.totalpoints.papi;

import me.z5882852.totalpoints.database.MySQLManager;
import me.z5882852.totalpoints.yaml.YamlStorageManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public List<String> getRankings() {
        if (enableMySQL) {
            return mySQLManager.getRanking();
        } else {
            return yamlStorageManager.getRanking();
        }
    }

    public String getPlayerRanking(String playerName) {
        List<Map<String, String>> ranking;
        if (enableMySQL) {
            ranking = mySQLManager.getPlayerRanking();
        } else {
            ranking = yamlStorageManager.getPlayerRanking();
        }
        int r = 1;
        for (Map<String, String> map : ranking) {
            String total = map.getOrDefault(playerName, null);
            if (total != null) {
                return this.plugin.getConfig()
                        .getString("rankings_format", "玩家 {player_name} 累计充值 {player_total} 点券")
                        .replace("{ranking}", String.valueOf(r))
                        .replace("{player_name}", playerName)
                        .replace("{player_total}", total);
            }
            r++;
        }

        return null;
    }



    public String getRanking(int rankingNum) {
        List<String> rankings = getRankings();
        if (rankings.size() < rankingNum) {
            return "null";
        }
        return rankings.get(rankingNum - 1);
    }

    public String getRankingString() {
        List<String> rankings = getRankings();
        if (rankings.size() == 0) {
            return null;
        }
        String rankingString = "";
        Integer rankingsNum = this.config.getInt("rankings_number", 10);
        for (String ranking : rankings) {
            if (rankingsNum == 0) {
                break;
            }
            rankingString = rankingString + ranking + "\n";
            rankingsNum--;
        }
        return rankingString;
    }

    public void close() {
        if (enableMySQL) {
            mySQLManager.closeConn();
        } else {
            yamlStorageManager.close();
        }
    }
}
