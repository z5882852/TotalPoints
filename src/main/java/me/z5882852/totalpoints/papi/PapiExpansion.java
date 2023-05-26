package me.z5882852.totalpoints.papi;

import org.bukkit.OfflinePlayer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

public class PapiExpansion extends PlaceholderExpansion {
    private JavaPlugin plugin;
    private FileConfiguration config;

    public PapiExpansion(JavaPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    @Override
    public String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public String getIdentifier() {
        return "TotalPoints";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // This is required or else PlaceholderAPI will unregister the Expansion on reload
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if(params.equalsIgnoreCase("points_total")) {
            PapiManager papiManager = new PapiManager(plugin, player);
            int totalPoints = papiManager.getPlayerTotalPoints();
            papiManager.close();
            return String.valueOf(totalPoints);
        }
        if (params.startsWith("points_ranking_")) {
            try {
                int rankingNum = Integer.parseInt(params.split("_")[2]);
                if (rankingNum <= 0) {
                    return null;
                }
                PapiManager papiManager = new PapiManager(plugin, player);
                String ranking = papiManager.getRanking(rankingNum);
                papiManager.close();
                return ranking;
            } catch (Exception e) {
                return null;
            }
        }
        if(params.equalsIgnoreCase("points_rankings")) {
            PapiManager papiManager = new PapiManager(plugin, player);
            String rankings = papiManager.getRankingString();
            papiManager.close();
            return rankings;
        }
        Set<String> groups = config.getConfigurationSection("groups").getKeys(false);
        for (String group : groups) {
            int groupId = Integer.parseInt(group);
            if(params.equalsIgnoreCase("group_" + group + "_status")) {
                PapiManager papiManager = new PapiManager(plugin, player);
                Boolean status = papiManager.getGroupStatus(groupId);
                papiManager.close();
                if (status) {
                    return config.getString("status_receive");
                } else {
                    return config.getString("status_not_receive");
                }
            }
            if(params.equalsIgnoreCase("group_" + group + "_name")) {
                PapiManager papiManager = new PapiManager(plugin, player);
                String name = papiManager.getGroupName(groupId);
                papiManager.close();
                return  name;
            }
            if(params.equalsIgnoreCase("group_" + group + "_total")) {
                PapiManager papiManager = new PapiManager(plugin, player);
                String total = papiManager.getGroupName(groupId);
                papiManager.close();
                return  total;
            }
            if(params.equalsIgnoreCase("group_" + group + "_prompt")) {
                PapiManager papiManager = new PapiManager(plugin, player);
                String prompt = papiManager.getGroupPrompt(groupId);
                papiManager.close();
                return  prompt;
            }
        }
        return null; // Placeholder is unknown by the Expansion
    }
}
