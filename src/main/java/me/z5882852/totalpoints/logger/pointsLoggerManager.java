package me.z5882852.totalpoints.logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class pointsLoggerManager {
    private JavaPlugin plugin;
    private FileConfiguration config;
    private boolean enableMySQL;
    private boolean enableStackTrace;

    public pointsLoggerManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.enableMySQL = config.getBoolean("mysql.enable", false);
        this.enableStackTrace = config.getBoolean("logger.enable_stackTrace", false);
    }
}
