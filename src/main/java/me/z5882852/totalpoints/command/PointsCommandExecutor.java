package me.z5882852.totalpoints.command;

import me.z5882852.totalpoints.database.MySQLManager;
import me.z5882852.totalpoints.yaml.YamlStorageManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

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
                if (sender.hasPermission("totalpoints.default.remove")) {
                    sender.sendMessage(prefix + removePlayerTotalPoints(args[1], Integer.parseInt(args[2])));
                } else {
                    sender.sendMessage(ChatColor.RED + "你没有执行该命令的权限。");
                }
                return true;
            case "set":
                if (sender.hasPermission("totalpoints.default.set")) {
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
                if (sender.hasPermission("totalpoints.default.setgroup")) {
                    sender.sendMessage(prefix + setPlayerReward(args[1], Integer.parseInt(args[2])));
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
        String helpMessage =  "\n&6/tpw: 查看自己的累计点数。\n" +
                "&6/tpw look <玩家名>: 查看指定玩家的累计点数。\n" +
                "&6/tpw add <玩家名> <点数>: 增加指定玩家的累计点数。\n" +
                "&6/tpw remove <玩家名> <点数>: 减少指定玩家的点数。\n" +
                "&6/tpw set <玩家名> <点数>: 设定指定玩家的点数。\n" +
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

    public boolean checkInteger(String number) {
        try {
            int amount = Integer.parseInt(number);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
