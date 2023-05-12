package me.z5882852.totalpoints;

import me.z5882852.totalpoints.database.MySQLManager;
import me.z5882852.totalpoints.database.MySQLTest;
import me.z5882852.totalpoints.logger.PointsLoggerManager;
import me.z5882852.totalpoints.papi.papiExpansion;
import me.z5882852.totalpoints.yaml.YamlStorageManager;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;
import java.io.IOException;
import java.util.*;

import me.clip.placeholderapi.PlaceholderAPI;
import org.black_ixx.playerpoints.event.PlayerPointsResetEvent;
import org.black_ixx.playerpoints.event.PlayerPointsChangeEvent;

public class TotalPoints extends JavaPlugin implements Listener {
    private boolean enableMySQL;
    private boolean enablePlugin;
    private boolean enablePapi;
    private FileConfiguration cfg;
    private String pointName;

    public void onEnable() {
        getLogger().info("插件正在初始化中...");

        saveDefaultConfig();
        loadDataFile();
        Bukkit.getServer().getPluginManager().registerEvents(this, this);

        cfg = this.getConfig();
        enablePlugin = cfg.getBoolean("enable", false);
        enableMySQL = cfg.getBoolean("mysql.enable", false);
        enablePapi = cfg.getBoolean("enable_papi", false);
        pointName = cfg.getString("name", "点券");

        if (!enablePlugin) {
            getLogger().warning("配置文件未启用该插件。");
            this.getServer().getPluginManager().disablePlugin(this);
        }
        if (enablePapi) {
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
                getLogger().warning("找不到前置 PlaceholderAPI!");
            } else {
                this.getLogger().info("PlaceholderAPI变量注册中");
                new papiExpansion(this).register();
            }
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
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        String playerName = player.getName();
        if (enableMySQL) {
            MySQLManager sqlManager = new MySQLManager(this);
            if (sqlManager.getPlayerName(uuid.toString()) == null) {
                sqlManager.insertPlayerData(uuid.toString(), playerName);
            }
            sqlManager.closeConn();
        } else {
            YamlStorageManager storageManager = new YamlStorageManager(this);
            if (storageManager.getPlayerName(uuid.toString()) == null) {
                storageManager.addPlayerData(uuid.toString(), playerName);
            }
            storageManager.close();
        }
        checkPoints(uuid.toString(), player, offlinePlayer);
    }

    @EventHandler
    public void onPlayerPointsChange(PlayerPointsChangeEvent event){
        if (!enablePlugin) {
            return;
        }
        int change = event.getChange();

        UUID uuid = event.getPlayerId();
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        Player player = Bukkit.getPlayer(uuid);
        if (cfg.getBoolean("logger.enable")) {
            PointsLoggerManager pointsLoggerManager = new PointsLoggerManager(this);
            pointsLoggerManager.log(offlinePlayer, change);
            pointsLoggerManager.close();
        }
        if(change > 0) {
            //获得Points
            if (enableMySQL) {
                MySQLManager sqlManager = new MySQLManager(this);
                int newTotalPoints = sqlManager.getPlayerTotal(uuid.toString()) + change;
                sqlManager.setPlayerTotal(uuid.toString(), newTotalPoints);
                sqlManager.closeConn();
            } else {
                YamlStorageManager storageManager = new YamlStorageManager(this);
                int newTotalPoints = storageManager.getPlayerTotal(uuid.toString()) + change;
                storageManager.setPlayerTotal(uuid.toString(), newTotalPoints);
                storageManager.close();
            }
            checkPoints(uuid.toString(), player, offlinePlayer);
        }
    }

    @EventHandler
    public void onPlayerPointsReset(PlayerPointsResetEvent event){
        if (cfg.getBoolean("logger.enable")) {
            UUID uuid = event.getPlayerId();
            PointsLoggerManager pointsLoggerManager = new PointsLoggerManager(this);
            pointsLoggerManager.log(Bukkit.getOfflinePlayer(uuid), event.getChange());
            pointsLoggerManager.close();
        }
    }

    public void checkPoints(String uuid, Player player, OfflinePlayer offlinePlayer){
        if (!cfg.getBoolean("enable_reward")) {
            return;
        }
        Set<String> groups = cfg.getConfigurationSection("groups").getKeys(false);
        boolean enableContinuousExecution = cfg.getBoolean("enable_continuous_execution");
        if (!cfg.getBoolean("enable_offline_execution") && player == null) {
            return;
        }
        int totalPoints = 0;
        int rewardId = 0;
        if (enableMySQL) {
            MySQLManager sqlManager = new MySQLManager(this);
            totalPoints = sqlManager.getPlayerTotal(uuid);
            rewardId = sqlManager.getPlayerReward(uuid);
            sqlManager.closeConn();
        } else {
            YamlStorageManager storageManager = new YamlStorageManager(this);
            totalPoints = storageManager.getPlayerTotal(uuid);
            rewardId = storageManager.getPlayerReward(uuid);
            storageManager.close();
        }
        List<Integer> executionGroupId = new ArrayList<>();
        for (String group : groups) {
            int groupConditionPoints = cfg.getInt("groups." + group + ".total");
            int groupId = Integer.parseInt(group);
            if (totalPoints >= groupConditionPoints && rewardId < groupId) {
                executionGroupId.add(groupId);
            }
        }
        if (executionGroupId.size() == 0) {
            return;
        }
        if (!enableContinuousExecution) {
            executionGroupId = new ArrayList<>(Collections.max(executionGroupId));
        }
        for (int groupId : executionGroupId) {
            String prompt = cfg.getString("groups." + groupId + ".prompt", "");
            List<String> commands = getConfig().getStringList("groups." + groupId + ".commands");
            for (String command : commands) {
                command = PlaceholderAPI.setPlaceholders(offlinePlayer, command);
                command = command.replace("{player_name}", offlinePlayer.getName());
                getServer().dispatchCommand(getServer().getConsoleSender(), command);
            }
            if (player != null && prompt != "") {
                prompt = ChatColor.translateAlternateColorCodes('&', prompt);
                player.sendMessage(prompt);
            }
        }
        if (enableMySQL) {
            MySQLManager sqlManager = new MySQLManager(this);
            sqlManager.setPlayerReward(uuid, Collections.max(executionGroupId));
            sqlManager.closeConn();
        } else {
            YamlStorageManager storageManager = new YamlStorageManager(this);
            storageManager.setPlayerReward(uuid, Collections.max(executionGroupId));
            storageManager.close();
        }
    }

}