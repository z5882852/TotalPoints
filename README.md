# TotalPoints

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
# Points别名
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

# 累计Points奖励组
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
      prompt: "累充100礼包领取成功"
   2:
      name: "XXX"
      total: 200
      commands:
         - "XXX"
      prompt: "XXX"

# Points变化记录
logger:
   # 是否启用Points变化记录
   enable: false
   # 记录方式，填"mysql" 或者 “local”
   type: local
   # 是否记录堆栈跟踪信息
   # 记录堆栈跟踪信息可以更好溯源Points变化来源(比如可以查到刷点券具体是什么插件导致的，前提是你看得懂)，但是会增加日志大小
   enable_stackTrace: true
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
```

要更改配置选项，请编辑config.yml文件。

## 使用方法

该插件提供以下命令：

* `/tpw`: 查看自己的累计点数。
* `/tpw <玩家名>`: 查看指定玩家的累计点数。
* `/tpw add <玩家名> <点数>`: 增加指定玩家的累计点数。
* `/tpw remove <玩家名> <点数>`: 减少指定玩家的点数。
* `/tpw set <玩家名> <点数>`: 设定指定玩家的点数。
* `/tpw reload`: 重载配置文件。

除此之外，`TotalPoints`插件还提供以下`PlaceholderAPI`变量：

* `%points_total%`: 玩家的累积点数。
* `%reward_group_{组名}_status%`: 玩家的奖励组领取状态
* `%reward_group_{组名}_name%`: 该奖励组的名称
* `%reward_group_{组名}_total%`: 该奖励组的的领取条件
* `%reward_group_{组名}_prompt%`: 该奖励组的领取后的提示。

## 常见问题

1. `TotalPoints`插件需要安装哪些前置插件？

    `TotalPoints`插件需要安装`PlayerPoints`插件才能正常运行。

2. `PlaceholderAPI`插件是否为必需的前置插件？

    `PlaceholderAPI`为可选的前置插件，如果服务器没有相应版本的`PlaceholderAPI`插件，`TotalPoints`插件会关闭PAPI功能。


## 建议和反馈

如果您对`TotalPoints`插件有任何建议和反馈，请发送电子邮件至以下任意一个邮箱：
* z5882852@qq.com
* silence5882852@gmail.com

## 更新日志