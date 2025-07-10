package com.github.chengge.zprefix.manager;

import com.github.chengge.zprefix.ZPrefix;
import com.github.chengge.zprefix.data.TitleInfo;
import com.github.chengge.zprefix.util.MessageUtil;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

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

    // ==================== 新增基础统计方法 ====================

    /**
     * 获取玩家击杀玩家数量
     *
     * @param player 玩家
     * @return 击杀玩家数量
     */
    public int getPlayerKills(Player player) {
        try {
            return player.getStatistic(Statistic.PLAYER_KILLS);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "获取玩家 " + player.getName() + " PVP击杀统计时出错", e);
            return 0;
        }
    }

    /**
     * 获取玩家死亡次数
     *
     * @param player 玩家
     * @return 死亡次数
     */
    public int getDeaths(Player player) {
        try {
            return player.getStatistic(Statistic.DEATHS);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "获取玩家 " + player.getName() + " 死亡统计时出错", e);
            return 0;
        }
    }

    /**
     * 获取玩家跳跃次数
     *
     * @param player 玩家
     * @return 跳跃次数
     */
    public int getJumps(Player player) {
        try {
            return player.getStatistic(Statistic.JUMP);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "获取玩家 " + player.getName() + " 跳跃统计时出错", e);
            return 0;
        }
    }

    /**
     * 获取玩家钓鱼数量
     *
     * @param player 玩家
     * @return 钓鱼数量
     */
    public int getFishCaught(Player player) {
        try {
            return player.getStatistic(Statistic.FISH_CAUGHT);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "获取玩家 " + player.getName() + " 钓鱼统计时出错", e);
            return 0;
        }
    }

    /**
     * 获取玩家繁殖动物数量
     *
     * @param player 玩家
     * @return 繁殖动物数量
     */
    public int getAnimalsBred(Player player) {
        try {
            return player.getStatistic(Statistic.ANIMALS_BRED);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "获取玩家 " + player.getName() + " 繁殖统计时出错", e);
            return 0;
        }
    }

    /**
     * 获取玩家制作物品总数
     *
     * @param player 玩家
     * @return 制作物品总数
     */
    public int getItemsCrafted(Player player) {
        try {
            // 获取所有制作相关的统计
            int totalCrafted = 0;

            // 遍历所有可制作的物品
            for (Material material : Material.values()) {
                if (material.isItem()) {
                    try {
                        totalCrafted += player.getStatistic(Statistic.CRAFT_ITEM, material);
                    } catch (Exception e) {
                        // 某些物品可能不支持制作统计，忽略错误
                    }
                }
            }

            return totalCrafted;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "获取玩家 " + player.getName() + " 制作统计时出错", e);
            return 0;
        }
    }

    /**
     * 获取玩家附魔物品数量
     *
     * @param player 玩家
     * @return 附魔物品数量
     */
    public int getItemsEnchanted(Player player) {
        try {
            return player.getStatistic(Statistic.ITEM_ENCHANTED);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "获取玩家 " + player.getName() + " 附魔统计时出错", e);
            return 0;
        }
    }

    /**
     * 获取玩家造成的伤害总量
     *
     * @param player 玩家
     * @return 造成伤害总量
     */
    public double getDamageDealt(Player player) {
        try {
            // 原版统计中伤害以十分之一心为单位
            int damageDealt = player.getStatistic(Statistic.DAMAGE_DEALT);
            return damageDealt / 10.0; // 转换为心数
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "获取玩家 " + player.getName() + " 伤害输出统计时出错", e);
            return 0.0;
        }
    }

    /**
     * 获取玩家破坏方块总数
     *
     * @param player 玩家
     * @return 破坏方块总数
     */
    public int getBlocksBroken(Player player) {
        try {
            // 获取所有破坏方块的统计
            int totalBroken = 0;

            // 遍历所有可破坏的方块
            for (Material material : Material.values()) {
                if (material.isBlock()) {
                    try {
                        totalBroken += player.getStatistic(Statistic.MINE_BLOCK, material);
                    } catch (Exception e) {
                        // 某些方块可能不支持挖掘统计，忽略错误
                    }
                }
            }

            return totalBroken;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "获取玩家 " + player.getName() + " 方块破坏统计时出错", e);
            return 0;
        }
    }

    /**
     * 获取玩家总移动距离（包括所有移动方式）
     *
     * @param player 玩家
     * @return 总移动距离（米）
     */
    public double getTotalDistance(Player player) {
        try {
            // 获取所有移动方式的距离
            int walkCm = player.getStatistic(Statistic.WALK_ONE_CM);
            int sprintCm = player.getStatistic(Statistic.SPRINT_ONE_CM);
            int crouchCm = player.getStatistic(Statistic.CROUCH_ONE_CM);
            int swimCm = player.getStatistic(Statistic.SWIM_ONE_CM);
            int flyCm = player.getStatistic(Statistic.FLY_ONE_CM);
            int climbCm = player.getStatistic(Statistic.CLIMB_ONE_CM);
            int fallCm = player.getStatistic(Statistic.FALL_ONE_CM);
            int horseCm = player.getStatistic(Statistic.HORSE_ONE_CM);
            int pigCm = player.getStatistic(Statistic.PIG_ONE_CM);
            int boatCm = player.getStatistic(Statistic.BOAT_ONE_CM);
            int minecartCm = player.getStatistic(Statistic.MINECART_ONE_CM);
            int elytraCm = player.getStatistic(Statistic.AVIATE_ONE_CM);

            // 转换为米
            return (walkCm + sprintCm + crouchCm + swimCm + flyCm + climbCm +
                   fallCm + horseCm + pigCm + boatCm + minecartCm + elytraCm) / 100.0;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "获取玩家 " + player.getName() + " 总移动距离统计时出错", e);
            return 0.0;
        }
    }

    // ==================== 数量型特殊事件方法 ====================

    /**
     * 获取玩家挖掘的钻石数量
     *
     * @param player 玩家
     * @return 钻石挖掘数量
     */
    public int getDiamondsMined(Player player) {
        try {
            return getBlocksMined(player, Material.DIAMOND_ORE) +
                   getBlocksMined(player, Material.DEEPSLATE_DIAMOND_ORE);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "获取玩家 " + player.getName() + " 钻石挖掘统计时出错", e);
            return 0;
        }
    }

    /**
     * 获取玩家挖掘的绿宝石数量
     *
     * @param player 玩家
     * @return 绿宝石挖掘数量
     */
    public int getEmeraldsMined(Player player) {
        try {
            return getBlocksMined(player, Material.EMERALD_ORE) +
                   getBlocksMined(player, Material.DEEPSLATE_EMERALD_ORE);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "获取玩家 " + player.getName() + " 绿宝石挖掘统计时出错", e);
            return 0;
        }
    }

    /**
     * 获取玩家挖掘的远古残骸数量
     *
     * @param player 玩家
     * @return 远古残骸挖掘数量
     */
    public int getAncientDebrisMined(Player player) {
        try {
            return getBlocksMined(player, Material.ANCIENT_DEBRIS);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "获取玩家 " + player.getName() + " 远古残骸挖掘统计时出错", e);
            return 0;
        }
    }

    /**
     * 获取玩家挖掘的铜矿数量
     *
     * @param player 玩家
     * @return 铜矿挖掘数量
     */
    public int getCopperMined(Player player) {
        try {
            return getBlocksMined(player, Material.COPPER_ORE) +
                   getBlocksMined(player, Material.DEEPSLATE_COPPER_ORE);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "获取玩家 " + player.getName() + " 铜矿挖掘统计时出错", e);
            return 0;
        }
    }

    /**
     * 获取玩家挖掘的青金石数量
     *
     * @param player 玩家
     * @return 青金石挖掘数量
     */
    public int getLapisMined(Player player) {
        try {
            return getBlocksMined(player, Material.LAPIS_ORE) +
                   getBlocksMined(player, Material.DEEPSLATE_LAPIS_ORE);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "获取玩家 " + player.getName() + " 青金石挖掘统计时出错", e);
            return 0;
        }
    }

    /**
     * 获取玩家挖掘的红石数量
     *
     * @param player 玩家
     * @return 红石挖掘数量
     */
    public int getRedstoneMined(Player player) {
        try {
            return getBlocksMined(player, Material.REDSTONE_ORE) +
                   getBlocksMined(player, Material.DEEPSLATE_REDSTONE_ORE);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "获取玩家 " + player.getName() + " 红石挖掘统计时出错", e);
            return 0;
        }
    }

    /**
     * 获取玩家挖掘的石英数量
     *
     * @param player 玩家
     * @return 石英挖掘数量
     */
    public int getQuartzMined(Player player) {
        try {
            return getBlocksMined(player, Material.NETHER_QUARTZ_ORE);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "获取玩家 " + player.getName() + " 石英挖掘统计时出错", e);
            return 0;
        }
    }

    /**
     * 获取玩家挖掘的金矿数量
     *
     * @param player 玩家
     * @return 金矿挖掘数量
     */
    public int getGoldMined(Player player) {
        try {
            return getBlocksMined(player, Material.GOLD_ORE) +
                   getBlocksMined(player, Material.DEEPSLATE_GOLD_ORE) +
                   getBlocksMined(player, Material.NETHER_GOLD_ORE);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "获取玩家 " + player.getName() + " 金矿挖掘统计时出错", e);
            return 0;
        }
    }

    /**
     * 获取玩家挖掘的铁矿数量
     *
     * @param player 玩家
     * @return 铁矿挖掘数量
     */
    public int getIronMined(Player player) {
        try {
            return getBlocksMined(player, Material.IRON_ORE) +
                   getBlocksMined(player, Material.DEEPSLATE_IRON_ORE);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "获取玩家 " + player.getName() + " 铁矿挖掘统计时出错", e);
            return 0;
        }
    }

    /**
     * 获取玩家挖掘的煤矿数量
     *
     * @param player 玩家
     * @return 煤矿挖掘数量
     */
    public int getCoalMined(Player player) {
        try {
            return getBlocksMined(player, Material.COAL_ORE) +
                   getBlocksMined(player, Material.DEEPSLATE_COAL_ORE);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "获取玩家 " + player.getName() + " 煤矿挖掘统计时出错", e);
            return 0;
        }
    }

    /**
     * 获取玩家获得的钻石物品数量
     *
     * @param player 玩家
     * @return 钻石物品数量
     */
    public int getDiamondsObtained(Player player) {
        try {
            return getItemsPickedUp(player, Material.DIAMOND);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "获取玩家 " + player.getName() + " 钻石获得统计时出错", e);
            return 0;
        }
    }

    /**
     * 获取玩家获得的绿宝石物品数量
     *
     * @param player 玩家
     * @return 绿宝石物品数量
     */
    public int getEmeraldsObtained(Player player) {
        try {
            return getItemsPickedUp(player, Material.EMERALD);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "获取玩家 " + player.getName() + " 绿宝石获得统计时出错", e);
            return 0;
        }
    }

    /**
     * 获取玩家获得的下界合金锭数量
     *
     * @param player 玩家
     * @return 下界合金锭数量
     */
    public int getNetheriteObtained(Player player) {
        try {
            return getItemsPickedUp(player, Material.NETHERITE_INGOT);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "获取玩家 " + player.getName() + " 下界合金锭获得统计时出错", e);
            return 0;
        }
    }

    // ==================== 扩展特殊事件方法 ====================

    /**
     * 获取玩家击杀特定实体的数量
     *
     * @param player 玩家
     * @param entityType 实体类型
     * @return 击杀数量
     */
    public int getEntityKills(Player player, EntityType entityType) {
        try {
            return player.getStatistic(Statistic.KILL_ENTITY, entityType);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "获取玩家 " + player.getName() + " 击杀 " + entityType + " 统计时出错", e);
            return 0;
        }
    }

    /**
     * 检查玩家是否进入过下界
     *
     * @param player 玩家
     * @return 是否进入过下界
     */
    public boolean hasEnteredNether(Player player) {
        try {
            // 通过检查下界相关的统计来判断
            return getBlocksMined(player, Material.NETHERRACK) > 0 ||
                   getBlocksMined(player, Material.NETHER_GOLD_ORE) > 0 ||
                   getBlocksMined(player, Material.NETHER_QUARTZ_ORE) > 0 ||
                   getItemsPickedUp(player, Material.BLAZE_ROD) > 0 ||
                   getItemsPickedUp(player, Material.GHAST_TEAR) > 0;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "检查玩家 " + player.getName() + " 下界探索统计时出错", e);
            return false;
        }
    }

    /**
     * 检查玩家是否进入过末地
     *
     * @param player 玩家
     * @return 是否进入过末地
     */
    public boolean hasEnteredEnd(Player player) {
        try {
            // 通过检查末地相关的统计来判断
            return getBlocksMined(player, Material.END_STONE) > 0 ||
                   getItemsPickedUp(player, Material.ENDER_PEARL) > 0 ||
                   getItemsPickedUp(player, Material.CHORUS_FRUIT) > 0 ||
                   getEntityKills(player, EntityType.ENDERMAN) > 0 ||
                   getEntityKills(player, EntityType.ENDER_DRAGON) > 0;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "检查玩家 " + player.getName() + " 末地探索统计时出错", e);
            return false;
        }
    }



    /**
     * 获取玩家与村民交易次数
     *
     * @param player 玩家
     * @return 交易次数
     */
    public int getVillagerTrades(Player player) {
        try {
            return player.getStatistic(Statistic.TRADED_WITH_VILLAGER);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "获取玩家 " + player.getName() + " 村民交易统计时出错", e);
            return 0;
        }
    }

    /**
     * 获取玩家吃食物的总数
     *
     * @param player 玩家
     * @return 吃食物总数
     */
    public int getFoodEaten(Player player) {
        try {
            // 获取所有食物相关的统计
            int totalEaten = 0;

            // 常见食物列表
            Material[] foods = {
                Material.BREAD, Material.APPLE, Material.GOLDEN_APPLE, Material.ENCHANTED_GOLDEN_APPLE,
                Material.COOKED_BEEF, Material.COOKED_PORKCHOP, Material.COOKED_CHICKEN, Material.COOKED_MUTTON,
                Material.COOKED_RABBIT, Material.COOKED_COD, Material.COOKED_SALMON, Material.BAKED_POTATO,
                Material.CARROT, Material.POTATO, Material.BEETROOT, Material.MELON_SLICE, Material.SWEET_BERRIES,
                Material.COOKIE, Material.CAKE, Material.PUMPKIN_PIE, Material.MUSHROOM_STEW, Material.RABBIT_STEW,
                Material.BEETROOT_SOUP, Material.SUSPICIOUS_STEW, Material.HONEY_BOTTLE
            };

            for (Material food : foods) {
                try {
                    totalEaten += player.getStatistic(Statistic.USE_ITEM, food);
                } catch (Exception e) {
                    // 某些食物可能不支持统计，忽略错误
                }
            }

            return totalEaten;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "获取玩家 " + player.getName() + " 食物消费统计时出错", e);
            return 0;
        }
    }

    /**
     * 获取玩家喝药水的次数
     *
     * @param player 玩家
     * @return 喝药水次数
     */
    public int getPotionsDrunk(Player player) {
        try {
            // 获取所有药水相关的统计
            int totalDrunk = 0;

            // 药水类型列表
            Material[] potions = {
                Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION,
                Material.MILK_BUCKET, Material.HONEY_BOTTLE
            };

            for (Material potion : potions) {
                try {
                    totalDrunk += player.getStatistic(Statistic.USE_ITEM, potion);
                } catch (Exception e) {
                    // 某些药水可能不支持统计，忽略错误
                }
            }

            return totalDrunk;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "获取玩家 " + player.getName() + " 药水消费统计时出错", e);
            return 0;
        }
    }

    /**
     * 获取玩家损坏工具的次数
     *
     * @param player 玩家
     * @return 损坏工具次数
     */
    public int getToolsBroken(Player player) {
        try {
            return player.getStatistic(Statistic.BREAK_ITEM, Material.DIAMOND_PICKAXE) +
                   player.getStatistic(Statistic.BREAK_ITEM, Material.DIAMOND_AXE) +
                   player.getStatistic(Statistic.BREAK_ITEM, Material.DIAMOND_SHOVEL) +
                   player.getStatistic(Statistic.BREAK_ITEM, Material.DIAMOND_HOE) +
                   player.getStatistic(Statistic.BREAK_ITEM, Material.DIAMOND_SWORD) +
                   player.getStatistic(Statistic.BREAK_ITEM, Material.IRON_PICKAXE) +
                   player.getStatistic(Statistic.BREAK_ITEM, Material.IRON_AXE) +
                   player.getStatistic(Statistic.BREAK_ITEM, Material.IRON_SHOVEL) +
                   player.getStatistic(Statistic.BREAK_ITEM, Material.IRON_HOE) +
                   player.getStatistic(Statistic.BREAK_ITEM, Material.IRON_SWORD);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "获取玩家 " + player.getName() + " 工具损坏统计时出错", e);
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

                // 新增基础统计参数
                case "player-kills":
                    int requiredPlayerKills = ((Number) conditionValue).intValue();
                    if (getPlayerKills(player) < requiredPlayerKills) {
                        return false;
                    }
                    break;

                case "deaths":
                    int requiredDeaths = ((Number) conditionValue).intValue();
                    if (getDeaths(player) < requiredDeaths) {
                        return false;
                    }
                    break;

                case "jump":
                    int requiredJumps = ((Number) conditionValue).intValue();
                    if (getJumps(player) < requiredJumps) {
                        return false;
                    }
                    break;

                case "fish-caught":
                    int requiredFish = ((Number) conditionValue).intValue();
                    if (getFishCaught(player) < requiredFish) {
                        return false;
                    }
                    break;

                case "animals-bred":
                    int requiredBreeding = ((Number) conditionValue).intValue();
                    if (getAnimalsBred(player) < requiredBreeding) {
                        return false;
                    }
                    break;

                case "items-crafted":
                    int requiredCrafting = ((Number) conditionValue).intValue();
                    if (getItemsCrafted(player) < requiredCrafting) {
                        return false;
                    }
                    break;

                case "items-enchanted":
                    int requiredEnchanting = ((Number) conditionValue).intValue();
                    if (getItemsEnchanted(player) < requiredEnchanting) {
                        return false;
                    }
                    break;

                case "damage-dealt":
                    double requiredDamageDealt = ((Number) conditionValue).doubleValue();
                    if (getDamageDealt(player) < requiredDamageDealt) {
                        return false;
                    }
                    break;

                case "blocks-broken":
                    int requiredBlocksBroken = ((Number) conditionValue).intValue();
                    if (getBlocksBroken(player) < requiredBlocksBroken) {
                        return false;
                    }
                    break;

                case "distance-traveled":
                    double requiredTotalDistance = ((Number) conditionValue).doubleValue();
                    if (getTotalDistance(player) < requiredTotalDistance) {
                        return false;
                    }
                    break;

                // 矿物挖掘数量统计
                case "diamonds-mined":
                    int requiredDiamonds = ((Number) conditionValue).intValue();
                    if (getDiamondsMined(player) < requiredDiamonds) {
                        return false;
                    }
                    break;

                case "emeralds-mined":
                    int requiredEmeralds = ((Number) conditionValue).intValue();
                    if (getEmeraldsMined(player) < requiredEmeralds) {
                        return false;
                    }
                    break;

                case "ancient-debris-mined":
                    int requiredDebris = ((Number) conditionValue).intValue();
                    if (getAncientDebrisMined(player) < requiredDebris) {
                        return false;
                    }
                    break;

                case "gold-mined":
                    int requiredGold = ((Number) conditionValue).intValue();
                    if (getGoldMined(player) < requiredGold) {
                        return false;
                    }
                    break;

                case "iron-mined":
                    int requiredIron = ((Number) conditionValue).intValue();
                    if (getIronMined(player) < requiredIron) {
                        return false;
                    }
                    break;

                case "coal-mined":
                    int requiredCoal = ((Number) conditionValue).intValue();
                    if (getCoalMined(player) < requiredCoal) {
                        return false;
                    }
                    break;

                case "copper-mined":
                    int requiredCopper = ((Number) conditionValue).intValue();
                    if (getCopperMined(player) < requiredCopper) {
                        return false;
                    }
                    break;

                case "lapis-mined":
                    int requiredLapis = ((Number) conditionValue).intValue();
                    if (getLapisMined(player) < requiredLapis) {
                        return false;
                    }
                    break;

                case "redstone-mined":
                    int requiredRedstone = ((Number) conditionValue).intValue();
                    if (getRedstoneMined(player) < requiredRedstone) {
                        return false;
                    }
                    break;

                case "quartz-mined":
                    int requiredQuartz = ((Number) conditionValue).intValue();
                    if (getQuartzMined(player) < requiredQuartz) {
                        return false;
                    }
                    break;

                // Boss击杀数量统计
                case "ender-dragons-killed":
                    int requiredDragons = ((Number) conditionValue).intValue();
                    if (getEntityKills(player, EntityType.ENDER_DRAGON) < requiredDragons) {
                        return false;
                    }
                    break;

                case "withers-killed":
                    int requiredWithers = ((Number) conditionValue).intValue();
                    if (getEntityKills(player, EntityType.WITHER) < requiredWithers) {
                        return false;
                    }
                    break;

                case "elder-guardians-killed":
                    int requiredElderGuardians = ((Number) conditionValue).intValue();
                    if (getEntityKills(player, EntityType.ELDER_GUARDIAN) < requiredElderGuardians) {
                        return false;
                    }
                    break;

                case "wardens-killed":
                    int requiredWardens = ((Number) conditionValue).intValue();
                    if (getEntityKills(player, EntityType.WARDEN) < requiredWardens) {
                        return false;
                    }
                    break;

                // 怪物击杀数量统计
                case "zombies-killed":
                    int requiredZombies = ((Number) conditionValue).intValue();
                    if (getEntityKills(player, EntityType.ZOMBIE) < requiredZombies) {
                        return false;
                    }
                    break;

                case "skeletons-killed":
                    int requiredSkeletons = ((Number) conditionValue).intValue();
                    if (getEntityKills(player, EntityType.SKELETON) < requiredSkeletons) {
                        return false;
                    }
                    break;

                case "creepers-killed":
                    int requiredCreepers = ((Number) conditionValue).intValue();
                    if (getEntityKills(player, EntityType.CREEPER) < requiredCreepers) {
                        return false;
                    }
                    break;

                case "spiders-killed":
                    int requiredSpiders = ((Number) conditionValue).intValue();
                    if (getEntityKills(player, EntityType.SPIDER) < requiredSpiders) {
                        return false;
                    }
                    break;

                case "endermen-killed":
                    int requiredEndermen = ((Number) conditionValue).intValue();
                    if (getEntityKills(player, EntityType.ENDERMAN) < requiredEndermen) {
                        return false;
                    }
                    break;

                // 动物击杀数量统计
                case "cows-killed":
                    int requiredCows = ((Number) conditionValue).intValue();
                    if (getEntityKills(player, EntityType.COW) < requiredCows) {
                        return false;
                    }
                    break;

                case "pigs-killed":
                    int requiredPigs = ((Number) conditionValue).intValue();
                    if (getEntityKills(player, EntityType.PIG) < requiredPigs) {
                        return false;
                    }
                    break;

                case "sheep-killed":
                    int requiredSheep = ((Number) conditionValue).intValue();
                    if (getEntityKills(player, EntityType.SHEEP) < requiredSheep) {
                        return false;
                    }
                    break;

                case "chickens-killed":
                    int requiredChickens = ((Number) conditionValue).intValue();
                    if (getEntityKills(player, EntityType.CHICKEN) < requiredChickens) {
                        return false;
                    }
                    break;

                // 生活活动数量统计
                case "villager-trades":
                    int requiredTrades = ((Number) conditionValue).intValue();
                    if (getVillagerTrades(player) < requiredTrades) {
                        return false;
                    }
                    break;

                case "food-eaten":
                    int requiredFoodEaten = ((Number) conditionValue).intValue();
                    if (getFoodEaten(player) < requiredFoodEaten) {
                        return false;
                    }
                    break;

                case "potions-drunk":
                    int requiredPotionsDrunk = ((Number) conditionValue).intValue();
                    if (getPotionsDrunk(player) < requiredPotionsDrunk) {
                        return false;
                    }
                    break;

                case "tools-broken":
                    int requiredToolsBroken = ((Number) conditionValue).intValue();
                    if (getToolsBroken(player) < requiredToolsBroken) {
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
            // 稀有物品获得事件
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

            // 维度探索事件
            case "enter-nether":
                return hasEnteredNether(player);

            case "enter-end":
                return hasEnteredEnd(player);

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
