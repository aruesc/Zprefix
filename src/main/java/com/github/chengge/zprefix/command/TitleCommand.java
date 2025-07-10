package com.github.chengge.zprefix.command;

import com.github.chengge.zprefix.ZPrefix;
import com.github.chengge.zprefix.data.PlayerTitleData;
import com.github.chengge.zprefix.data.TitleInfo;
import com.github.chengge.zprefix.gui.TitleGUI;
import com.github.chengge.zprefix.manager.BuffManager;
import com.github.chengge.zprefix.manager.ConfigManager;
import com.github.chengge.zprefix.manager.TitleManager;
import com.github.chengge.zprefix.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 称号命令处理器
 * 处理所有与称号相关的命令
 */
public class TitleCommand implements CommandExecutor, TabCompleter {
    
    private final ZPrefix plugin;
    private final ConfigManager configManager;
    private final TitleManager titleManager;
    private final BuffManager buffManager;
    private final TitleGUI titleGUI;
    
    public TitleCommand(ZPrefix plugin, ConfigManager configManager, TitleManager titleManager, 
                      BuffManager buffManager, TitleGUI titleGUI) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.titleManager = titleManager;
        this.buffManager = buffManager;
        this.titleGUI = titleGUI;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 如果没有参数，显示帮助或打开GUI
        if (args.length == 0) {
            if (sender instanceof Player) {
                titleGUI.openGUI((Player) sender);
            } else {
                showHelp(sender);
            }
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "gui":
                return handleGUICommand(sender);
            case "set":
                return handleSetCommand(sender, args);
            case "remove":
                return handleRemoveCommand(sender);
            case "list":
                return handleListCommand(sender);
            case "info":
                return handleInfoCommand(sender, args);
            case "give":
                return handleGiveCommand(sender, args);
            case "take":
                return handleTakeCommand(sender, args);
            case "reload":
                return handleReloadCommand(sender);
            case "cleanup":
                return handleCleanupCommand(sender, args);
            case "help":
            default:
                showHelp(sender);
                return true;
        }
    }
    
    /**
     * 处理GUI命令
     */
    private boolean handleGUICommand(CommandSender sender) {
        if (!MessageUtil.checkPlayer(sender)) {
            return true;
        }
        
        Player player = (Player) sender;
        titleGUI.openGUI(player);
        return true;
    }
    
    /**
     * 处理设置称号命令
     */
    private boolean handleSetCommand(CommandSender sender, String[] args) {
        if (!MessageUtil.checkPlayer(sender)) {
            return true;
        }

        if (args.length < 2) {
            MessageUtil.sendPrefixedMessage(sender, "common.invalid-args", "usage", "/title set <称号显示名>");
            return true;
        }

        Player player = (Player) sender;

        // 将参数拼接为完整的称号显示名（支持带空格的称号名）
        StringBuilder titleNameBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) {
                titleNameBuilder.append(" ");
            }
            titleNameBuilder.append(args[i]);
        }
        String titleDisplayName = titleNameBuilder.toString();

        // 通过显示名查找称号ID
        String titleId = findTitleIdByDisplayName(titleDisplayName);

        if (titleId == null) {
            MessageUtil.sendPrefixedMessage(sender, "title.title-not-found", "title", titleDisplayName);
            return true;
        }

        // 检查称号是否存在（双重验证）
        if (!configManager.titleExists(titleId)) {
            MessageUtil.sendPrefixedMessage(sender, "title.title-not-found", "title", titleDisplayName);
            return true;
        }

        // 检查是否已解锁
        if (!titleManager.hasTitle(player, titleId)) {
            TitleInfo titleInfo = configManager.getTitleInfo(titleId);
            MessageUtil.sendPrefixedMessage(sender, "title.title-not-unlocked");
            return true;
        }

        // 设置称号
        if (titleManager.setPlayerTitle(player, titleId)) {
            TitleInfo titleInfo = configManager.getTitleInfo(titleId);
            MessageUtil.sendPrefixedMessage(sender, "title.title-set", "title", titleInfo.getDisplayName());
        } else {
            MessageUtil.sendPrefixedMessage(sender, "title.title-not-unlocked");
        }

        return true;
    }
    
    /**
     * 处理移除称号命令
     */
    private boolean handleRemoveCommand(CommandSender sender) {
        if (!MessageUtil.checkPlayer(sender)) {
            return true;
        }
        
        Player player = (Player) sender;
        PlayerTitleData playerData = titleManager.getPlayerData(player);
        
        if (!playerData.hasCurrentTitle()) {
            MessageUtil.sendPrefixedMessage(sender, "title.no-current-title");
            return true;
        }
        
        titleManager.removePlayerTitle(player);
        MessageUtil.sendPrefixedMessage(sender, "title.title-removed");
        return true;
    }
    
    /**
     * 处理列出称号命令
     */
    private boolean handleListCommand(CommandSender sender) {
        if (!MessageUtil.checkPlayer(sender)) {
            return true;
        }
        
        Player player = (Player) sender;
        PlayerTitleData playerData = titleManager.getPlayerData(player);
        Set<String> unlockedTitles = playerData.getUnlockedTitles();
        
        if (unlockedTitles.isEmpty()) {
            MessageUtil.sendPrefixedMessage(sender, "title.no-titles");
            return true;
        }
        
        // 构建称号列表
        List<String> titleNames = new ArrayList<>();
        for (String titleId : unlockedTitles) {
            TitleInfo titleInfo = configManager.getTitleInfo(titleId);
            if (titleInfo != null) {
                String displayName = titleInfo.getDisplayName();
                if (titleId.equals(playerData.getCurrentTitle())) {
                    displayName += " §a(当前)";
                }
                titleNames.add(displayName);
            }
        }
        
        String titleList = String.join("§7, ", titleNames);

        // 发送称号列表，包含数量信息
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("titles", titleList);
        placeholders.put("count", String.valueOf(unlockedTitles.size()));
        MessageUtil.sendPrefixedMessage(sender, "title.unlocked-titles", placeholders);

        // 显示统计信息
        int totalTitles = configManager.getAllTitles().size();
        Map<String, String> countPlaceholders = new HashMap<>();
        countPlaceholders.put("unlocked", String.valueOf(unlockedTitles.size()));
        countPlaceholders.put("total", String.valueOf(totalTitles));
        MessageUtil.sendPrefixedMessage(sender, "title.title-count", countPlaceholders);

        return true;
    }
    
    /**
     * 处理称号信息命令
     */
    private boolean handleInfoCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            MessageUtil.sendPrefixedMessage(sender, "common.invalid-args", "usage", "/title info <称号显示名>");
            return true;
        }

        // 将参数拼接为完整的称号显示名（支持带空格的称号名）
        StringBuilder titleNameBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) {
                titleNameBuilder.append(" ");
            }
            titleNameBuilder.append(args[i]);
        }
        String titleDisplayName = titleNameBuilder.toString();

        // 通过显示名查找称号ID
        String titleId = findTitleIdByDisplayName(titleDisplayName);

        if (titleId == null) {
            MessageUtil.sendPrefixedMessage(sender, "title.title-not-found", "title", titleDisplayName);
            return true;
        }

        TitleInfo titleInfo = configManager.getTitleInfo(titleId);

        if (titleInfo == null) {
            MessageUtil.sendPrefixedMessage(sender, "title.title-not-found", "title", titleDisplayName);
            return true;
        }
        
        // 显示称号信息 - 简化版本
        sender.sendMessage("§6=== §f" + titleInfo.getDisplayName() + " §6称号信息 ===");
        sender.sendMessage("");

        // 显示解锁条件
        Map<String, Object> unlockConditions = titleInfo.getUnlockConditions();
        if (unlockConditions != null && !unlockConditions.isEmpty()) {
            sender.sendMessage("§6解锁条件:");
            for (Map.Entry<String, Object> condition : unlockConditions.entrySet()) {
                String conditionType = condition.getKey();
                Object conditionValue = condition.getValue();

                switch (conditionType) {
                    case "auto-unlock":
                        boolean autoUnlock = (Boolean) conditionValue;
                        if (autoUnlock) {
                            sender.sendMessage("  §a✓ 自动解锁 (加入服务器时获得)");
                        } else {
                            sender.sendMessage("  §c✗ 不自动解锁");
                        }
                        break;

                    case "admin-only":
                        boolean adminOnly = (Boolean) conditionValue;
                        if (adminOnly) {
                            sender.sendMessage("  §c⚠ 管理员专用 (只能由管理员给予)");
                        }
                        break;

                    // 基础统计参数
                    case "kill-mobs":
                        int requiredKills = ((Number) conditionValue).intValue();
                        sender.sendMessage("  §7⚔ 击杀怪物: §e" + requiredKills + " §7只");
                        break;

                    case "player-kills":
                        int requiredPlayerKills = ((Number) conditionValue).intValue();
                        sender.sendMessage("  §7⚔ PVP击杀: §e" + requiredPlayerKills + " §7次");
                        break;

                    case "damage-taken":
                        double requiredDamage = ((Number) conditionValue).doubleValue();
                        sender.sendMessage("  §7💔 受到伤害: §e" + String.format("%.1f", requiredDamage) + " §7点");
                        break;

                    case "damage-dealt":
                        double requiredDamageDealt = ((Number) conditionValue).doubleValue();
                        sender.sendMessage("  §7⚔ 造成伤害: §e" + String.format("%.1f", requiredDamageDealt) + " §7点");
                        break;

                    case "walk-distance":
                        double requiredDistance = ((Number) conditionValue).doubleValue();
                        sender.sendMessage("  §7🚶 行走距离: §e" + String.format("%.0f", requiredDistance) + " §7米");
                        break;

                    case "distance-traveled":
                        double requiredTotalDistance = ((Number) conditionValue).doubleValue();
                        sender.sendMessage("  §7🌍 总移动距离: §e" + String.format("%.0f", requiredTotalDistance) + " §7米");
                        break;

                    case "play-time":
                        double requiredTime = ((Number) conditionValue).doubleValue();
                        sender.sendMessage("  §7⏰ 游戏时间: §e" + String.format("%.1f", requiredTime) + " §7小时");
                        break;

                    case "deaths":
                        int requiredDeaths = ((Number) conditionValue).intValue();
                        sender.sendMessage("  §7💀 死亡次数: §e" + requiredDeaths + " §7次");
                        break;

                    case "jump":
                        int requiredJumps = ((Number) conditionValue).intValue();
                        sender.sendMessage("  §7🦘 跳跃次数: §e" + requiredJumps + " §7次");
                        break;

                    case "fish-caught":
                        int requiredFish = ((Number) conditionValue).intValue();
                        sender.sendMessage("  §7🎣 钓鱼次数: §e" + requiredFish + " §7次");
                        break;

                    case "animals-bred":
                        int requiredBreeding = ((Number) conditionValue).intValue();
                        sender.sendMessage("  §7🐄 繁殖动物: §e" + requiredBreeding + " §7只");
                        break;

                    case "items-crafted":
                        int requiredCrafting = ((Number) conditionValue).intValue();
                        sender.sendMessage("  §7🔨 制作物品: §e" + requiredCrafting + " §7个");
                        break;

                    case "items-enchanted":
                        int requiredEnchanting = ((Number) conditionValue).intValue();
                        sender.sendMessage("  §7✨ 附魔物品: §e" + requiredEnchanting + " §7个");
                        break;

                    case "blocks-broken":
                        int requiredBlocksBroken = ((Number) conditionValue).intValue();
                        sender.sendMessage("  §7⛏ 破坏方块: §e" + requiredBlocksBroken + " §7个");
                        break;

                    // 矿物挖掘数量统计
                    case "diamonds-mined":
                        int requiredDiamonds = ((Number) conditionValue).intValue();
                        sender.sendMessage("  §7💎 挖掘钻石矿: §e" + requiredDiamonds + " §7个");
                        break;

                    case "emeralds-mined":
                        int requiredEmeralds = ((Number) conditionValue).intValue();
                        sender.sendMessage("  §7💚 挖掘绿宝石矿: §e" + requiredEmeralds + " §7个");
                        break;

                    case "ancient-debris-mined":
                        int requiredDebris = ((Number) conditionValue).intValue();
                        sender.sendMessage("  §7🔥 挖掘远古残骸: §e" + requiredDebris + " §7个");
                        break;

                    case "gold-mined":
                        int requiredGold = ((Number) conditionValue).intValue();
                        sender.sendMessage("  §7🟨 挖掘金矿: §e" + requiredGold + " §7个");
                        break;

                    case "iron-mined":
                        int requiredIron = ((Number) conditionValue).intValue();
                        sender.sendMessage("  §7⚪ 挖掘铁矿: §e" + requiredIron + " §7个");
                        break;

                    case "coal-mined":
                        int requiredCoal = ((Number) conditionValue).intValue();
                        sender.sendMessage("  §7⚫ 挖掘煤矿: §e" + requiredCoal + " §7个");
                        break;

                    case "copper-mined":
                        int requiredCopper = ((Number) conditionValue).intValue();
                        sender.sendMessage("  §7🟫 挖掘铜矿: §e" + requiredCopper + " §7个");
                        break;

                    case "lapis-mined":
                        int requiredLapis = ((Number) conditionValue).intValue();
                        sender.sendMessage("  §7🔵 挖掘青金石矿: §e" + requiredLapis + " §7个");
                        break;

                    case "redstone-mined":
                        int requiredRedstone = ((Number) conditionValue).intValue();
                        sender.sendMessage("  §7🔴 挖掘红石矿: §e" + requiredRedstone + " §7个");
                        break;

                    case "quartz-mined":
                        int requiredQuartz = ((Number) conditionValue).intValue();
                        sender.sendMessage("  §7⚪ 挖掘石英矿: §e" + requiredQuartz + " §7个");
                        break;

                    // Boss击杀数量统计
                    case "ender-dragons-killed":
                        int requiredDragons = ((Number) conditionValue).intValue();
                        sender.sendMessage("  §7🐉 击杀末影龙: §e" + requiredDragons + " §7条");
                        break;

                    case "withers-killed":
                        int requiredWithers = ((Number) conditionValue).intValue();
                        sender.sendMessage("  §7💀 击杀凋零: §e" + requiredWithers + " §7只");
                        break;

                    case "elder-guardians-killed":
                        int requiredElderGuardians = ((Number) conditionValue).intValue();
                        sender.sendMessage("  §7🐟 击杀远古守卫者: §e" + requiredElderGuardians + " §7只");
                        break;

                    case "wardens-killed":
                        int requiredWardens = ((Number) conditionValue).intValue();
                        sender.sendMessage("  §7👁 击杀监守者: §e" + requiredWardens + " §7只");
                        break;

                    // 怪物击杀数量统计
                    case "zombies-killed":
                        int requiredZombies = ((Number) conditionValue).intValue();
                        sender.sendMessage("  §7🧟 击杀僵尸: §e" + requiredZombies + " §7只");
                        break;

                    case "skeletons-killed":
                        int requiredSkeletons = ((Number) conditionValue).intValue();
                        sender.sendMessage("  §7💀 击杀骷髅: §e" + requiredSkeletons + " §7只");
                        break;

                    case "creepers-killed":
                        int requiredCreepers = ((Number) conditionValue).intValue();
                        sender.sendMessage("  §7💥 击杀苦力怕: §e" + requiredCreepers + " §7只");
                        break;

                    case "spiders-killed":
                        int requiredSpiders = ((Number) conditionValue).intValue();
                        sender.sendMessage("  §7🕷 击杀蜘蛛: §e" + requiredSpiders + " §7只");
                        break;

                    case "endermen-killed":
                        int requiredEndermen = ((Number) conditionValue).intValue();
                        sender.sendMessage("  §7👤 击杀末影人: §e" + requiredEndermen + " §7只");
                        break;

                    // 动物击杀数量统计
                    case "cows-killed":
                        int requiredCows = ((Number) conditionValue).intValue();
                        sender.sendMessage("  §7🐄 击杀牛: §e" + requiredCows + " §7只");
                        break;

                    case "pigs-killed":
                        int requiredPigs = ((Number) conditionValue).intValue();
                        sender.sendMessage("  §7🐷 击杀猪: §e" + requiredPigs + " §7只");
                        break;

                    case "sheep-killed":
                        int requiredSheep = ((Number) conditionValue).intValue();
                        sender.sendMessage("  §7🐑 击杀羊: §e" + requiredSheep + " §7只");
                        break;

                    case "chickens-killed":
                        int requiredChickens = ((Number) conditionValue).intValue();
                        sender.sendMessage("  §7🐔 击杀鸡: §e" + requiredChickens + " §7只");
                        break;

                    // 生活活动数量统计
                    case "villager-trades":
                        int requiredTrades = ((Number) conditionValue).intValue();
                        sender.sendMessage("  §7🏪 村民交易: §e" + requiredTrades + " §7次");
                        break;

                    case "food-eaten":
                        int requiredFoodEaten = ((Number) conditionValue).intValue();
                        sender.sendMessage("  §7🍖 食物消费: §e" + requiredFoodEaten + " §7个");
                        break;

                    case "potions-drunk":
                        int requiredPotionsDrunk = ((Number) conditionValue).intValue();
                        sender.sendMessage("  §7🧪 药水消费: §e" + requiredPotionsDrunk + " §7瓶");
                        break;

                    case "tools-broken":
                        int requiredToolsBroken = ((Number) conditionValue).intValue();
                        sender.sendMessage("  §7🔧 工具损坏: §e" + requiredToolsBroken + " §7个");
                        break;

                    case "special-event":
                        String eventId = (String) conditionValue;
                        String eventName = getEventDisplayName(eventId);
                        sender.sendMessage("  §7✨ 特殊事件: §e" + eventName);
                        break;

                    case "default":
                        boolean isDefault = (Boolean) conditionValue;
                        if (isDefault) {
                            sender.sendMessage("  §b⭐ 默认称号");
                        }
                        break;
                }
            }
        } else {
            sender.sendMessage("§6解锁条件: §7无特殊条件");
        }
        sender.sendMessage("");

        // 显示玩家解锁状态（如果是玩家执行命令）
        if (sender instanceof Player) {
            Player player = (Player) sender;
            boolean hasUnlocked = titleManager.getPlayerData(player).hasUnlockedTitle(titleId);
            boolean isCurrent = titleId.equals(titleManager.getPlayerData(player).getCurrentTitle());

            sender.sendMessage("");
            if (hasUnlocked) {
                if (isCurrent) {
                    sender.sendMessage("§a✓ 状态: §f当前使用中");
                } else {
                    sender.sendMessage("§a✓ 状态: §f已解锁，可切换");
                }
            } else {
                sender.sendMessage("§c✗ 状态: §f未解锁");
            }
        }

        return true;
    }
    
    /**
     * 处理给予称号命令
     */
    private boolean handleGiveCommand(CommandSender sender, String[] args) {
        if (!MessageUtil.checkPermission(sender, "zprefix.give")) {
            return true;
        }
        
        if (args.length < 3) {
            MessageUtil.sendPrefixedMessage(sender, "common.invalid-args", "usage", "/title give <玩家> <称号>");
            return true;
        }
        
        String playerName = args[1];
        String titleId = args[2];
        
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            MessageUtil.sendPrefixedMessage(sender, "common.player-not-found", "player", playerName);
            return true;
        }
        
        if (!configManager.titleExists(titleId)) {
            MessageUtil.sendPrefixedMessage(sender, "title.title-not-found", "title", titleId);
            return true;
        }
        
        if (titleManager.hasTitle(target, titleId)) {
            MessageUtil.sendPrefixedMessage(sender, "title.title-already-unlocked", "player", target.getName());
            return true;
        }
        
        if (titleManager.givePlayerTitle(target, titleId)) {
            TitleInfo titleInfo = configManager.getTitleInfo(titleId);
            MessageUtil.sendPrefixedMessage(sender, "title.title-given", 
                Map.of("player", target.getName(), "title", titleInfo.getDisplayName()));
            
            // 通知目标玩家
            MessageUtil.sendPrefixedMessage(target, "title.title-unlocked", "title", titleInfo.getDisplayName());
        }
        
        return true;
    }
    
    /**
     * 处理移除称号命令
     */
    private boolean handleTakeCommand(CommandSender sender, String[] args) {
        if (!MessageUtil.checkPermission(sender, "zprefix.take")) {
            return true;
        }
        
        if (args.length < 3) {
            MessageUtil.sendPrefixedMessage(sender, "common.invalid-args", "usage", "/title take <玩家> <称号>");
            return true;
        }
        
        String playerName = args[1];
        String titleId = args[2];
        
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            MessageUtil.sendPrefixedMessage(sender, "common.player-not-found", "player", playerName);
            return true;
        }
        
        if (!titleManager.hasTitle(target, titleId)) {
            MessageUtil.sendPrefixedMessage(sender, "title.title-not-owned", "player", target.getName());
            return true;
        }
        
        if (titleManager.takePlayerTitle(target, titleId)) {
            TitleInfo titleInfo = configManager.getTitleInfo(titleId);
            MessageUtil.sendPrefixedMessage(sender, "title.title-taken", 
                Map.of("player", target.getName(), "title", titleInfo.getDisplayName()));
        }
        
        return true;
    }
    
    /**
     * 处理重载命令
     */
    private boolean handleReloadCommand(CommandSender sender) {
        if (!MessageUtil.checkPermission(sender, "zprefix.reload")) {
            return true;
        }
        
        try {
            configManager.reloadConfigs();
            MessageUtil.sendPrefixedMessage(sender, "common.config-reloaded");
        } catch (Exception e) {
            sender.sendMessage("§c重载配置时出错: " + e.getMessage());
            plugin.getLogger().warning("重载配置时出错: " + e.getMessage());
        }
        
        return true;
    }

    /**
     * 处理清理无效称号命令
     */
    private boolean handleCleanupCommand(CommandSender sender, String[] args) {
        if (!MessageUtil.checkPermission(sender, "zprefix.admin")) {
            return true;
        }

        if (args.length < 2) {
            // 清理所有在线玩家的无效称号
            return handleCleanupAllCommand(sender);
        }

        String subCommand = args[1].toLowerCase();
        switch (subCommand) {
            case "all":
                return handleCleanupAllCommand(sender);
            case "player":
                return handleCleanupPlayerCommand(sender, args);
            default:
                MessageUtil.sendPrefixedMessage(sender, "common.invalid-args",
                    "usage", "/title cleanup [all|player <玩家名>]");
                return true;
        }
    }

    /**
     * 清理所有在线玩家的无效称号
     */
    private boolean handleCleanupAllCommand(CommandSender sender) {
        int totalCleaned = 0;
        int playersAffected = 0;

        sender.sendMessage("§e§l[称号系统] §7开始清理所有在线玩家的无效称号数据...");

        for (Player player : Bukkit.getOnlinePlayers()) {
            int cleaned = titleManager.cleanupInvalidTitles(player);
            if (cleaned > 0) {
                totalCleaned += cleaned;
                playersAffected++;
                sender.sendMessage("§7- 玩家 §e" + player.getName() + " §7清理了 §c" + cleaned + " §7个无效称号");
            }
        }

        if (totalCleaned > 0) {
            sender.sendMessage("§a§l[称号系统] §7清理完成！共为 §e" + playersAffected + " §7名玩家清理了 §c" + totalCleaned + " §7个无效称号");
        } else {
            sender.sendMessage("§a§l[称号系统] §7清理完成！未发现无效称号数据");
        }

        return true;
    }

    /**
     * 清理指定玩家的无效称号
     */
    private boolean handleCleanupPlayerCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            MessageUtil.sendPrefixedMessage(sender, "common.invalid-args",
                "usage", "/title cleanup player <玩家名>");
            return true;
        }

        String playerName = args[2];
        Player target = Bukkit.getPlayer(playerName);

        if (target == null) {
            MessageUtil.sendPrefixedMessage(sender, "common.player-not-found", "player", playerName);
            return true;
        }

        int cleaned = titleManager.cleanupInvalidTitles(target);

        if (cleaned > 0) {
            sender.sendMessage("§a§l[称号系统] §7已为玩家 §e" + target.getName() + " §7清理了 §c" + cleaned + " §7个无效称号");
            target.sendMessage("§e§l[称号系统] §7管理员为您清理了 §c" + cleaned + " §7个无效称号");
        } else {
            sender.sendMessage("§a§l[称号系统] §7玩家 §e" + target.getName() + " §7没有无效称号数据");
        }

        return true;
    }

    /**
     * 显示帮助信息
     */
    private void showHelp(CommandSender sender) {
        sender.sendMessage(MessageUtil.getMessage("help.title-command"));
        sender.sendMessage(MessageUtil.getMessage("help.title-gui"));
        sender.sendMessage(MessageUtil.getMessage("help.title-set"));
        sender.sendMessage(MessageUtil.getMessage("help.title-remove"));
        sender.sendMessage(MessageUtil.getMessage("help.title-list"));
        sender.sendMessage(MessageUtil.getMessage("help.title-info"));

        if (sender.hasPermission("zprefix.admin")) {
            sender.sendMessage(MessageUtil.getMessage("help.title-give"));
            sender.sendMessage(MessageUtil.getMessage("help.title-take"));
            sender.sendMessage(MessageUtil.getMessage("help.title-reload"));
            sender.sendMessage("§e/title cleanup [all|player <玩家>] §7- 清理无效称号数据");
        }

        // 显示底部提示
        sender.sendMessage("");
        sender.sendMessage(MessageUtil.getMessage("help.footer"));
    }

    /**
     * 通过显示名查找称号ID
     * 支持精确匹配和模糊匹配（去除颜色代码）
     */
    private String findTitleIdByDisplayName(String displayName) {
        Map<String, TitleInfo> allTitles = configManager.getAllTitles();

        // 去除输入的颜色代码，用于模糊匹配
        String cleanInputName = displayName.replaceAll("§[0-9a-fk-or]", "");

        // 首先尝试精确匹配（包含颜色代码）
        for (Map.Entry<String, TitleInfo> entry : allTitles.entrySet()) {
            String titleDisplayName = entry.getValue().getDisplayName();
            if (titleDisplayName.equals(displayName)) {
                return entry.getKey();
            }
        }

        // 然后尝试模糊匹配（去除颜色代码）
        for (Map.Entry<String, TitleInfo> entry : allTitles.entrySet()) {
            String titleDisplayName = entry.getValue().getDisplayName();
            String cleanTitleName = titleDisplayName.replaceAll("§[0-9a-fk-or]", "");
            if (cleanTitleName.equals(cleanInputName)) {
                return entry.getKey();
            }
        }

        // 最后尝试包含匹配（部分匹配）
        for (Map.Entry<String, TitleInfo> entry : allTitles.entrySet()) {
            String titleDisplayName = entry.getValue().getDisplayName();
            String cleanTitleName = titleDisplayName.replaceAll("§[0-9a-fk-or]", "");
            if (cleanTitleName.toLowerCase().contains(cleanInputName.toLowerCase())) {
                return entry.getKey();
            }
        }

        return null;
    }

    /**
     * 获取事件显示名称
     */
    private String getEventDisplayName(String eventId) {
        switch (eventId) {
            // 稀有物品获得事件
            case "get-diamond": return "获得钻石";
            case "get-emerald": return "获得绿宝石";
            case "get-netherite": return "获得下界合金锭";
            case "get-totem": return "获得不死图腾";
            case "get-elytra": return "获得鞘翅";
            case "get-nether-star": return "获得下界之星";
            case "get-dragon-egg": return "获得龙蛋";

            // 维度探索事件
            case "enter-nether": return "进入下界";
            case "enter-end": return "进入末地";

            default: return eventId;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // 第一个参数：子命令
            List<String> subCommands = Arrays.asList("gui", "set", "remove", "list", "info");
            if (sender.hasPermission("zprefix.admin")) {
                subCommands = new ArrayList<>(subCommands);
                subCommands.addAll(Arrays.asList("give", "take", "reload", "cleanup"));
            }
            
            return subCommands.stream()
                    .filter(cmd -> cmd.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            
            if ("set".equals(subCommand)) {
                // set命令：只显示已解锁的称号显示名
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    Set<String> unlockedTitles = titleManager.getUnlockedTitles(player);
                    return unlockedTitles.stream()
                            .map(titleId -> {
                                TitleInfo titleInfo = configManager.getTitleInfo(titleId);
                                return titleInfo != null ? titleInfo.getDisplayName().replaceAll("§[0-9a-fk-or]", "") : titleId;
                            })
                            .filter(displayName -> displayName.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                }
            } else if ("info".equals(subCommand)) {
                // info命令：显示所有称号的显示名
                Map<String, TitleInfo> allTitles = configManager.getAllTitles();
                return allTitles.values().stream()
                        .map(titleInfo -> titleInfo.getDisplayName().replaceAll("§[0-9a-fk-or]", ""))
                        .filter(displayName -> displayName.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if ("give".equals(subCommand) || "take".equals(subCommand)) {
                // 玩家名补全
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if ("cleanup".equals(subCommand)) {
                // cleanup子命令补全
                return Arrays.asList("all", "player").stream()
                        .filter(cmd -> cmd.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        
        if (args.length == 3) {
            String subCommand = args[0].toLowerCase();

            if ("give".equals(subCommand) || "take".equals(subCommand)) {
                // 称号ID补全
                return configManager.getAllTitles().keySet().stream()
                        .filter(title -> title.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            } else if ("cleanup".equals(subCommand) && "player".equals(args[1].toLowerCase())) {
                // cleanup player 玩家名补全
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        
        return completions;
    }
}
