package me.z5882852.totalpoints.command;

import me.clip.placeholderapi.PlaceholderAPI;
import me.z5882852.totalpoints.database.MySQLManager;
import me.z5882852.totalpoints.yaml.YamlStorageManager;
import me.z5882852.totalpoints.TotalPoints;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;


public class PointsCommandExecutor implements CommandExecutor {
    private FileConfiguration config;
    private JavaPlugin plugin;
    private String prefix;
    private boolean enableMySQL;
    private String pointName;

    public PointsCommandExecutor(JavaPlugin plugin){
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.enableMySQL = config.getBoolean("mysql.enable", false);
        this.prefix = ChatColor.translateAlternateColorCodes('&', config.getString("prefix", "&8[&6TotalPoints&8]"));
        this.pointName = config.getString("name", "点券");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender.hasPermission("totalpoints.default.look")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    String player_name = player.getName();
                    int totalPoints = getPlayerTotalPoints(player_name);
                    if (totalPoints == -1) {
                        sender.sendMessage(ChatColor.RED + "没有该玩家的数据！");
                    } else {
                        sender.sendMessage(prefix + ChatColor.GOLD + "你的累计" + pointName + "为" + ChatColor.GREEN + totalPoints);
                    }
                } else {
                    sender.sendMessage(prefix + ChatColor.RED + "你不是玩家。");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "你没有执行该命令的权限。");
            }
            return true;
        }

        if (args.length == 3) {
            if (!checkInteger(args[2])) {
                sender.sendMessage(prefix + ChatColor.RED + "你输入的不是整数。");
                return true;
            }
        }

        switch (args[0].toLowerCase()) {
            case "help":
                if (sender.hasPermission("totalpoints.default.help")) {
                    sender.sendMessage(prefix + getHelpMessage());
                } else {
                    sender.sendMessage(ChatColor.RED + "你没有执行该命令的权限。");
                }
                return true;
            case "look":
                if (sender.hasPermission("totalpoints.default.look")) {
                    int totalPoints = getPlayerTotalPoints(args[1]);
                    if (totalPoints == -1) {
                        sender.sendMessage(ChatColor.RED + "没有该玩家的数据！");
                    } else {
                        sender.sendMessage(prefix + ChatColor.GOLD + "该玩家的累计" + pointName + "为" + ChatColor.GREEN + totalPoints);
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "你没有执行该命令的权限。");
                }
                return true;
            case "add":
                if (sender.hasPermission("totalpoints.default.add")) {
                    sender.sendMessage(prefix + addPlayerTotalPoints(args[1], Integer.parseInt(args[2])));
                } else {
                    sender.sendMessage(ChatColor.RED + "你没有执行该命令的权限。");
                }
                return true;
            case "remove":
                if (sender.hasPermission("totalpoints.admin.remove")) {
                    sender.sendMessage(prefix + removePlayerTotalPoints(args[1], Integer.parseInt(args[2])));
                } else {
                    sender.sendMessage(ChatColor.RED + "你没有执行该命令的权限。");
                }
                return true;
            case "set":
                if (sender.hasPermission("totalpoints.admin.set")) {
                    sender.sendMessage(prefix + setPlayerTotalPoints(args[1], Integer.parseInt(args[2])));
                } else {
                    sender.sendMessage(ChatColor.RED + "你没有执行该命令的权限。");
                }
                return true;
            case "lookgroup":
                if (sender.hasPermission("totalpoints.default.lookgroup")) {
                    int RewardId = getPlayerReward(args[1]);
                    if (RewardId == -1) {
                        sender.sendMessage(ChatColor.RED + "没有该玩家的数据！");
                    } else {
                        sender.sendMessage(prefix + ChatColor.GOLD + "该玩家的已领取的奖励组为" + ChatColor.GREEN + RewardId);
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "你没有执行该命令的权限。");
                }
                return true;
            case "setgroup":
                if (sender.hasPermission("totalpoints.admin.setgroup")) {
                    sender.sendMessage(prefix + setPlayerReward(args[1], Integer.parseInt(args[2])));
                } else {
                    sender.sendMessage(ChatColor.RED + "你没有执行该命令的权限。");
                }
                return true;
            case "get":
                if (sender.hasPermission("totalpoints.default.get")) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(prefix + ChatColor.RED + "你不是玩家。");
                    }
                    if (!checkInteger(args[1])) {
                        sender.sendMessage(prefix + ChatColor.RED + "请输入正确的组名！");
                    }
                    Player player = (Player) sender;
                    String player_name = player.getName();
                    sender.sendMessage(prefix + getReward(player_name,Integer.parseInt(args[1])));
                } else {
                    sender.sendMessage(ChatColor.RED + "你没有执行该命令的权限。");
                }
                return true;
            case "give":
                if (sender.hasPermission("totalpoints.admin.give")) {
                    sender.sendMessage(prefix + givePlayerReward(args[1], Integer.parseInt(args[2]), false));
                } else {
                    sender.sendMessage(ChatColor.RED + "你没有执行该命令的权限。");
                }
                return true;
            case "reload":
                if (sender.hasPermission("totalpoints.admin.reload")) {
                    TotalPoints.thisPlugin.onReload();
                    plugin.reloadConfig();
                    this.config = plugin.getConfig();
                    this.enableMySQL = config.getBoolean("mysql.enable", false);
                    this.prefix = ChatColor.translateAlternateColorCodes('&', config.getString("prefix", "&8[&6TotalPoints&8]"));
                    this.pointName = config.getString("name", "点券");
                    sender.sendMessage(prefix + ChatColor.GREEN + "配置文件已重新加载！");
                } else {
                    sender.sendMessage(ChatColor.RED + "你没有执行该命令的权限。");
                }
                return true;
            default:
                // 处理未知子命令的情况
                return false;
        }
    }

    public String getHelpMessage(){
        String helpMessage = "&6/tpw: 查看自己的累计点数。\n" +
                "&6/tpw look <玩家名>: 查看指定玩家的累计点数。\n" +
                "&6/tpw add <玩家名> <点数>: 增加指定玩家的累计点数。\n" +
                "&6/tpw remove <玩家名> <点数>: 减少指定玩家的累计点数。\n" +
                "&6/tpw set <玩家名> <点数>: 设定指定玩家的累计点数。\n" +
                "&6/tpw get <组名>: 领取指定的奖励组。\n" +
                "&6/tpw give <玩家名> <组名>: 给予指定玩家指定的奖励组。\n" +
                "&6/tpw lookgroup <玩家名>: 查看指定玩家已领取的奖励组。\n" +
                "&6/tpw setgroup <玩家名> <组名>: 设定指定玩家已领取的奖励组。\n" +
                "&6/tpw reload: 重载配置文件。";
        return ChatColor.translateAlternateColorCodes('&', helpMessage);
    }

    public String addPlayerTotalPoints(String playerName, int addTotalPoints) {
        if (enableMySQL) {
            MySQLManager mySQLManager = new MySQLManager(plugin);
            String playerUUID = mySQLManager.getPlayerUUID(playerName);
            if (playerUUID == null) {
                mySQLManager.closeConn();
                return ChatColor.RED + "没有该玩家的数据！";
            }
            mySQLManager.setPlayerTotal(playerUUID, mySQLManager.getPlayerTotal(playerUUID) + addTotalPoints);
            mySQLManager.closeConn();
        } else {
            YamlStorageManager yamlStorageManager = new YamlStorageManager(plugin);
            String playerUUID = yamlStorageManager.getPlayerUUID(playerName);
            if (playerUUID == null) {
                yamlStorageManager.close();
                return ChatColor.RED + "没有该玩家的数据！";
            }
            yamlStorageManager.setPlayerTotal(playerUUID, yamlStorageManager.getPlayerTotal(playerUUID) + addTotalPoints);
            yamlStorageManager.close();
        }
        return ChatColor.GREEN + "设置成功！";
    }

    public String removePlayerTotalPoints(String playerName, int removeTotalPoints) {
        if (enableMySQL) {
            MySQLManager mySQLManager = new MySQLManager(plugin);
            String playerUUID = mySQLManager.getPlayerUUID(playerName);
            if (playerUUID == null) {
                mySQLManager.closeConn();
                return ChatColor.RED + "没有该玩家的数据！";
            }
            mySQLManager.setPlayerTotal(playerUUID, mySQLManager.getPlayerTotal(playerUUID) - removeTotalPoints);
            mySQLManager.closeConn();
        } else {
            YamlStorageManager yamlStorageManager = new YamlStorageManager(plugin);
            String playerUUID = yamlStorageManager.getPlayerUUID(playerName);
            if (playerUUID == null) {
                yamlStorageManager.close();
                return ChatColor.RED + "没有该玩家的数据！";
            }
            yamlStorageManager.setPlayerTotal(playerUUID, yamlStorageManager.getPlayerTotal(playerUUID) - removeTotalPoints);
            yamlStorageManager.close();
        }
        return ChatColor.GREEN + "设置成功！";
    }

    public String setPlayerTotalPoints(String playerName, int setTotalPoints) {
        if (enableMySQL) {
            MySQLManager mySQLManager = new MySQLManager(plugin);
            String playerUUID = mySQLManager.getPlayerUUID(playerName);
            if (playerUUID == null) {
                mySQLManager.closeConn();
                return ChatColor.RED + "没有该玩家的数据！";
            }
            mySQLManager.setPlayerTotal(playerUUID, setTotalPoints);
            mySQLManager.closeConn();
        } else {
            YamlStorageManager yamlStorageManager = new YamlStorageManager(plugin);
            String playerUUID = yamlStorageManager.getPlayerUUID(playerName);
            if (playerUUID == null) {
                yamlStorageManager.close();
                return ChatColor.RED + "没有该玩家的数据！";
            }
            yamlStorageManager.setPlayerTotal(playerUUID, setTotalPoints);
            yamlStorageManager.close();
        }
        return ChatColor.GREEN + "设置成功！";
    }

    public int getPlayerTotalPoints(String playerName) {
        int playerTotalPoints= 0;
        if (enableMySQL) {
            MySQLManager mySQLManager = new MySQLManager(plugin);
            String playerUUID = mySQLManager.getPlayerUUID(playerName);
            if (playerUUID == null) {
                mySQLManager.closeConn();
                return -1;
            }
            playerTotalPoints = mySQLManager.getPlayerTotal(playerUUID);
            mySQLManager.closeConn();
        } else {
            YamlStorageManager yamlStorageManager = new YamlStorageManager(plugin);
            String playerUUID = yamlStorageManager.getPlayerUUID(playerName);
            if (playerUUID == null) {
                yamlStorageManager.close();
                return -1;
            }
            playerTotalPoints = yamlStorageManager.getPlayerTotal(playerUUID);
            yamlStorageManager.close();
        }
        return playerTotalPoints;
    }

    public int getPlayerReward(String playerName) {
        int playerReward = 0;
        if (enableMySQL) {
            MySQLManager mySQLManager = new MySQLManager(plugin);
            String playerUUID = mySQLManager.getPlayerUUID(playerName);
            if (playerUUID == null) {
                mySQLManager.closeConn();
                return -1;
            }
            playerReward = mySQLManager.getPlayerReward(playerUUID);
            mySQLManager.closeConn();
        } else {
            YamlStorageManager yamlStorageManager = new YamlStorageManager(plugin);
            String playerUUID = yamlStorageManager.getPlayerUUID(playerName);
            if (playerUUID == null) {
                yamlStorageManager.close();
                return -1;
            }
            playerReward = yamlStorageManager.getPlayerReward(playerUUID);
            yamlStorageManager.close();
        }
        return playerReward;
    }

    public String setPlayerReward(String playerName, int groupId) {
        if (enableMySQL) {
            MySQLManager mySQLManager = new MySQLManager(plugin);
            String playerUUID = mySQLManager.getPlayerUUID(playerName);
            if (playerUUID == null) {
                mySQLManager.closeConn();
                return ChatColor.RED + "没有该玩家的数据！";
            }
            mySQLManager.setPlayerReward(playerUUID, groupId);
            mySQLManager.closeConn();
        } else {
            YamlStorageManager yamlStorageManager = new YamlStorageManager(plugin);
            String playerUUID = yamlStorageManager.getPlayerUUID(playerName);
            if (playerUUID == null) {
                yamlStorageManager.close();
                return ChatColor.RED + "没有该玩家的数据！";
            }
            yamlStorageManager.setPlayerReward(playerUUID, groupId);
            yamlStorageManager.close();
        }
        return ChatColor.GREEN + "设置成功！";
    }

    public String getReward(String playerName, int groupId) {
        int playerTotalPoints = getPlayerTotalPoints(playerName);
        int RewardId = getPlayerReward(playerName);
        if (RewardId == -1) {
            return ChatColor.RED + "没有玩家的数据！";
        }
        if (playerTotalPoints == -1) {
            return ChatColor.RED + "没有玩家的数据！";
        }
        if (!checkGroup(groupId)) {
            return ChatColor.RED + "请输入正确的组名！";
        }
        if (groupId <= RewardId) {
            return ChatColor.RED + "你已经领取过该奖励组！";
        }
        int groupConditionPoints = config.getInt("groups." + groupId + ".total", -1);
        if (groupConditionPoints == -1) {
            return ChatColor.RED + "奖励组错误: 该奖励组没有设置数额！";
        }
        if (playerTotalPoints < groupConditionPoints) {
            return ChatColor.RED + "你没有达到该奖励组的领取条件！";
        }
        return givePlayerReward(playerName, groupId, true);
    }

    public String givePlayerReward(String playerName, int groupId, boolean isSetReward) {
        String uuid;
        if (!checkGroup(groupId)) {
            return ChatColor.RED + "请输入正确的组名！";
        }
        if (enableMySQL) {
            MySQLManager mySQLManager = new MySQLManager(plugin);
            uuid = mySQLManager.getPlayerUUID(playerName);
            if (uuid == null) {
                mySQLManager.closeConn();
                return ChatColor.RED + "没有该玩家的数据！";
            }
            mySQLManager.closeConn();
        } else {
            YamlStorageManager yamlStorageManager = new YamlStorageManager(plugin);
            uuid = yamlStorageManager.getPlayerUUID(playerName);
            if (uuid == null) {
                yamlStorageManager.close();
                return ChatColor.RED + "没有该玩家的数据！";
            }
            yamlStorageManager.close();
        }
        UUID playerUUID = UUID.fromString(uuid);
        Player player = Bukkit.getPlayer(playerUUID);
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
        String prompt = config.getString("groups." + groupId + ".prompt", "");
        List<String> commands = config.getStringList("groups." + groupId + ".commands");
        for (String command : commands) {
            if (TotalPoints.thisPlugin.papiOnLoad) {
                command = PlaceholderAPI.setPlaceholders(offlinePlayer, command);
            }
            command = command.replace("{player_name}", offlinePlayer.getName());
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
        }
        if (player != null && prompt != "") {
            prompt = ChatColor.translateAlternateColorCodes('&', prompt);
            player.sendMessage(prefix + prompt);
        }
        if (isSetReward) {
            if (enableMySQL) {
                MySQLManager sqlManager = new MySQLManager(plugin);
                sqlManager.setPlayerReward(uuid, groupId);
                sqlManager.closeConn();
            } else {
                YamlStorageManager storageManager = new YamlStorageManager(plugin);
                storageManager.setPlayerReward(uuid, groupId);
                storageManager.close();
            }
        }
        return ChatColor.GREEN + "给予玩家 " + playerName + " 奖励成功！";
    }

    public boolean checkGroup(int groupId) {
        Set<String> groups = config.getConfigurationSection("groups").getKeys(false);
        List<Integer> groupIds = new ArrayList<>();
        for (String group : groups) {
            int id = Integer.parseInt(group);
            groupIds.add(id);
        }
        if (groupIds.size() == 0) {
            return false;
        }
        if (groupIds.contains(groupId)) {
            return true;
        }
        return false;
    }

    public boolean checkInteger(String number) {
        try {
            int amount = Integer.parseInt(number);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
