# zPrefix - 高级玩家称号系统插件

[![构建状态](https://img.shields.io/badge/构建-成功-brightgreen.svg)](https://github.com/dyzg/zPrefix)
[![Paper版本](https://img.shields.io/badge/Paper-1.20.1-blue.svg)](https://papermc.io/)
[![Java版本](https://img.shields.io/badge/Java-17+-orange.svg)](https://adoptium.net/)
[![许可证](https://img.shields.io/badge/许可证-开源-green.svg)](LICENSE)

## 📖 简介

**zPrefix** 是一个功能强大、高度可定制的 Minecraft Paper 1.20.1 称号系统插件。基于现代化的模块化架构设计，提供完整的称号管理、属性加成、统计追踪和第三方集成功能。

### 🌟 核心特色
- 🎯 **Paper API 1.20.1 最佳实践** - 完全基于官方API实现
- 🏗️ **模块化架构** - 易于扩展和维护的代码结构
- 📊 **智能统计系统** - 基于游戏统计的自动解锁机制
- 🔧 **高度可配置** - 所有功能都可通过配置文件自定义
- 🚀 **高性能优化** - 智能缓存和异步处理机制

## ✨ 功能特性

### 🏆 核心功能
- **🎖️ 称号系统**: 玩家可以解锁、切换和管理多个称号
- **⚡ 属性加成**: 每个称号提供不同的属性buff效果
- **💾 永久解锁**: 称号一旦解锁将永久保存
- **🖥️ GUI界面**: 直观的54槽位分页图形界面
- **⌨️ 命令支持**: 完整的命令系统和Tab补全
- **📊 统计追踪**: 基于Paper API的游戏统计自动解锁
- **🔄 数据清理**: 智能检测和清理无效称号数据

### ⚡ 属性加成系统
支持所有Minecraft原生属性和SagaLoreStats自定义属性：

#### 原生属性
- 🩸 **生命值** (GENERIC_MAX_HEALTH)
- ⚔️ **攻击力** (GENERIC_ATTACK_DAMAGE)
- 🛡️ **防御力** (GENERIC_ARMOR)
- 🏃 **移动速度** (GENERIC_MOVEMENT_SPEED)
- ⚡ **攻击速度** (GENERIC_ATTACK_SPEED)
- 🍀 **幸运值** (GENERIC_LUCK)
- 🛡️ **击退抗性** (GENERIC_KNOCKBACK_RESISTANCE)
- 📏 **攻击范围** (GENERIC_ATTACK_REACH)
- 🔍 **跟随范围** (GENERIC_FOLLOW_RANGE)

#### SagaLoreStats自定义属性 (可选)
- 🔥 **火焰伤害**、❄️ **冰霜伤害**、⚡ **雷电伤害**
- 🛡️ **魔法防御**、🏃 **闪避率**、💥 **暴击率**
- 🩹 **生命恢复**、🔮 **法力值**、✨ **经验加成**
- 更多自定义属性...

### 🎮 用户界面
- **📄 分页GUI**: 54槽位固定布局，最后一行为功能按钮
- **🔄 实时状态**: 显示已解锁/未解锁/当前使用状态
- **🖱️ 点击切换**: 直接点击称号进行切换
- **📋 属性预览**: 鼠标悬停显示详细属性信息
- **🔍 数据验证**: 打开GUI时自动检查数据一致性

### 📊 智能统计系统
基于Paper API 1.20.1实现的完整统计追踪：

#### 基础统计 (9种)
- 🗡️ **怪物击杀** (kill-mobs)
- 💔 **受到伤害** (damage-taken)
- 🚶 **行走距离** (walk-distance)
- ⏰ **游戏时间** (play-time)
- ⚔️ **玩家击杀** (player-kills)
- 💀 **死亡次数** (deaths)
- 🦘 **跳跃次数** (jump)
- 🎣 **钓鱼数量** (fish-caught)
- 🐄 **繁殖动物** (animals-bred)

#### 特殊事件 (12种)
- 💎 **矿物发现**: 钻石、绿宝石、远古残骸、金矿、铁矿、煤矿
- 🐉 **Boss击杀**: 末影龙、凋零、远古守卫者、监守者
- 🌍 **维度探索**: 下界、末地

### 🔌 第三方集成
- **PlaceholderAPI**: 提供称号占位符供聊天插件使用
- **SagaLoreStats**: 集成自定义属性系统 (可选)
- **优雅降级**: 依赖插件不存在时功能正常运行

## 🚀 快速开始

### 📋 系统要求
- **服务端**: Paper 1.20.1 或更高版本
- **Java**: 17 或更高版本
- **内存**: 建议至少 2GB RAM

### 📦 安装步骤
1. **下载插件**: 从 [Releases](https://github.com/dyzg/zPrefix/releases) 下载最新版本
2. **安装插件**: 将 `zPrefix-x.x.x.x.jar` 放入服务器的 `plugins` 文件夹
3. **重启服务器**: 重启服务器或使用 `/reload` 命令
4. **配置插件**: 插件将自动生成配置文件，根据需要进行调整

### 🔌 可选依赖 (推荐)
```bash
# PlaceholderAPI - 用于聊天插件集成
/plugins/PlaceholderAPI-2.11.5.jar

# SagaLoreStats - 用于自定义属性系统
/plugins/SagaLoreStats.jar
```

### ✅ 安装验证
```bash
# 检查插件是否正常加载
/plugins

# 测试基本功能
/title gui

# 查看版本信息
/title help
```

## ⌨️ 命令使用

### 🎮 玩家命令
| 命令 | 描述 | 权限 |
|------|------|------|
| `/title` | 打开称号GUI界面 | `zprefix.use` |
| `/title gui` | 打开称号GUI界面 | `zprefix.use` |
| `/title set <称号ID>` | 切换到指定称号 | `zprefix.use` |
| `/title remove` | 移除当前称号 | `zprefix.use` |
| `/title list` | 查看已解锁的称号列表 | `zprefix.use` |
| `/title info <称号ID>` | 查看称号详细信息 | `zprefix.use` |
| `/title help` | 显示帮助信息 | `zprefix.use` |

### 🛠️ 管理员命令
| 命令 | 描述 | 权限 |
|------|------|------|
| `/title give <玩家> <称号ID>` | 给予玩家指定称号 | `zprefix.admin` |
| `/title take <玩家> <称号ID>` | 移除玩家指定称号 | `zprefix.admin` |
| `/title reload` | 重新加载配置文件 | `zprefix.admin` |
| `/title cleanup [all\|player <玩家>]` | 清理无效称号数据 | `zprefix.admin` |

### 💡 使用示例
```bash
# 打开称号GUI
/title

# 切换到勇士称号
/title set warrior

# 查看传奇称号信息
/title info legend

# 给玩家Steve传奇称号
/title give Steve legend

# 清理所有玩家的无效称号
/title cleanup all

# 清理指定玩家的无效称号
/title cleanup player Steve
```

### 🔄 Tab补全支持
- 所有命令都支持智能Tab补全
- 自动补全称号ID、玩家名等参数
- 根据权限显示可用命令

## 🔐 权限系统

### 🎮 玩家权限
| 权限节点 | 描述 | 默认值 |
|----------|------|--------|
| `zprefix.use` | 使用称号系统的基本权限 | `true` |
| `zprefix.gui` | 打开称号GUI界面 | `true` |
| `zprefix.set` | 切换称号 | `true` |
| `zprefix.remove` | 移除当前称号 | `true` |
| `zprefix.list` | 查看称号列表 | `true` |
| `zprefix.info` | 查看称号信息 | `true` |

### 🛠️ 管理员权限
| 权限节点 | 描述 | 默认值 |
|----------|------|--------|
| `zprefix.admin` | 所有管理员权限 | `op` |
| `zprefix.give` | 给予玩家称号 | `op` |
| `zprefix.take` | 移除玩家称号 | `op` |
| `zprefix.reload` | 重新加载配置 | `op` |
| `zprefix.cleanup` | 清理无效数据 | `op` |

### 📝 权限配置示例
```yaml
# LuckPerms配置示例
groups:
  default:
    permissions:
      - zprefix.use

  vip:
    permissions:
      - zprefix.use
      - zprefix.gui

  admin:
    permissions:
      - zprefix.admin
```

## ⚙️ 配置文件

### 📄 config.yml - 主配置
```yaml
# 调试模式
debug: false

# 玩家设置
player:
  # 是否在玩家第一次加入时显示欢迎信息
  show-welcome-message: false

# GUI配置
gui:
  title: "§6§l称号系统"
  size: 54

# 进度统计配置
progress:
  enabled: true
  debug-logging: false

# 属性管理配置
attributes:
  cleanup-on-remove: true
  debug-logging: false
```

### 🏆 titles.yml - 称号配置
```yaml
titles:
  # 新手称号 - 自动解锁
  newbie:
    display-name: "§7新手"
    item:
      material: "LEATHER_HELMET"
      name: "§7新手"
      lore:
        - "§7刚刚踏入服务器的新人"
        - ""
        - "§a点击切换称号"
    attributes: {}
    unlock-conditions:
      auto-unlock: true
    default: true

  # 勇士称号 - 基于统计解锁
  warrior:
    display-name: "§c勇士"
    item:
      material: "IRON_SWORD"
      name: "§c勇士"
      lore:
        - "§7经验丰富的战士"
        - ""
        - "§e属性加成:"
        - "§c攻击力 +2"
        - "§c生命值 +4"
        - ""
        - "§a点击切换称号"
    attributes:
      GENERIC_ATTACK_DAMAGE: 2.0
      GENERIC_MAX_HEALTH: 4.0
    unlock-conditions:
      auto-unlock: false
      kill-mobs: 100
      damage-taken: 500.0

  # 传奇称号 - 管理员专用
  legend:
    display-name: "§6§l传奇"
    item:
      material: "NETHERITE_SWORD"
      name: "§6§l传奇"
      lore:
        - "§7传说中的英雄"
        - ""
        - "§e属性加成:"
        - "§c攻击力 +5"
        - "§c生命值 +10"
        - "§b移动速度 +0.05"
        - "§a幸运值 +3"
        - ""
        - "§a点击切换称号"
    attributes:
      GENERIC_ATTACK_DAMAGE: 5.0
      GENERIC_MAX_HEALTH: 10.0
      GENERIC_MOVEMENT_SPEED: 0.05
      GENERIC_LUCK: 3.0
    unlock-conditions:
      admin-only: true
```

### 💬 messages.yml - 消息配置
```yaml
# 所有插件输出的消息都可以在此文件中自定义
prefix: "§e§l[称号系统] §r"

common:
  no-permission: "§c你没有权限执行此命令"
  player-not-found: "§c找不到玩家: {player}"
  invalid-args: "§c参数错误，用法: {usage}"

title:
  title-unlocked: "§a恭喜！你解锁了称号: {title}"
  title-set: "§a称号已切换为: {title}"
  title-removed: "§a已移除当前称号"
  no-titles: "§c你还没有解锁任何称号"

gui:
  title: "§6§l称号系统"
  previous-page: "§e上一页"
  next-page: "§e下一页"
  close: "§c关闭"
```

## 🏆 预设称号系统

插件预设了完整的称号进阶体系：

### 🌱 新手系列
| 称号 | 解锁条件 | 属性加成 |
|------|----------|----------|
| **新手** | 自动解锁 | 无 |
| **初学者** | 游戏1小时 | 生命值+2 |
| **冒险者** | 行走1公里 + 跳跃100次 | 生命值+2, 移动速度+0.01 |

### ⚔️ 战斗系列
| 称号 | 解锁条件 | 属性加成 |
|------|----------|----------|
| **战士** | 击杀50只怪物 | 攻击力+1, 生命值+2 |
| **勇士** | 击杀200只怪物 + 受伤500点 | 攻击力+2, 生命值+4 |
| **战神** | 击杀1000只怪物 + PVP击杀10次 | 攻击力+4, 生命值+8, 攻击速度+0.1 |

### 🛡️ 防御系列
| 称号 | 解锁条件 | 属性加成 |
|------|----------|----------|
| **守护者** | 受伤1000点 | 防御力+2, 生命值+6 |
| **坚盾** | 受伤2000点 + 死亡5次 | 防御力+4, 生命值+10, 击退抗性+0.2 |

### 🌍 探索系列
| 称号 | 解锁条件 | 属性加成 |
|------|----------|----------|
| **旅行者** | 行走5公里 | 移动速度+0.02 |
| **探险家** | 行走20公里 + 进入下界 | 移动速度+0.05, 幸运值+1 |
| **世界行者** | 行走100公里 + 进入所有维度 | 移动速度+0.1, 幸运值+2, 生命值+6 |

### 🎣 生活系列
| 称号 | 解锁条件 | 属性加成 |
|------|----------|----------|
| **渔夫** | 钓鱼100次 | 幸运值+1 |
| **牧场主** | 繁殖50只动物 | 幸运值+1, 生命值+4 |
| **生活大师** | 钓鱼500次 + 繁殖200只动物 + 游戏100小时 | 幸运值+3, 生命值+8 |

### 🐉 成就系列
| 称号 | 解锁条件 | 属性加成 |
|------|----------|----------|
| **屠龙者** | 击杀末影龙 | 攻击力+3, 生命值+6, 幸运值+2 |
| **凋零克星** | 击杀凋零 | 攻击力+3, 防御力+2, 生命值+6 |
| **传说猎人** | 击杀所有Boss | 全属性大幅加成 |

### 👑 特殊系列
| 称号 | 解锁条件 | 属性加成 |
|------|----------|----------|
| **传奇** | 管理员给予 | 全属性顶级加成 |
| **创世神** | 管理员专用 | 超级属性加成 |

## 🔌 PlaceholderAPI 集成

### 📋 可用占位符
| 占位符 | 描述 | 示例输出 |
|--------|------|----------|
| `%zprefix_current%` | 当前称号显示名 | `§c勇士` |
| `%zprefix_prefix%` | 当前称号前缀 | `[§c勇士§r]` |
| `%zprefix_count%` | 已解锁称号数量 | `5` |
| `%zprefix_has_<称号ID>%` | 是否拥有指定称号 | `true/false` |

### 💬 聊天插件配置示例

#### EssentialsX Chat
```yaml
format: '{DISPLAYNAME} %zprefix_prefix% {MESSAGE}'
```

#### ChatEx
```yaml
format: '%zprefix_prefix% %player_displayname%: %message%'
```

#### DeluxeChat
```yaml
formats:
  default:
    priority: 1
    prefix: '%zprefix_prefix%'
    name_color: '&f'
    chat_color: '&f'
```

## 🏗️ 技术架构

### 📦 核心组件
```
zPrefix/
├── ZPrefix.java                    # 插件主类
├── command/TitleCommand.java       # 命令处理器
├── data/                          # 数据模型
│   ├── PlayerTitleData.java       # 玩家数据
│   └── TitleInfo.java             # 称号信息
├── gui/TitleGUI.java              # GUI管理器
├── integration/                   # 第三方集成
│   ├── PlaceholderAPIExpansion.java
│   └── SagaLoreStatsIntegration.java
├── listener/                      # 事件监听器
│   ├── GUIListener.java
│   ├── PlayerListener.java
│   └── VanillaStatsListener.java
├── manager/                       # 业务逻辑管理器
│   ├── ConfigManager.java         # 配置管理
│   ├── TitleManager.java          # 称号管理
│   ├── BuffManager.java           # 属性管理
│   ├── UnifiedStatsManager.java   # 统计管理
│   ├── AbstractStatsProcessor.java # 抽象处理器
│   ├── BasicStatsProcessor.java   # 基础统计
│   ├── SpecialEventProcessor.java # 特殊事件
│   └── VanillaStatsManager.java   # 兼容适配器
└── util/MessageUtil.java          # 工具类
```

### 🏛️ 设计模式
- **🎯 单例模式**: 管理器类保持单例
- **🔄 适配器模式**: VanillaStatsManager适配新系统
- **📋 策略模式**: 不同统计处理器实现不同策略
- **👀 观察者模式**: 事件监听器处理游戏事件
- **🏭 工厂模式**: 统计处理器的创建和注册

### ⚡ 性能优化
- **💾 智能缓存**: 30秒有效期的统计数据缓存
- **🔄 异步处理**: 统计检查和数据保存异步执行
- **🧹 内存管理**: 玩家离线时自动清理缓存
- **🔒 线程安全**: 使用ConcurrentHashMap等线程安全集合

## 🔧 开发规范

本插件严格遵循现代化开发规范：

### 🏗️ 架构设计原则
- **🎯 统一方法原则**: 避免重复造轮子，相同功能使用统一方法
- **📦 模块化编程**: 使用抽象类实现功能模块化
- **🔗 Paper API优先**: 优先使用Paper官方API，避免自定义实现
- **🔄 职责分离**: 每个类和方法都有明确的职责

### 📝 代码质量标准
- **📖 高可读性**: 避免深层嵌套，使用描述性方法名
- **📚 详细注释**: 所有方法都有完整的JavaDoc注释
- **🛡️ 异常处理**: 完善的异常处理和错误恢复机制
- **⚡ 性能优化**: 使用缓存、异步处理和优化算法

### 🔧 配置驱动设计
- **🎨 高度自定义**: 所有文本、颜色、功能都可配置
- **🔄 动态适配**: 使用全局自动动态适配方法
- **⬆️ 向后兼容**: 重构时保持原有功能完整性
- **🔌 扩展友好**: 易于添加新功能和第三方集成

## 🔄 版本管理

### 📊 自动版本迭代
- **版本格式**: `主版本.次版本.修订版本.构建号` (例如: `1.0.0.15`)
- **自动递增**: 每次构建自动递增构建号
- **版本追踪**: 记录构建时间、用户、次数等详细信息

### 🛠️ 版本管理命令
```bash
# 显示版本信息
gradle showVersion

# 设置自定义版本
gradle setVersion -PnewVersion=2.1.3.5

# 重置版本
gradle resetVersion

# 完整发布构建
gradle buildRelease
```

### 📋 版本信息
- **当前版本**: 自动迭代 (查看 `gradle showVersion`)
- **支持版本**: Paper 1.20.1+
- **Java版本**: 17+
- **作者**: dyzg
- **许可证**: 开源

## 📊 统计参数完整列表

### 🎯 基础统计参数 (9个)
| 参数名 | 描述 | 数据类型 | Paper API |
|--------|------|----------|-----------|
| `kill-mobs` | 怪物击杀数 | 整数 | `MOB_KILLS` |
| `damage-taken` | 受到伤害 | 浮点数 | `DAMAGE_TAKEN` |
| `walk-distance` | 行走距离 | 浮点数 | `WALK_ONE_CM` |
| `play-time` | 游戏时间 | 浮点数 | `PLAY_ONE_MINUTE` |
| `player-kills` | 玩家击杀数 | 整数 | `PLAYER_KILLS` |
| `deaths` | 死亡次数 | 整数 | `DEATHS` |
| `jump` | 跳跃次数 | 整数 | `JUMP` |
| `fish-caught` | 钓鱼数量 | 整数 | `FISH_CAUGHT` |
| `animals-bred` | 繁殖动物数量 | 整数 | `ANIMALS_BRED` |

### 🌟 特殊事件参数 (12个)
| 参数名 | 描述 | 检测方式 |
|--------|------|----------|
| `find-diamond` | 发现钻石矿 | 挖掘钻石矿石 |
| `find-emerald` | 发现绿宝石矿 | 挖掘绿宝石矿石 |
| `find-ancient-debris` | 发现远古残骸 | 挖掘远古残骸 |
| `find-gold` | 发现金矿 | 挖掘金矿石 |
| `find-iron` | 发现铁矿 | 挖掘铁矿石 |
| `find-coal` | 发现煤矿 | 挖掘煤矿石 |
| `kill-ender-dragon` | 击杀末影龙 | 击杀末影龙实体 |
| `kill-wither` | 击杀凋零 | 击杀凋零实体 |
| `kill-elder-guardian` | 击杀远古守卫者 | 击杀远古守卫者实体 |
| `kill-warden` | 击杀监守者 | 击杀监守者实体 |
| `enter-nether` | 进入下界 | 间接检测下界活动 |
| `enter-end` | 进入末地 | 间接检测末地活动 |

### 🔧 特殊控制参数 (3个)
| 参数名 | 描述 | 数据类型 |
|--------|------|----------|
| `auto-unlock` | 自动解锁控制 | 布尔值 |
| `admin-only` | 管理员专用 | 布尔值 |
| `default` | 默认称号标记 | 布尔值 |

## 🚀 快速配置示例

### 基础统计称号
```yaml
warrior:
  display-name: "§c勇士"
  unlock-conditions:
    auto-unlock: false
    kill-mobs: 100        # 击杀100只怪物
    damage-taken: 500.0   # 受到500点伤害
    play-time: 10.0       # 游戏10小时
```

### 特殊事件称号
```yaml
dragon_slayer:
  display-name: "§5屠龙者"
  unlock-conditions:
    auto-unlock: false
    special-event: "kill-ender-dragon"  # 击杀末影龙
```

### 复合条件称号
```yaml
master_explorer:
  display-name: "§6§l大师探险家"
  unlock-conditions:
    auto-unlock: false
    walk-distance: 5000000              # 行走50公里
    special-event: "enter-nether"       # 进入下界
    special-event: "enter-end"          # 进入末地
    special-event: "find-diamond"       # 发现钻石
```

## 🛠️ 故障排除

### 常见问题
1. **称号不显示在聊天中**
   - 安装PlaceholderAPI插件
   - 配置聊天插件使用占位符

2. **统计数据不更新**
   - 检查`progress.enabled`配置
   - 启用调试模式查看日志

3. **属性加成不生效**
   - 检查SagaLoreStats插件状态
   - 验证属性名称是否正确

4. **GUI显示异常**
   - 执行`/title cleanup`清理无效数据
   - 检查titles.yml配置格式

### 调试模式
```yaml
# config.yml
debug: true
progress:
  debug-logging: true
attributes:
  debug-logging: true
```

## 📄 许可证

本项目采用开源许可证，欢迎贡献代码和建议。

## 💬 支持与反馈

### 🔗 联系方式
- **GitHub Issues**: [提交问题和建议](https://github.com/dyzg/zPrefix/issues)
- **文档**: [查看完整文档](https://github.com/dyzg/zPrefix/wiki)
- **更新日志**: [查看版本更新](https://github.com/dyzg/zPrefix/releases)

### 🤝 贡献指南
1. Fork 项目仓库
2. 创建功能分支
3. 提交代码更改
4. 发起 Pull Request

### ⭐ 支持项目
如果这个插件对您有帮助，请考虑：
- 给项目点个 ⭐ Star
- 分享给其他服务器管理员
- 提供反馈和建议

---

## 🎉 特别感谢

感谢所有为 zPrefix 项目做出贡献的开发者和用户！

**zPrefix - 让称号系统变得简单而强大！** 🚀
