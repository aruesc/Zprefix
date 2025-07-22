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

        if (!titleManager.hasTitle(player, titleId)) {
            TitleInfo titleInfo = configManager.getTitleInfo(titleId);
            MessageUtil.sendPrefixedMessage(sender, "title.title-not-owned",
                Map.of("player", player.getName(), "title", titleInfo.getDisplayName()));
            return true;
        }

        if (titleManager.setPlayerTitle(player, titleId)) {
            TitleInfo titleInfo = configManager.getTitleInfo(titleId);
            MessageUtil.sendPrefixedMessage(sender, "title.title-set", "title", titleInfo.getDisplayName());
        } else {
            MessageUtil.sendPrefixedMessage(sender, "title.title-set-failed");
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
        
        // 显示称号信息 - 使用配置化消息
        String titleHeader = MessageUtil.getMessage("title.info-header", "title", titleInfo.getDisplayName());
        sender.sendMessage(titleHeader);
        sender.sendMessage("");

        // 显示解锁条件
        Map<String, Object> unlockConditions = titleInfo.getUnlockConditions();
        if (unlockConditions != null && !unlockConditions.isEmpty()) {
            String conditionsHeader = MessageUtil.getMessage("title.info-conditions-header");
            sender.sendMessage(conditionsHeader);
            
            for (Map.Entry<String, Object> condition : unlockConditions.entrySet()) {
                String conditionType = condition.getKey();
                Object conditionValue = condition.getValue();
                
                // 使用统一的格式化方法
                String conditionText = formatUnlockConditionForInfo(conditionType, conditionValue);
                if (conditionText != null && !conditionText.isEmpty()) {
                    sender.sendMessage("  " + conditionText);
                }
            }
        } else {
            String noConditions = MessageUtil.getMessage("title.info-no-conditions");
            sender.sendMessage(noConditions);
        }
        sender.sendMessage("");

        // 显示玩家解锁状态（如果是玩家执行命令）
        if (sender instanceof Player) {
            Player player = (Player) sender;
            boolean hasUnlocked = titleManager.getPlayerData(player).hasUnlockedTitle(titleId);
            boolean isCurrent = titleId.equals(titleManager.getPlayerData(player).getCurrentTitle());

            String statusMessage;
            if (hasUnlocked) {
                if (isCurrent) {
                    statusMessage = MessageUtil.getMessage("title.info-status-current");
                } else {
                    statusMessage = MessageUtil.getMessage("title.info-status-unlocked");
                }
            } else {
                statusMessage = MessageUtil.getMessage("title.info-status-locked");
            }
            sender.sendMessage(statusMessage);
        }

        return true;
    }

    /**
     * 格式化解锁条件文本用于info命令显示
     *
     * @param key 条件键
     * @param value 条件值
     * @return 格式化后的文本
     */
    private String formatUnlockConditionForInfo(String key, Object value) {
        // 特殊处理布尔值条件
        if (value instanceof Boolean) {
            boolean boolValue = (Boolean) value;
            if ("auto-unlock".equals(key)) {
                if (boolValue) {
                    return MessageUtil.getMessage("title.unlock-condition.auto-unlock-true");
                } else {
                    return MessageUtil.getMessage("title.unlock-condition.auto-unlock-false");
                }
            } else if ("admin-only".equals(key)) {
                if (boolValue) {
                    return MessageUtil.getMessage("title.unlock-condition.admin-only");
                }
                return null; // 不显示 admin-only: false
            } else if ("default".equals(key)) {
                if (boolValue) {
                    return MessageUtil.getMessage("title.unlock-condition.default");
                }
                return null; // 不显示 default: false
            }
        }

        // 特殊处理special-event类型
        if ("special-event".equals(key)) {
            String eventId = String.valueOf(value);
            String eventMessageKey = "title.unlock-condition." + eventId;
            String eventTemplate = MessageUtil.getMessage(eventMessageKey);

            // 如果找到了特殊事件的配置
            if (eventTemplate != null && !eventTemplate.startsWith("§c消息配置错误:")) {
                return eventTemplate;
            }

            // 如果没有找到特殊事件配置，使用通用格式
            String generalTemplate = MessageUtil.getMessage("title.unlock-condition.special-event");
            if (generalTemplate != null && !generalTemplate.startsWith("§c消息配置错误:")) {
                return generalTemplate.replace("{value}", eventId);
            }
        }

        // 使用统一的格式化方法
        String messageKey = "title.unlock-condition." + key;
        String template = MessageUtil.getMessage(messageKey);

        // 检查是否找到了有效的配置（不是错误消息）
        if (template != null && !template.startsWith("§c消息配置错误:")) {
            // 找到了配置的模板，替换占位符
            return template.replace("{value}", String.valueOf(value));
        }

        // 如果没有找到配置，使用默认格式
        return "§7" + key + ": §e" + value;
    }

    /**
     * 处理给予称号命令
     */
    private boolean handleGiveCommand(CommandSender sender, String[] args) {
        if (!MessageUtil.checkPermission(sender, "zprefix.give")) {
            return true;
        }

        if (args.length < 3) {
            MessageUtil.sendPrefixedMessage(sender, "common.invalid-args", "usage", "/title give <玩家> <称号显示名>");
            return true;
        }

        String playerName = args[1];

        // 将参数拼接为完整的称号显示名（支持带空格的称号名）
        StringBuilder titleNameBuilder = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            if (i > 2) {
                titleNameBuilder.append(" ");
            }
            titleNameBuilder.append(args[i]);
        }
        String titleDisplayName = titleNameBuilder.toString();

        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            MessageUtil.sendPrefixedMessage(sender, "common.player-not-found", "player", playerName);
            return true;
        }

        // 通过显示名查找称号ID
        String titleId = findTitleIdByDisplayName(titleDisplayName);
        if (titleId == null) {
            MessageUtil.sendPrefixedMessage(sender, "title.title-not-found", "title", titleDisplayName);
            return true;
        }

        if (titleManager.hasTitle(target, titleId)) {
            TitleInfo titleInfo = configManager.getTitleInfo(titleId);
            MessageUtil.sendPrefixedMessage(sender, "title.title-already-unlocked",
                Map.of("player", target.getName(), "title", titleInfo.getDisplayName()));
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
            MessageUtil.sendPrefixedMessage(sender, "common.invalid-args", "usage", "/title take <玩家> <称号显示名>");
            return true;
        }

        String playerName = args[1];

        // 将参数拼接为完整的称号显示名（支持带空格的称号名）
        StringBuilder titleNameBuilder = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            if (i > 2) {
                titleNameBuilder.append(" ");
            }
            titleNameBuilder.append(args[i]);
        }
        String titleDisplayName = titleNameBuilder.toString();

        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            MessageUtil.sendPrefixedMessage(sender, "common.player-not-found", "player", playerName);
            return true;
        }

        // 通过显示名查找称号ID
        String titleId = findTitleIdByDisplayName(titleDisplayName);
        if (titleId == null) {
            MessageUtil.sendPrefixedMessage(sender, "title.title-not-found", "title", titleDisplayName);
            return true;
        }

        if (!titleManager.hasTitle(target, titleId)) {
            TitleInfo titleInfo = configManager.getTitleInfo(titleId);
            MessageUtil.sendPrefixedMessage(sender, "title.title-not-owned",
                Map.of("player", target.getName(), "title", titleInfo.getDisplayName()));
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
     * 处理清理所有玩家命令
     */
    private boolean handleCleanupAllCommand(CommandSender sender) {
        int cleanedCount = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            cleanedCount += titleManager.cleanupInvalidTitles(player);
        }

        MessageUtil.sendPrefixedMessage(sender, "title.cleanup-success", "count", String.valueOf(cleanedCount));
        return true;
    }

    /**
     * 处理清理指定玩家命令
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

        int cleanedCount = titleManager.cleanupInvalidTitles(target);
        MessageUtil.sendPrefixedMessage(sender, "title.cleanup-success", "count", String.valueOf(cleanedCount));
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
                // info命令：显示所有称号显示名
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
                // cleanup子命令
                return Arrays.asList("all", "player").stream()
                        .filter(cmd -> cmd.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        if (args.length == 3) {
            String subCommand = args[0].toLowerCase();

            if ("give".equals(subCommand) || "take".equals(subCommand)) {
                // 称号显示名补全
                Map<String, TitleInfo> allTitles = configManager.getAllTitles();
                return allTitles.values().stream()
                        .map(titleInfo -> titleInfo.getDisplayName().replaceAll("§[0-9a-fk-or]", ""))
                        .filter(displayName -> displayName.toLowerCase().startsWith(args[2].toLowerCase()))
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
