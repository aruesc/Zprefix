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
- 📊 **强大统计系统** - 35个统计参数，支持精确的解锁条件
- 🎮 **显示名支持** - 所见即所得的操作体验，零学习成本
- 🏆 **完整称号体系** - 26个预设称号，6大系列完整进阶路线
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
基于Paper API 1.20.1实现的完整统计追踪，支持35个统计参数：

#### 基础统计参数 (13个)
- 🗡️ **怪物击杀** (kill-mobs) - 击杀怪物数量
- 💔 **受到伤害** (damage-taken) - 受到伤害总量
- 🚶 **行走距离** (walk-distance) - 行走距离
- ⏰ **游戏时间** (play-time) - 游戏时间
- ⚔️ **玩家击杀** (player-kills) - PVP击杀数
- ⚔️ **造成伤害** (damage-dealt) - 造成伤害总量
- 🌍 **总移动距离** (distance-traveled) - 包含所有移动方式
- 💀 **死亡次数** (deaths) - 死亡次数
- 🦘 **跳跃次数** (jump) - 跳跃次数
- 🎣 **钓鱼数量** (fish-caught) - 钓鱼次数
- 🐄 **繁殖动物** (animals-bred) - 繁殖动物数量
- 🔨 **制作物品** (items-crafted) - 制作物品总数
- ✨ **附魔物品** (items-enchanted) - 附魔物品数量
- ⛏ **破坏方块** (blocks-broken) - 破坏方块总数

#### 矿物挖掘数量统计 (10个)
- 💎 **钻石矿** (diamonds-mined) - 挖掘钻石矿数量
- 💚 **绿宝石矿** (emeralds-mined) - 挖掘绿宝石矿数量
- 🔥 **远古残骸** (ancient-debris-mined) - 挖掘远古残骸数量
- 🟨 **金矿** (gold-mined) - 挖掘金矿数量
- ⚪ **铁矿** (iron-mined) - 挖掘铁矿数量
- ⚫ **煤矿** (coal-mined) - 挖掘煤矿数量
- 🟫 **铜矿** (copper-mined) - 挖掘铜矿数量
- 🔵 **青金石矿** (lapis-mined) - 挖掘青金石矿数量
- 🔴 **红石矿** (redstone-mined) - 挖掘红石矿数量
- ⚪ **石英矿** (quartz-mined) - 挖掘石英矿数量

#### 生物击杀数量统计 (9个)
- 🐉 **Boss击杀**: 末影龙、凋零、远古守卫者、监守者
- 🧟 **怪物击杀**: 僵尸、骷髅、苦力怕、蜘蛛、末影人
- 🐄 **动物击杀**: 牛、猪、羊、鸡

#### 生活活动数量统计 (4个)
- 🏪 **村民交易** (villager-trades) - 村民交易次数
- 🍖 **食物消费** (food-eaten) - 食物消费数量
- 🧪 **药水消费** (potions-drunk) - 药水消费数量
- 🔧 **工具损坏** (tools-broken) - 工具损坏数量

#### 特殊事件参数 (9个)
- 💎 **稀有物品获得**: 钻石、绿宝石、下界合金锭、不死图腾、鞘翅、下界之星、龙蛋
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
| `/title set <称号显示名>` | 切换到指定称号 | `zprefix.use` |
| `/title remove` | 移除当前称号 | `zprefix.use` |
| `/title list` | 查看已解锁的称号列表 | `zprefix.use` |
| `/title info <称号显示名>` | 查看称号详细信息 | `zprefix.use` |
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

# 切换到勇士称号 (使用显示名)
/title set 勇士

# 查看传奇称号信息 (使用显示名)
/title info 传奇

# 查看挖矿大师称号信息 (支持多词称号)
/title info 挖矿大师

# 给玩家Steve传奇称号 (管理员命令仍使用ID)
/title give Steve legend

# 清理所有玩家的无效称号
/title cleanup all

