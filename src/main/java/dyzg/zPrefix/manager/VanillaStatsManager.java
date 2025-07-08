package dyzg.zPrefix.manager;

import dyzg.zPrefix.ZPrefix;
import dyzg.zPrefix.data.TitleInfo;
import dyzg.zPrefix.util.MessageUtil;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * 原版统计数据管理器
 * 使用Minecraft原版的统计数据来检查称号解锁条件
 */
public class VanillaStatsManager {
    
    private final ZPrefix plugin;
    private final ConfigManager configManager;
    private final TitleManager titleManager;
    
    public VanillaStatsManager(ZPrefix plugin, ConfigManager configManager, TitleManager titleManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.titleManager = titleManager;
    }
    
    // ==================== 原版统计数据获取 ====================
    
    /**
     * 获取玩家击杀怪物总数
     * 
     * @param player 玩家
     * @return 击杀怪物总数
     */
    public int getMobKills(Player player) {
        try {
            int totalKills = 0;
            
            // 统计所有敌对生物的击杀数
            EntityType[] hostileMobs = {
                EntityType.ZOMBIE, EntityType.SKELETON, EntityType.CREEPER, EntityType.SPIDER,
                EntityType.ENDERMAN, EntityType.WITCH, EntityType.SLIME, EntityType.MAGMA_CUBE,
                EntityType.BLAZE, EntityType.GHAST, EntityType.WITHER_SKELETON, EntityType.ZOMBIFIED_PIGLIN,
                EntityType.PIGLIN, EntityType.PIGLIN_BRUTE, EntityType.HOGLIN, EntityType.ZOGLIN,
                EntityType.ENDERMITE, EntityType.SILVERFISH, EntityType.GUARDIAN, EntityType.ELDER_GUARDIAN,
                EntityType.SHULKER, EntityType.VEX, EntityType.VINDICATOR, EntityType.EVOKER,
                EntityType.RAVAGER, EntityType.PILLAGER, EntityType.DROWNED, EntityType.HUSK,
                EntityType.STRAY, EntityType.PHANTOM, EntityType.WITHER, EntityType.ENDER_DRAGON
            };
            
            for (EntityType entityType : hostileMobs) {
                try {
                    totalKills += player.getStatistic(Statistic.KILL_ENTITY, entityType);
                } catch (Exception e) {
                    // 某些实体类型可能不支持，忽略错误
                }
            }
            
            return totalKills;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "获取玩家 " + player.getName() + " 击杀统计时出错", e);
            return 0;
        }
    }
    
    /**
     * 获取玩家受到的伤害总量
     * 
     * @param player 玩家
     * @return 受到伤害总量
     */
    public double getDamageTaken(Player player) {
        try {
            // 原版统计中伤害以十分之一心为单位
            int damageDealt = player.getStatistic(Statistic.DAMAGE_TAKEN);
            return damageDealt / 10.0; // 转换为心数
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "获取玩家 " + player.getName() + " 受伤统计时出错", e);
            return 0.0;
        }
    }
    
    /**
     * 获取玩家行走距离（米）
     * 
     * @param player 玩家
     * @return 行走距离（米）
     */
    public double getWalkDistance(Player player) {
        try {
            // 原版统计距离以厘米为单位
            int walkCm = player.getStatistic(Statistic.WALK_ONE_CM);
            int sprintCm = player.getStatistic(Statistic.SPRINT_ONE_CM);
            int crouchCm = player.getStatistic(Statistic.CROUCH_ONE_CM);
            
            // 转换为米
            return (walkCm + sprintCm + crouchCm) / 100.0;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "获取玩家 " + player.getName() + " 移动统计时出错", e);
            return 0.0;
        }
    }
    
    /**
     * 获取玩家游戏时间（小时）
     * 
     * @param player 玩家
     * @return 游戏时间（小时）
     */
    public double getPlayTime(Player player) {
        try {
            // 原版统计时间以tick为单位（20 tick = 1秒）
            int playTimeTicks = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
            return playTimeTicks / (20.0 * 60.0 * 60.0); // 转换为小时
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "获取玩家 " + player.getName() + " 游戏时间统计时出错", e);
            return 0.0;
        }
    }
    
    /**
     * 检查玩家是否挖掘过指定方块
     * 
     * @param player 玩家
     * @param material 方块类型
     * @return 挖掘数量
     */
    public int getBlocksMined(Player player, Material material) {
        try {
            return player.getStatistic(Statistic.MINE_BLOCK, material);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "获取玩家 " + player.getName() + " 挖掘统计时出错: " + material, e);
            return 0;
        }
    }
    
    /**
     * 检查玩家是否拾取过指定物品
     * 
     * @param player 玩家
     * @param material 物品类型
     * @return 拾取数量
     */
    public int getItemsPickedUp(Player player, Material material) {
        try {
            return player.getStatistic(Statistic.PICKUP, material);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "获取玩家 " + player.getName() + " 拾取统计时出错: " + material, e);
            return 0;
        }
    }
    
    // ==================== 称号解锁条件检查 ====================
    
    /**
     * 检查玩家是否满足所有称号的解锁条件
     *
     * @param player 玩家
     */
    public void checkAllUnlockConditions(Player player) {
        if (!isProgressEnabled()) {
            return;
        }

        boolean debugEnabled = plugin.getConfigManager().getConfigValue("debug", false) &&
                              plugin.getConfigManager().getConfigValue("progress.debug-logging", false);

        for (TitleInfo titleInfo : configManager.getAllTitles().values()) {
            // 跳过已解锁的称号
            if (titleManager.getPlayerData(player).hasUnlockedTitle(titleInfo.getId())) {
                continue;
            }

            if (debugEnabled) {
                plugin.getLogger().info("检查玩家 " + player.getName() + " 的称号 " + titleInfo.getId() +
                                      " 解锁条件: " + titleInfo.getUnlockConditions());
            }

            // 检查是否满足解锁条件
            boolean canUnlock = checkTitleUnlockConditions(player, titleInfo);

            if (debugEnabled) {
                plugin.getLogger().info("玩家 " + player.getName() + " 称号 " + titleInfo.getId() +
                                      " 检查结果: " + (canUnlock ? "可解锁" : "不可解锁"));
            }

            if (canUnlock) {
                // 解锁称号
                titleManager.givePlayerTitle(player, titleInfo.getId());

                // 发送解锁消息
                MessageUtil.sendPrefixedMessage(player, "title.title-unlocked",
                    "title", titleInfo.getDisplayName());

                plugin.getLogger().info("玩家 " + player.getName() + " 通过游戏统计解锁称号: " + titleInfo.getDisplayName());
            }
        }
    }
    
    /**
     * 检查单个称号的解锁条件
     *
     * @param player 玩家
     * @param titleInfo 称号信息
     * @return 是否满足条件
     */
    private boolean checkTitleUnlockConditions(Player player, TitleInfo titleInfo) {
        Map<String, Object> conditions = titleInfo.getUnlockConditions();

        // 首先检查特殊条件
        if (conditions.containsKey("auto-unlock")) {
            boolean autoUnlock = (Boolean) conditions.get("auto-unlock");
            if (!autoUnlock) {
                // auto-unlock: false 表示不能通过统计自动解锁
                // 只有在有其他有效条件时才继续检查
                boolean hasOtherConditions = conditions.entrySet().stream()
                    .anyMatch(entry -> !entry.getKey().equals("auto-unlock") &&
                                     !entry.getKey().equals("admin-only") &&
                                     !entry.getKey().equals("default"));
                if (!hasOtherConditions) {
                    return false; // 没有其他解锁条件，不能自动解锁
                }
            }
        }

        if (conditions.containsKey("admin-only")) {
            boolean adminOnly = (Boolean) conditions.get("admin-only");
            if (adminOnly) {
                return false; // 管理员专用称号，不能通过统计解锁
            }
        }

        // 检查统计相关的解锁条件
        for (Map.Entry<String, Object> condition : conditions.entrySet()) {
            String conditionType = condition.getKey();
            Object conditionValue = condition.getValue();

            switch (conditionType) {
                case "auto-unlock":
                case "admin-only":
                case "default":
                    // 这些条件已经在上面处理过了
                    continue;
                    
                case "kill-mobs":
                    int requiredKills = ((Number) conditionValue).intValue();
                    if (getMobKills(player) < requiredKills) {
                        return false;
                    }
                    break;
                    
                case "damage-taken":
                    double requiredDamage = ((Number) conditionValue).doubleValue();
                    if (getDamageTaken(player) < requiredDamage) {
                        return false;
                    }
                    break;
                    
                case "walk-distance":
                    double requiredDistance = ((Number) conditionValue).doubleValue();
                    if (getWalkDistance(player) < requiredDistance) {
                        return false;
                    }
                    break;
                    
                case "play-time":
                    double requiredTime = ((Number) conditionValue).doubleValue();
                    if (getPlayTime(player) < requiredTime) {
                        return false;
                    }
                    break;
                    
                case "special-event":
                    // 特殊事件需要特殊处理
                    if (!checkSpecialEvent(player, (String) conditionValue)) {
                        return false;
                    }
                    break;
                    
                default:
                    plugin.getLogger().warning("未知的解锁条件类型: " + conditionType);
                    continue;
            }
        }
        
        return true;
    }
    
    /**
     * 检查特殊事件条件
     * 
     * @param player 玩家
     * @param eventId 事件ID
     * @return 是否满足条件
     */
    private boolean checkSpecialEvent(Player player, String eventId) {
        switch (eventId) {
            case "find-diamond":
                return getBlocksMined(player, Material.DIAMOND_ORE) > 0 || 
                       getBlocksMined(player, Material.DEEPSLATE_DIAMOND_ORE) > 0;
                       
            case "find-emerald":
                return getBlocksMined(player, Material.EMERALD_ORE) > 0 || 
                       getBlocksMined(player, Material.DEEPSLATE_EMERALD_ORE) > 0;
                       
            case "find-ancient-debris":
                return getBlocksMined(player, Material.ANCIENT_DEBRIS) > 0;
                
            case "find-gold":
                return getBlocksMined(player, Material.GOLD_ORE) > 0 || 
                       getBlocksMined(player, Material.DEEPSLATE_GOLD_ORE) > 0 ||
                       getBlocksMined(player, Material.NETHER_GOLD_ORE) > 0;
                       
            case "find-iron":
                return getBlocksMined(player, Material.IRON_ORE) > 0 || 
                       getBlocksMined(player, Material.DEEPSLATE_IRON_ORE) > 0;
                       
            case "get-diamond":
                return getItemsPickedUp(player, Material.DIAMOND) > 0;
                
            case "get-emerald":
                return getItemsPickedUp(player, Material.EMERALD) > 0;
                
            case "get-netherite":
                return getItemsPickedUp(player, Material.NETHERITE_INGOT) > 0;
                
            case "get-totem":
                return getItemsPickedUp(player, Material.TOTEM_OF_UNDYING) > 0;
                
            case "get-elytra":
                return getItemsPickedUp(player, Material.ELYTRA) > 0;
                
            case "get-nether-star":
                return getItemsPickedUp(player, Material.NETHER_STAR) > 0;
                
            case "get-dragon-egg":
                return getItemsPickedUp(player, Material.DRAGON_EGG) > 0;
                
            default:
                plugin.getLogger().warning("未知的特殊事件: " + eventId);
                return false;
        }
    }





    /**
     * 检查是否启用进度统计功能
     */
    private boolean isProgressEnabled() {
        return plugin.getConfigManager().getConfigValue("progress.enabled", true);
    }
}
