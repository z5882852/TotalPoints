package me.z5882852.totalpoints.papi;

import org.bukkit.OfflinePlayer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

public class papiExpansion extends PlaceholderExpansion {
    private JavaPlugin plugin;
    private FileConfiguration config;

    public papiExpansion(JavaPlugin plugin) {
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
        if(params.equalsIgnoreCase("points_total")){
            papiManager papiManager = new papiManager(plugin, player);
            int totalPoints = papiManager.getPlayerTotalPoints();
            papiManager.close();
            return String.valueOf(totalPoints);
        }
        Set<String> groups = config.getConfigurationSection("groups").getKeys(false);
        for (String group : groups) {
            int groupId = Integer.parseInt(group);
            if(params.equalsIgnoreCase("reward_group_" + group + "_status")) {
                papiManager papiManager = new papiManager(plugin, player);
                Boolean status = papiManager.getGroupStatus(groupId);
                papiManager.close();
                if (status) {
                    return config.getString("status_receive");
                } else {
                    return config.getString("status_not_receive");
                }
            }
            if(params.equalsIgnoreCase("reward_group_" + group + "_name")) {
                papiManager papiManager = new papiManager(plugin, player);
                String name = papiManager.getGroupName(groupId);
                papiManager.close();
                return  name;
            }
            if(params.equalsIgnoreCase("reward_group_" + group + "_total")) {
                papiManager papiManager = new papiManager(plugin, player);
                String total = papiManager.getGroupName(groupId);
                papiManager.close();
                return  total;
            }
            if(params.equalsIgnoreCase("reward_group_" + group + "_prompt")) {
                papiManager papiManager = new papiManager(plugin, player);
                String prompt = papiManager.getGroupPrompt(groupId);
                papiManager.close();
                return  prompt;
            }
        }
        return null; // Placeholder is unknown by the Expansion
    }
}
