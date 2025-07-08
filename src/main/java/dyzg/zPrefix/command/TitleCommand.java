package dyzg.zPrefix.command;

import dyzg.zPrefix.ZPrefix;
import dyzg.zPrefix.data.PlayerTitleData;
import dyzg.zPrefix.data.TitleInfo;
import dyzg.zPrefix.gui.TitleGUI;
import dyzg.zPrefix.manager.BuffManager;
import dyzg.zPrefix.manager.ConfigManager;
import dyzg.zPrefix.manager.TitleManager;
import dyzg.zPrefix.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

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
            MessageUtil.sendPrefixedMessage(sender, "common.invalid-args", "usage", "/title set <称号>");
            return true;
        }
        
        Player player = (Player) sender;
        String titleId = args[1];
        
        // 检查称号是否存在
        if (!configManager.titleExists(titleId)) {
            MessageUtil.sendPrefixedMessage(sender, "title.title-not-found", "title", titleId);
            return true;
        }
        
        // 检查是否已解锁
        if (!titleManager.hasTitle(player, titleId)) {
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
            MessageUtil.sendPrefixedMessage(sender, "common.invalid-args", "usage", "/title info <称号>");
            return true;
        }
        
        String titleId = args[1];
        TitleInfo titleInfo = configManager.getTitleInfo(titleId);
        
        if (titleInfo == null) {
            MessageUtil.sendPrefixedMessage(sender, "title.title-not-found", "title", titleId);
            return true;
        }
        
        // 显示称号信息
        MessageUtil.sendPrefixedMessage(sender, "title.title-info", "title", titleInfo.getDisplayName());
        sender.sendMessage("§e称号ID: §f" + titleId);
        sender.sendMessage("§e显示名: " + titleInfo.getDisplayName());
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

                    case "kill-mobs":
                        int requiredKills = ((Number) conditionValue).intValue();
                        sender.sendMessage("  §7⚔ 击杀怪物: §e" + requiredKills + " §7只");
                        break;

                    case "damage-taken":
                        double requiredDamage = ((Number) conditionValue).doubleValue();
                        sender.sendMessage("  §7💔 受到伤害: §e" + String.format("%.1f", requiredDamage) + " §7点");
                        break;

                    case "walk-distance":
                        double requiredDistance = ((Number) conditionValue).doubleValue();
                        sender.sendMessage("  §7🚶 移动距离: §e" + String.format("%.0f", requiredDistance) + " §7米");
                        break;

                    case "play-time":
                        double requiredTime = ((Number) conditionValue).doubleValue();
                        sender.sendMessage("  §7⏰ 游戏时间: §e" + String.format("%.1f", requiredTime) + " §7小时");
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

        // 显示属性加成
        List<String> buffDesc = buffManager.getTitleBuffDescription(titleInfo);
        if (!buffDesc.isEmpty()) {
            sender.sendMessage("§6属性加成:");
            for (String buff : buffDesc) {
                sender.sendMessage("  " + buff);
            }
        } else {
            sender.sendMessage("§6属性加成: §7无");
        }

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
     * 获取事件显示名称
     */
    private String getEventDisplayName(String eventId) {
        switch (eventId) {
            case "find-diamond": return "找到钻石矿";
            case "find-emerald": return "找到绿宝石矿";
            case "find-ancient-debris": return "找到远古残骸";
            case "find-gold": return "找到金矿";
            case "find-iron": return "找到铁矿";
            case "get-diamond": return "获得钻石";
            case "get-emerald": return "获得绿宝石";
            case "get-netherite": return "获得下界合金锭";
            case "get-totem": return "获得不死图腾";
            case "get-elytra": return "获得鞘翅";
            case "get-nether-star": return "获得下界之星";
            case "get-dragon-egg": return "获得龙蛋";
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
            
            if ("set".equals(subCommand) || "info".equals(subCommand)) {
                // 称号ID补全
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    Set<String> unlockedTitles = titleManager.getUnlockedTitles(player);
                    return unlockedTitles.stream()
                            .filter(title -> title.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                }
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
