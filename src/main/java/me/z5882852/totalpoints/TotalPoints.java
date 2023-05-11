package me.z5882852.totalpoints;

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
        // 查询玩家points余额
        if (!enablePlugin) {
            return;
        }
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String playerName = player.getName();
        if (enableMySQL) {

        } else {
            yamlStorageManager storageManager = new yamlStorageManager(this);
            if (storageManager.getPlayerName(uuid.toString()) == null) {
                storageManager.addPlayerData(uuid.toString(), playerName);
                storageManager.close();
            } else {
                int totalPoints = storageManager.getPlayerTotal(uuid.toString());
                storageManager.close();
                CheckPoints(totalPoints, totalPoints, uuid.toString(), player);
            }
        }
    }

    @EventHandler
    public void onPlayerPointsChange(PlayerPointsChangeEvent event){
        if (!enablePlugin) {
            return;
        }
        int change = event.getChange();
        UUID uuid = event.getPlayerId();
        Player player = Bukkit.getPlayer(uuid);

        if(change > 0) {

        } else {
            //失去点券
        }
    }

    public void CheckPoints(int value,int history_value, String uuid, Player player){
        getLogger().info("玩家" + uuid + "的累计充值为" + value);
        player.sendMessage(String.format("累计%s为", pointName) + history_value);
    }

}