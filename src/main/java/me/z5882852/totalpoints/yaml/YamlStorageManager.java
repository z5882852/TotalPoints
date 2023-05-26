package me.z5882852.totalpoints.yaml;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class YamlStorageManager {
    private File dataFile;
    private JavaPlugin plugin;
    public YamlStorageManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
    }

    public void addPlayerData(String playerUUID, String playerName) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        config.set(playerUUID + ".name", playerName);
        config.set(playerUUID + ".total", 0);
        config.set(playerUUID + ".reward", 0);
        try {
            config.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getPlayerName(String playerUUID) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        return config.getString(playerUUID + ".name", null);
    }

    public int getPlayerTotal(String playerUUID) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        return config.getInt(playerUUID + ".total");
    }

    public int getPlayerReward(String playerUUID) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        return config.getInt(playerUUID + ".reward");
    }

    public String getPlayerUUID(String playerName) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);

        Set<String> uuids = config.getKeys(false);
        for (String uuid : uuids) {
            String name = config.getString(uuid + ".name");
            if (name.equals(playerName)) {
                return uuid;
            }
        }
        return null;
    }

    public void setPlayerTotal(String playerUUID, int totalPoints) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        config.set(playerUUID + ".total", totalPoints);
        try {
            config.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setPlayerReward(String playerUUID, int RewardId) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        config.set(playerUUID + ".reward", RewardId);
        try {
            config.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getRanking() {
        Map<String, Integer> playerData = new HashMap<>();
        List<String> rankings = new ArrayList<>();
        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        Set<String> uuids = config.getKeys(false);
        for (String uuid : uuids) {
            String name = config.getString(uuid + ".name");
            int total = config.getInt(uuid + ".total", 0);
            if ((total == 0)) {
                continue;
            }
            playerData.put(name, total);
        }
        Map<String, Integer> sortPlayerData = sortMapByValue(playerData);
        int r = 1;
        for (Map.Entry<String, Integer> entry : sortPlayerData.entrySet()) {
            String playerName = entry.getKey();
            String playerTotal = String.valueOf(entry.getValue());
            String ranking = this.plugin.getConfig().getString("rankings_format", "玩家 {player_name} 累计充值 {player_total} 点券").replace("{ranking}", String.valueOf(r)).replace("{player_name}", playerName).replace("{player_total}", playerTotal);
            rankings.add(ranking);
            r++;
        }
        return rankings;
    }

    public static Map<String, Integer> sortMapByValue(Map<String, Integer> map) {
        List<Map.Entry<String, Integer>> entryList = new ArrayList<>(map.entrySet());
        entryList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        Map<String, Integer> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : entryList) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    public void close() {
        dataFile = null;
    }
}