# 清理指定玩家的无效称号
/title cleanup player Steve
```

### 🔄 智能Tab补全
- ✅ **显示名补全**: `/title set` 和 `/title info` 支持称号显示名补全
- ✅ **权限过滤**: `/title set` 只显示已解锁的称号
- ✅ **智能匹配**: 支持部分匹配和模糊搜索
- ✅ **多词支持**: 支持带空格的称号名（如"挖矿大师"）
- ✅ **去除颜色**: 自动去除颜色代码，提供纯文本补全

### 🎯 显示名支持特性
- **所见即所得**: 用户可以直接使用GUI中看到的称号名称
- **智能匹配**: 支持精确匹配、模糊匹配和部分匹配
- **向后兼容**: 原有的称号ID查询方式仍然有效
- **用户友好**: 无需记忆复杂的内部ID

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

## 🏆 完整称号体系

插件预设了26个称号的完整进阶体系，涵盖6大系列：

### 🌱 新手系列 (3个)
| 称号 | 解锁条件 | 属性加成 |
|------|----------|----------|
| **新手** | 自动解锁 | 无 |
| **初学者** | 游戏1小时 | 生命值+2 |
| **冒险者** | 行走1000米 + 跳跃100次 | 生命值+2, 移动速度+0.01 |

### ⚔️ 战斗系列 (3个)
| 称号 | 解锁条件 | 属性加成 |
|------|----------|----------|
| **战士** | 击杀50只怪物 | 攻击力+1, 生命值+2 |
| **勇士** | 击杀200只怪物 + 受伤500点 | 攻击力+2, 生命值+4 |
| **战神** | 击杀1000只怪物 + PVP击杀10次 | 攻击力+4, 生命值+8, 攻击速度+0.1 |

### 🛡️ 防御系列 (2个)
| 称号 | 解锁条件 | 属性加成 |
|------|----------|----------|
| **守护者** | 受伤1000点 | 防御力+2, 生命值+6 |
| **坚盾** | 受伤2000点 + 死亡5次 | 防御力+4, 生命值+10, 击退抗性+0.2 |

### 🌍 探索系列 (3个)
| 称号 | 解锁条件 | 属性加成 |
|------|----------|----------|
| **旅行者** | 行走5000米 | 移动速度+0.02 |
| **探险家** | 行走20000米 + 进入下界 | 移动速度+0.05, 幸运值+1 |
| **世界行者** | 总移动100000米 + 进入下界 + 进入末地 | 移动速度+0.1, 幸运值+2, 生命值+6 |

### 🏠 生活系列 (3个)
| 称号 | 解锁条件 | 属性加成 |
|------|----------|----------|
| **渔夫** | 钓鱼100次 | 幸运值+1 |
| **牧场主** | 繁殖50只动物 | 幸运值+1, 生命值+4 |
| **生活大师** | 钓鱼500次 + 繁殖200只动物 + 游戏100小时 | 幸运值+3, 生命值+8 |

### ⛏️ 矿工系列 (4个)
| 称号 | 解锁条件 | 属性加成 |
|------|----------|----------|
| **煤矿工** | 挖掘500个煤矿 | 幸运值+1 |
| **铁矿工** | 挖掘200个铁矿 + 挖掘1000个煤矿 | 幸运值+1, 生命值+2 |
| **钻石矿工** | 挖掘100个钻石矿 + 挖掘500个铁矿 | 幸运值+2, 生命值+4 |
| **挖矿大师** | 挖掘500个钻石矿 + 挖掘100个绿宝石矿 + 挖掘50个远古残骸 + 破坏50000个方块 | 幸运值+5, 生命值+10, 攻击力+2 |

### 👹 专业击杀系列 (3个)
| 称号 | 解锁条件 | 属性加成 |
|------|----------|----------|
| **僵尸杀手** | 击杀500只僵尸 | 攻击力+1, 生命值+2 |
| **骷髅猎人** | 击杀300只骷髅 | 攻击力+1, 攻击速度+0.05 |
| **苦力怕终结者** | 击杀200只苦力怕 | 攻击力+1, 击退抗性+0.1 |

### 🐉 Boss击杀系列 (3个)
| 称号 | 解锁条件 | 属性加成 |
|------|----------|----------|
| **屠龙者** | 击杀1条末影龙 | 攻击力+3, 生命值+6, 幸运值+2 |
| **凋零克星** | 击杀3只凋零 | 攻击力+3, 防御力+2, 生命值+6 |
| **传说猎人** | 击杀5条末影龙 + 击杀10只凋零 + 击杀3只远古守卫者 + 击杀1只监守者 | 攻击力+6, 生命值+15, 防御力+3, 幸运值+3 |

### 👑 传奇系列 (2个)
| 称号 | 解锁条件 | 属性加成 |
|------|----------|----------|
| **终极冒险家** | 复合条件 (总移动100000米 + 击杀1000只怪物 + 破坏50000个方块 + 挖掘200个钻石矿 + 击杀1条末影龙 + 击杀3只凋零 + 进入下界 + 进入末地) | 攻击力+5, 生命值+12, 防御力+3, 移动速度+0.08, 幸运值+4 |
| **传奇** | 管理员专用 | 攻击力+8, 生命值+20, 防御力+5, 移动速度+0.1, 幸运值+5 |

### 🎯 称号进阶路线
- **新手路线**: 新手 → 初学者 → 冒险者
- **战斗路线**: 战士 → 勇士 → 战神 → 专业击杀 → Boss击杀
- **探索路线**: 旅行者 → 探险家 → 世界行者
- **挖矿路线**: 煤矿工 → 铁矿工 → 钻石矿工 → 挖矿大师
- **终极路线**: 各系列大师称号 → 终极冒险家 → 传奇

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

**总计**: 35个统计参数，支持精确的称号解锁条件

### 🎯 基础统计参数 (13个)
| 参数名 | 描述 | 数据类型 | Paper API | 示例配置 |
|--------|------|----------|-----------|----------|
| `kill-mobs` | 怪物击杀数 | 整数 | `MOB_KILLS` | `kill-mobs: 100` |
| `player-kills` | 玩家击杀数 (PVP) | 整数 | `PLAYER_KILLS` | `player-kills: 10` |
| `damage-taken` | 受到伤害总量 | 浮点数 | `DAMAGE_TAKEN` | `damage-taken: 500.0` |
| `damage-dealt` | 造成伤害总量 | 浮点数 | `DAMAGE_DEALT` | `damage-dealt: 1000.0` |
| `walk-distance` | 行走距离 (米) | 浮点数 | `WALK_ONE_CM` + `SPRINT_ONE_CM` + `CROUCH_ONE_CM` | `walk-distance: 1000.0` |
| `distance-traveled` | 总移动距离 (米) | 浮点数 | 所有移动方式统计 | `distance-traveled: 5000.0` |
| `play-time` | 游戏时间 (小时) | 浮点数 | `PLAY_ONE_MINUTE` | `play-time: 50.0` |
| `deaths` | 死亡次数 | 整数 | `DEATHS` | `deaths: 5` |
| `jump` | 跳跃次数 | 整数 | `JUMP` | `jump: 1000` |
| `fish-caught` | 钓鱼数量 | 整数 | `FISH_CAUGHT` | `fish-caught: 100` |
| `animals-bred` | 繁殖动物数量 | 整数 | `ANIMALS_BRED` | `animals-bred: 50` |
| `items-crafted` | 制作物品总数 | 整数 | `CRAFT_ITEM` (所有物品) | `items-crafted: 500` |
| `items-enchanted` | 附魔物品数量 | 整数 | `ITEM_ENCHANTED` | `items-enchanted: 50` |
| `blocks-broken` | 破坏方块总数 | 整数 | `MINE_BLOCK` (所有方块) | `blocks-broken: 5000` |

### ⛏️ 矿物挖掘数量统计 (10个)
| 参数名 | 描述 | 包含矿石类型 | 示例配置 |
|--------|------|-------------|----------|
| `diamonds-mined` | 钻石矿挖掘数量 | 钻石矿石 + 深层钻石矿石 | `diamonds-mined: 50` |
| `emeralds-mined` | 绿宝石矿挖掘数量 | 绿宝石矿石 + 深层绿宝石矿石 | `emeralds-mined: 20` |
| `ancient-debris-mined` | 远古残骸挖掘数量 | 远古残骸 | `ancient-debris-mined: 10` |
| `gold-mined` | 金矿挖掘数量 | 金矿石 + 深层金矿石 + 下界金矿石 | `gold-mined: 100` |
| `iron-mined` | 铁矿挖掘数量 | 铁矿石 + 深层铁矿石 | `iron-mined: 200` |
| `coal-mined` | 煤矿挖掘数量 | 煤矿石 + 深层煤矿石 | `coal-mined: 500` |
| `copper-mined` | 铜矿挖掘数量 | 铜矿石 + 深层铜矿石 | `copper-mined: 300` |
| `lapis-mined` | 青金石矿挖掘数量 | 青金石矿石 + 深层青金石矿石 | `lapis-mined: 100` |
| `redstone-mined` | 红石矿挖掘数量 | 红石矿石 + 深层红石矿石 | `redstone-mined: 200` |
| `quartz-mined` | 石英矿挖掘数量 | 下界石英矿石 | `quartz-mined: 150` |

### 👹 生物击杀数量统计 (9个)
| 参数名 | 描述 | 目标实体 | 示例配置 |
|--------|------|----------|----------|
| `ender-dragons-killed` | 末影龙击杀数量 | 末影龙 | `ender-dragons-killed: 1` |
| `withers-killed` | 凋零击杀数量 | 凋零 | `withers-killed: 3` |
| `elder-guardians-killed` | 远古守卫者击杀数量 | 远古守卫者 | `elder-guardians-killed: 3` |
| `wardens-killed` | 监守者击杀数量 | 监守者 | `wardens-killed: 1` |
| `zombies-killed` | 僵尸击杀数量 | 僵尸 | `zombies-killed: 100` |
| `skeletons-killed` | 骷髅击杀数量 | 骷髅 | `skeletons-killed: 100` |
| `creepers-killed` | 苦力怕击杀数量 | 苦力怕 | `creepers-killed: 50` |
| `spiders-killed` | 蜘蛛击杀数量 | 蜘蛛 | `spiders-killed: 80` |
| `endermen-killed` | 末影人击杀数量 | 末影人 | `endermen-killed: 30` |

### 🏠 生活活动数量统计 (4个)
| 参数名 | 描述 | 统计内容 | 示例配置 |
|--------|------|----------|----------|
| `villager-trades` | 村民交易次数 | 与村民的交易次数 | `villager-trades: 100` |
| `food-eaten` | 食物消费数量 | 所有食物的消费总数 | `food-eaten: 500` |
| `potions-drunk` | 药水消费数量 | 所有药水的消费总数 | `potions-drunk: 50` |
| `tools-broken` | 工具损坏数量 | 所有工具的损坏总数 | `tools-broken: 20` |

### 🌟 特殊事件参数 (9个)
| 参数名 | 描述 | 检测方式 |
|--------|------|----------|
| `get-diamond` | 获得钻石 | 拾取过钻石物品 |
| `get-emerald` | 获得绿宝石 | 拾取过绿宝石物品 |
| `get-netherite` | 获得下界合金锭 | 拾取过下界合金锭 |
| `get-totem` | 获得不死图腾 | 拾取过不死图腾 |
| `get-elytra` | 获得鞘翅 | 拾取过鞘翅 |
| `get-nether-star` | 获得下界之星 | 拾取过下界之星 |
| `get-dragon-egg` | 获得龙蛋 | 拾取过龙蛋 |
| `enter-nether` | 进入下界 | 挖掘过下界岩、下界金矿石等或获得过烈焰棒、恶魂之泪 |
| `enter-end` | 进入末地 | 挖掘过末地石或获得过末影珍珠、紫颂果等 |

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

## 🎉 特别感谢

感谢所有为 zPrefix 项目做出贡献的开发者和用户！

**zPrefix - 让称号系统变得简单而强大！** 🚀
