package com.github.chengge.zprefix.util;

import com.github.chengge.zprefix.ZPrefix;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 消息工具类
 * 统一处理插件的所有消息输出
 */
public class MessageUtil {
    
    private static ZPrefix plugin;
    private static FileConfiguration messageConfig;
    private static final Map<String, String> messageCache = new HashMap<>();
    
    /**
     * 初始化消息工具
     * 
     * @param pluginInstance 插件实例
     */
    public static void initialize(ZPrefix pluginInstance) {
        plugin = pluginInstance;
        loadMessages();
    }
    
    /**
     * 加载消息配置
     */
    public static void loadMessages() {
        File messageFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messageFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        
        messageConfig = YamlConfiguration.loadConfiguration(messageFile);
        messageCache.clear();
        
        // 预加载常用消息到缓存
        cacheMessage("common.prefix");
        cacheMessage("common.no-permission");
        cacheMessage("common.player-only");
        cacheMessage("common.player-not-found");
        cacheMessage("common.invalid-args");
    }
    
    /**
     * 缓存消息
     * 
     * @param path 消息路径
     */
    private static void cacheMessage(String path) {
        String message = messageConfig.getString(path, "§c消息配置错误: " + path);
        messageCache.put(path, ChatColor.translateAlternateColorCodes('&', message));
    }
    
    /**
     * 获取消息
     * 
     * @param path 消息路径
     * @return 格式化后的消息
     */
    public static String getMessage(String path) {
        // 先从缓存获取
        if (messageCache.containsKey(path)) {
            return messageCache.get(path);
        }
        
        // 从配置文件获取
        String message = messageConfig.getString(path, "§c消息配置错误: " + path);
        String formattedMessage = ChatColor.translateAlternateColorCodes('&', message);
        
        // 添加到缓存
        messageCache.put(path, formattedMessage);
        
        return formattedMessage;
    }
    
    /**
     * 获取带占位符替换的消息
     *
     * @param path 消息路径
     * @param placeholders 占位符映射
     * @return 格式化后的消息
     */
    public static String getMessage(String path, Map<String, String> placeholders) {
        String message = getMessage(path);

        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                String placeholder = entry.getKey();
                String value = entry.getValue() != null ? entry.getValue() : "";
                message = message.replace("{" + placeholder + "}", value);
            }
        }

        return message;
    }
    
    /**
     * 获取带单个占位符替换的消息
     * 
     * @param path 消息路径
     * @param placeholder 占位符名称
     * @param value 替换值
     * @return 格式化后的消息
     */
    public static String getMessage(String path, String placeholder, String value) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(placeholder, value);
        return getMessage(path, placeholders);
    }
    
    /**
     * 发送消息给命令发送者
     * 
     * @param sender 命令发送者
     * @param path 消息路径
     */
    public static void sendMessage(CommandSender sender, String path) {
        sender.sendMessage(getMessage(path));
    }
    
    /**
     * 发送带占位符的消息给命令发送者
     * 
     * @param sender 命令发送者
     * @param path 消息路径
     * @param placeholders 占位符映射
     */
    public static void sendMessage(CommandSender sender, String path, Map<String, String> placeholders) {
        sender.sendMessage(getMessage(path, placeholders));
    }
    
    /**
     * 发送带单个占位符的消息给命令发送者
     * 
     * @param sender 命令发送者
     * @param path 消息路径
     * @param placeholder 占位符名称
     * @param value 替换值
     */
    public static void sendMessage(CommandSender sender, String path, String placeholder, String value) {
        sender.sendMessage(getMessage(path, placeholder, value));
    }
    
    /**
     * 发送带前缀的消息
     * 
     * @param sender 命令发送者
     * @param path 消息路径
     */
    public static void sendPrefixedMessage(CommandSender sender, String path) {
        String prefix = getMessage("common.prefix");
        String message = getMessage(path);
        sender.sendMessage(prefix + message);
    }
    
    /**
     * 发送带前缀和占位符的消息
     * 
     * @param sender 命令发送者
     * @param path 消息路径
     * @param placeholders 占位符映射
     */
    public static void sendPrefixedMessage(CommandSender sender, String path, Map<String, String> placeholders) {
        String prefix = getMessage("common.prefix");
        String message = getMessage(path, placeholders);
        sender.sendMessage(prefix + message);
    }
    
    /**
     * 发送带前缀和单个占位符的消息
     * 
     * @param sender 命令发送者
     * @param path 消息路径
     * @param placeholder 占位符名称
     * @param value 替换值
     */
    public static void sendPrefixedMessage(CommandSender sender, String path, String placeholder, String value) {
        String prefix = getMessage("common.prefix");
        String message = getMessage(path, placeholder, value);
        sender.sendMessage(prefix + message);
    }
    
    /**
     * 检查是否为玩家并发送错误消息
     * 
     * @param sender 命令发送者
     * @return 是否为玩家
     */
    public static boolean checkPlayer(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sendPrefixedMessage(sender, "common.player-only");
            return false;
        }
        return true;
    }
    
    /**
     * 检查权限并发送错误消息
     * 
     * @param sender 命令发送者
     * @param permission 权限节点
     * @return 是否有权限
     */
    public static boolean checkPermission(CommandSender sender, String permission) {
        if (!sender.hasPermission(permission)) {
            sendPrefixedMessage(sender, "common.no-permission");
            return false;
        }
        return true;
    }
    
    /**
     * 格式化颜色代码
     * 
     * @param text 原始文本
     * @return 格式化后的文本
     */
    public static String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
