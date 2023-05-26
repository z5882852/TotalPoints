# TotalPoints
![Author](https://img.shields.io/badge/Author-z5882852-blue) ![Version](https://img.shields.io/github/v/release/z5882852/TotalPoints?label=Version) ![](https://img.shields.io/badge/Bukkit/Spigot-1.12.2-blue.svg)

TotalPoints是一款基于PlayerPoints插件的我的世界Bukkit插件，用于记录玩家的累积点数，自定义以及自动发放奖励，提供papi变量和记录点数变化功能。

## 安装

要安装TotalPoints插件，请按照以下步骤操作：

1. 请确保服务器已经安装`PlayerPoints`，如果没有，请下载并安装`PlayerPoints`。
2. 如果需要启用PAPI变量，请安装`PlaceholderAPI`插件，并启用对应的变量扩展。
3. 下载`TotalPoints`插件的jar文件。
4. 将`TotalPoints`插件的jar文件复制到服务器的plugins目录下。
5. 重新启动服务器。

## 配置

TotalPoints插件有以下配置选项：

```yaml
# 配置文件版本号，请勿修改
version: 1.25

# Points别名,为了方便以下均称为“点券”
name: '点券'

# 是否启用插件
enable: true

# 是否启用奖励组
# 本插件主要功能就是这个，当然你想用其他功能（比如papi或者记录）那就随你(
enable_reward: true

# 是否启用 PlaceholderAPI 变量
enable_papi: true

# 是否启用连续执行
# 启用时，当一次性满足多个奖励组的条件时，依次执行每个组的命令。禁用时，则执行条件最大的组命令
enable_continuous_execution: true

# 是否启用离线执行
# 启用时，即使玩家离线，满足奖励组条件时仍然执行命令。禁用时，玩家在线才执行命令。
# 当你的命令必须要求玩家在线时，建议禁用
enable_offline_execution: false

# 累计点券奖励组
groups:
   # 奖励组名称,请按顺序使用正整数来命名，例如 1-10等
   1:
      # 该奖励组名称
      name: "累充100礼包"
      # 触发条件: >= total
      total: 100
      # 达成条件后要执行的控制台命令，玩家名变量‘{player_name}’
      # 允许使用PlaceholderAPI变量'%player_name%'获取玩家名(前提存在'%player_name%'变量)
      commands:
         - "例如eco give {player_name} 200"
      # 指令执行完成后的提示, 留空或者删除则不提示
      # 允许使用颜色转义字符，比如'&1'和'&3'等
      prompt: "&6累充100&4礼包领取成功"
   2:
      name: "XXX"
      total: 200
      commands:
         - "XXX"
      prompt: "XXX"

# 插件信息输出前缀
prefix: "&8[&6TotalPoints&8]"

# Points变化记录
# 格式[时间] [玩家UUID] [玩家名字] [变化类型] [变化数值] \n[StackTraceClassName_1, \nStackTraceClassName_2, \n...]
logger:
   # 是否启用点券变化记录
   enable: false
   # 记录方式，填"mysql" 或者 “local”
   type: local
   # 是否记录堆栈跟踪信息
   # 记录堆栈跟踪信息可以更好溯源点券变化来源(比如可以查到刷点券具体是什么插件导致的，前提是你看得懂)，但是会增加日志大小
   enable_stackTrace: true
   # 简化堆栈跟踪，如果关闭则输出所有StackTraceClassName
   simple_stackTrace: true
   # 记录在mysql的表名称（记录方式为"mysql"时生效）
   logger_table: PointsChange

# 数据库配置
mysql:
   # 是否启用数据库
   enable: false
   host: localhost
   port: 3306
   user: root
   password: ""
   database: database
   #  保存数据的表，不存在则创建表
   table: TotalPoints
   # 连接参数
   params: "?useSSL=false"


# PAPI变量设置
# %reward_group_{组名}_status% 解析返回的变量
status_receive: "已领取"
status_not_receive: "未领取"

# %points_rankings% papi输出的最大排行数
rankings_number: 10
# papi输出的排名格式,{ranking}排名 {player_name}为玩家名, {player_total}为累计获得点券数量
rankings_format: "{ranking}.玩家 {player_name} 累计充值 {player_total} 点券"
# 默认排名显示(当累计点券为0时)
default_ranking: "null"
```

要更改配置选项，请编辑config.yml文件。

## 使用方法

该插件提供以下命令：

* `/tpw`: 查看自己的累计点数。
* `/tpw look <玩家名>`: 查看指定玩家的累计点数。
* `/tpw add <玩家名> <点数>`: 增加指定玩家的累计点数。
* `/tpw remove <玩家名> <点数>`: 减少指定玩家的累计点数。
* `/tpw set <玩家名> <点数>`: 设定指定玩家的累计点数。
* `/tpw lookgroup <玩家名>`: 查看指定玩家已领取的奖励组。
* `/tpw setgroup <玩家名> <组名>`: 设定指定玩家已领取的奖励组。
* `/tpw reload`: 重载配置文件。

除此之外，`TotalPoints`插件还提供以下`PlaceholderAPI`变量：

* `%TotalPoints_points_total%`: 玩家的累积点数。
* `%TotalPoints_points_rankings%`: 玩家的累积点数排行榜。
* `%TotalPoints_points_ranking_{排名}%`: 指定排名的玩家信息。
* `%TotalPoints_group_{组名}_status%`: 玩家的奖励组领取状态。
* `%TotalPoints_group_{组名}_name%`: 该奖励组的名称。
* `%TotalPoints_group_{组名}_total%`: 该奖励组的的领取条。
* `%TotalPoints_group_{组名}_prompt%`: 该奖励组的领取后的提示。

## 常见问题

1. `TotalPoints`插件需要安装哪些前置插件？

    `TotalPoints`插件需要安装`PlayerPoints`插件才能正常运行。

2. `PlaceholderAPI`插件是否为必需的前置插件？

    `PlaceholderAPI`为可选的前置插件，如果服务器没有相应版本的`PlaceholderAPI`插件，`TotalPoints`插件会关闭PAPI功能。


## 建议和反馈

如果您对`TotalPoints`插件有任何建议和反馈，请发送电子邮件至以下任意一个邮箱：
* z5882852@qq.com
* silence5882852@gmail.com

## [更新日志](https://github.com/z5882852/TotalPoints/blob/main/CHANGELOG.md) 
