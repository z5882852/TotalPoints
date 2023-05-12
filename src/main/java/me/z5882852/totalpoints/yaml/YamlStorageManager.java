package me.z5882852.totalpoints.yaml;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class YamlStorageManager {
    private File dataFile;
    public YamlStorageManager(JavaPlugin plugin) {
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
        File file = new File(dataFile, "data.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        Set<String> uuids = config.getKeys(false);

        for (String uuid : uuids) {
            String name = config.getString(uuid + ".name");
            if (name == playerName) {
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

    public void close() {
        dataFile = null;
    }
}
