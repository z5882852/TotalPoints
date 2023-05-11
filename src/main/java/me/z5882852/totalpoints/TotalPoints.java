package me.z5882852.totalpoints;

import me.z5882852.totalpoints.database.MySQLManager;
import me.z5882852.totalpoints.database.MySQLTest;
import me.z5882852.totalpoints.yaml.yamlStorageManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import org.black_ixx.playerpoints.event.PlayerPointsChangeEvent;

public class TotalPoints extends JavaPlugin implements Listener {
    private boolean enableMySQL;
    private boolean enablePlugin;
    private FileConfiguration cfg;
    private String pointName;

    public void onEnable() {
        getLogger().info("插件正在初始化中...");
        saveDefaultConfig();
        loadDataFile();
        cfg = this.getConfig();
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        enablePlugin = cfg.getBoolean("enable", false);
        enableMySQL = cfg.getBoolean("mysql.enable", false);
        pointName = cfg.getString("name", "点券");
        Set<String> keys = getConfig().getConfigurationSection("groups").getKeys(false);
        for (String key : keys) {
            getLogger().info(key);
        }
        if (!enablePlugin) {
            getLogger().warning("配置文件未启用该插件。");
            this.getServer().getPluginManager().disablePlugin(this);
        }
        if (enableMySQL) {
            new MySQLTest(this);
        }
        getLogger().info("插件加载完成。");
    }

    public void onDisable() {
        getLogger().info("插件卸载完成。");
    }

    public void loadDataFile() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        File file = new File(getDataFolder(), "data.yml");
        if (!file.exists()) {
            getLogger().info("data.yml不存在,正在创建data.yml");
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // 玩家登录时检测玩家points余额
        if (!enablePlugin) {
            return;
        }
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String playerName = player.getName();
        if (enableMySQL) {
            MySQLManager sqlManager = new MySQLManager(this);
            if (sqlManager.getPlayerName(uuid.toString()) == null) {
                sqlManager.insertPlayerData(uuid.toString(), playerName);
            }
            sqlManager.closeConn();
        } else {
            yamlStorageManager storageManager = new yamlStorageManager(this);
            if (storageManager.getPlayerName(uuid.toString()) == null) {
                storageManager.addPlayerData(uuid.toString(), playerName);
            }
            storageManager.close();
        }
        checkPoints(uuid.toString(), player);
    }

    @EventHandler
    public void onPlayerPointsChange(PlayerPointsChangeEvent event){
        if (!enablePlugin) {
            return;
        }
        int change = event.getChange();
        UUID uuid = event.getPlayerId();
        String playerName = Bukkit.getOfflinePlayer(uuid).getName();
        Player player = Bukkit.getPlayer(uuid);
        getLogger().info(String.format("玩家'%s'%s发生变化: %d", playerName, pointName, change));
        if(change > 0) {
            //获得点券
            if (enableMySQL) {
                MySQLManager sqlManager = new MySQLManager(this);
                int newTotalPoints = sqlManager.getPlayerTotal(uuid.toString()) + change;
                sqlManager.setPlayerTotal(uuid.toString(), newTotalPoints);
                sqlManager.closeConn();
            } else {
                yamlStorageManager storageManager = new yamlStorageManager(this);
                int newTotalPoints = storageManager.getPlayerTotal(uuid.toString()) + change;
                storageManager.setPlayerTotal(uuid.toString(), newTotalPoints);
                storageManager.close();
            }
            checkPoints(uuid.toString(), player);
        } else {
            //失去点券
        }
    }

    public void checkPoints(String uuid, Player player){
        getLogger().info("...");

    }

}