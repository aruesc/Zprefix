package com.github.chengge.zprefix.integration;

import com.github.chengge.zprefix.ZPrefix;
import com.github.chengge.zprefix.data.TitleInfo;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * SagaLoreStats 集成类
 * 负责与SagaLoreStats插件的属性系统集成
 */
public class SagaLoreStatsIntegration {
    
    private final ZPrefix plugin;
    private final Map<UUID, String> playerTitleSources = new ConcurrentHashMap<>();
    
    // 属性源名称前缀
    private static final String TITLE_SOURCE_PREFIX = "zPrefix_Title_";
    
    public SagaLoreStatsIntegration(ZPrefix plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 初始化集成
     */
    public void initialize() {
        try {
            // 检查SagaLoreStats插件是否已安装并启用
            if (Bukkit.getPluginManager().getPlugin("SagaLoreStats") != null &&
                Bukkit.getPluginManager().isPluginEnabled("SagaLoreStats")) {

                // 尝试加载SagaLoreStats API类
                try {
                    Class<?> apiClass = Class.forName("com.saga.sagalorestats.api.SagaLoreStatsAPI");
                    plugin.getLogger().info("成功集成 SagaLoreStats 插件！");

                    // 在调试模式下显示可用的API方法
                    if (plugin.getConfigManager().getConfigValue("debug", false)) {
                        logAvailableMethods(apiClass);
                    }
                } catch (ClassNotFoundException e) {
                    plugin.getLogger().warning("检测到 SagaLoreStats 插件，但API版本不兼容，将使用原生属性系统");
                }
            } else {
                plugin.getLogger().info("未检测到 SagaLoreStats 插件，将使用原生属性系统");
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "初始化 SagaLoreStats 集成时出错，将使用原生属性系统", e);
        }
    }
    
    /**
     * 检查集成是否可用
     *
     * @return 是否可用
     */
    public boolean isEnabled() {
        try {
            // 检查插件是否存在并启用
            if (Bukkit.getPluginManager().getPlugin("SagaLoreStats") == null ||
                !Bukkit.getPluginManager().isPluginEnabled("SagaLoreStats")) {
                return false;
            }

            // 检查API类是否存在
            Class.forName("com.saga.sagalorestats.api.SagaLoreStatsAPI");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "检查 SagaLoreStats 集成状态时出错", e);
            return false;
        }
    }
    
    /**
     * 应用称号的SagaLoreStats属性
     * 
     * @param player 玩家
     * @param titleInfo 称号信息
     */
    public void applyTitleAttributes(Player player, TitleInfo titleInfo) {
        if (!isEnabled() || player == null || titleInfo == null) {
            return;
        }
        
        try {
            // 先移除现有的称号属性
            removeTitleAttributes(player);
            
            // 获取称号的SagaLoreStats属性配置
            Map<String, Object> sagaAttributes = titleInfo.getSagaLoreStatsAttributes();
            if (sagaAttributes == null || sagaAttributes.isEmpty()) {
                return;
            }
            
            // 转换为SagaLoreStats格式的属性列表
            List<String> attributeList = convertToSagaFormat(sagaAttributes);
            if (attributeList.isEmpty()) {
                return;
            }
            
            // 生成属性源名称
            String sourceName = TITLE_SOURCE_PREFIX + titleInfo.getId();
            
            // 应用临时属性
            callSagaLoreStatsMethod("addTemporaryAttributes", player, sourceName, attributeList);
            
            // 记录玩家的属性源
            playerTitleSources.put(player.getUniqueId(), sourceName);
            
            // 只在调试模式下显示详细信息
            if (plugin.getConfigManager().getConfigValue("debug", false)) {
                plugin.getLogger().info("为玩家 " + player.getName() + " 应用称号 " + titleInfo.getDisplayName() + " 的SagaLoreStats属性");
            }
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "应用称号SagaLoreStats属性时出错", e);
        }
    }
    
    /**
     * 移除玩家的称号属性
     * 增强错误处理，确保即使SagaLoreStats有问题也能正常清理
     *
     * @param player 玩家
     */
    public void removeTitleAttributes(Player player) {
        if (player == null) {
            return;
        }

        UUID playerId = player.getUniqueId();
        String sourceName = playerTitleSources.get(playerId);

        try {
            // 检查SagaLoreStats是否可用
            if (!isEnabled()) {
                // SagaLoreStats不可用，只清理本地记录
                playerTitleSources.remove(playerId);
                if (plugin.getConfigManager().getConfigValue("debug", false)) {
                    plugin.getLogger().info("SagaLoreStats不可用，只清理玩家 " + player.getName() + " 的本地记录");
                }
                return;
            }

            if (sourceName != null) {
                // 尝试移除临时属性
                try {
                    callSagaLoreStatsMethod("removeTemporaryAttributes", player, sourceName);

                    // 只在调试模式下显示详细信息
                    if (plugin.getConfigManager().getConfigValue("debug", false)) {
                        plugin.getLogger().info("成功移除玩家 " + player.getName() + " 的称号SagaLoreStats属性");
                    }
                } catch (Exception methodException) {
                    // SagaLoreStats方法调用失败，可能插件有问题
                    plugin.getLogger().warning("SagaLoreStats方法调用失败，玩家 " + player.getName() + " 的属性可能无法完全清理: " + methodException.getMessage());
                }

                // 无论是否成功，都清除本地记录
                playerTitleSources.remove(playerId);
            }

        } catch (Exception e) {
            // 发生任何错误都要清理本地记录，避免数据残留
            playerTitleSources.remove(playerId);
            plugin.getLogger().log(Level.WARNING, "移除称号SagaLoreStats属性时出错，已清理本地记录", e);
        }
    }
    
    /**
     * 刷新玩家的称号属性
     * 
     * @param player 玩家
     * @param titleInfo 新的称号信息
     */
    public void refreshTitleAttributes(Player player, TitleInfo titleInfo) {
        removeTitleAttributes(player);
        if (titleInfo != null) {
            applyTitleAttributes(player, titleInfo);
        }
    }
    
    /**
     * 将称号属性配置转换为SagaLoreStats格式
     * 
     * @param sagaAttributes 称号的SagaLoreStats属性配置
     * @return SagaLoreStats格式的属性列表
     */
    private List<String> convertToSagaFormat(Map<String, Object> sagaAttributes) {
        List<String> attributeList = new ArrayList<>();
        
        for (Map.Entry<String, Object> entry : sagaAttributes.entrySet()) {
            String attributeName = entry.getKey();
            Object valueObj = entry.getValue();
            
            if (valueObj instanceof Number) {
                double value = ((Number) valueObj).doubleValue();
                if (value != 0) {
                    String sign = value > 0 ? "+" : "";
                    String attributeString = formatAttributeString(attributeName, value, sign);
                    attributeList.add(attributeString);
                }
            }
        }
        
        return attributeList;
    }
    
    /**
     * 格式化属性字符串
     * 
     * @param attributeName 属性名称
     * @param value 属性值
     * @param sign 符号
     * @return 格式化后的属性字符串
     */
    private String formatAttributeString(String attributeName, double value, String sign) {
        // 根据属性类型决定显示格式
        if (isPercentageAttribute(attributeName)) {
            // 百分比属性
            return String.format("%s: %s%.1f%%", attributeName, sign, value);
        } else if (isDecimalAttribute(attributeName)) {
            // 小数属性
            return String.format("%s: %s%.2f", attributeName, sign, value);
        } else {
            // 整数属性
            return String.format("%s: %s%.0f", attributeName, sign, value);
        }
    }
    
    /**
     * 检查是否为百分比属性
     * 
     * @param attributeName 属性名称
     * @return 是否为百分比属性
     */
    private boolean isPercentageAttribute(String attributeName) {
        String lowerName = attributeName.toLowerCase();
        return lowerName.contains("率") || lowerName.contains("概率") || 
               lowerName.contains("几率") || lowerName.contains("chance") ||
               lowerName.contains("rate") || lowerName.contains("percent");
    }
    
    /**
     * 检查是否为小数属性
     * 
     * @param attributeName 属性名称
     * @return 是否为小数属性
     */
    private boolean isDecimalAttribute(String attributeName) {
        String lowerName = attributeName.toLowerCase();
        return lowerName.contains("速度") || lowerName.contains("倍率") ||
               lowerName.contains("系数") || lowerName.contains("speed") ||
               lowerName.contains("multiplier") || lowerName.contains("factor");
    }
    
    /**
     * 获取玩家的称号属性值
     * 
     * @param player 玩家
     * @param attributeKey 属性键
     * @return 属性值
     */
    public double getPlayerTitleAttributeValue(Player player, String attributeKey) {
        if (!isEnabled() || player == null || attributeKey == null) {
            return 0.0;
        }
        
        try {
            Object result = callSagaLoreStatsMethod("getPlayerAttributeValue", player, attributeKey);
            return result instanceof Number ? ((Number) result).doubleValue() : 0.0;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "获取玩家称号属性值时出错", e);
            return 0.0;
        }
    }
    
    /**
     * 获取玩家的所有属性
     * 
     * @param player 玩家
     * @return 属性映射
     */
    public Map<String, Double> getPlayerAllAttributes(Player player) {
        if (!isEnabled() || player == null) {
            return Map.of();
        }
        
        try {
            Object result = callSagaLoreStatsMethod("getPlayerAttributes", player);
            return result instanceof Map ? (Map<String, Double>) result : Map.of();
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "获取玩家所有属性时出错", e);
            return Map.of();
        }
    }
    
    /**
     * 清理玩家数据
     * 强制清理所有相关数据，即使SagaLoreStats有问题
     *
     * @param playerId 玩家UUID
     */
    public void cleanupPlayerData(UUID playerId) {
        String sourceName = playerTitleSources.get(playerId);

        // 如果有记录的属性源，尝试清理
        if (sourceName != null && isEnabled()) {
            try {
                // 尝试通过玩家对象清理
                Player player = plugin.getServer().getPlayer(playerId);
                if (player != null) {
                    callSagaLoreStatsMethod("removeTemporaryAttributes", player, sourceName);
                }
            } catch (Exception e) {
                // 清理失败，记录警告但继续清理本地数据
                plugin.getLogger().warning("清理玩家 " + playerId + " 的SagaLoreStats数据时出错: " + e.getMessage());
            }
        }

        // 无论如何都要清理本地记录
        playerTitleSources.remove(playerId);
    }

    /**
     * 强制清理所有残留的SagaLoreStats属性
     * 用于插件重启或SagaLoreStats出现问题时
     *
     * @param player 玩家
     */
    public void forceCleanupAllAttributes(Player player) {
        if (player == null) {
            return;
        }

        UUID playerId = player.getUniqueId();

        try {
            // 清理所有可能的称号属性源
            for (int i = 0; i < 10; i++) { // 最多清理10个可能的源
                String sourceName = TITLE_SOURCE_PREFIX + "title_" + i;
                try {
                    if (isEnabled()) {
                        callSagaLoreStatsMethod("removeTemporaryAttributes", player, sourceName);
                    }
                } catch (Exception e) {
                    // 忽略单个源的清理错误
                }
            }

            // 清理当前记录的源
            String currentSource = playerTitleSources.get(playerId);
            if (currentSource != null && isEnabled()) {
                try {
                    callSagaLoreStatsMethod("removeTemporaryAttributes", player, currentSource);
                } catch (Exception e) {
                    // 忽略清理错误
                }
            }

        } catch (Exception e) {
            plugin.getLogger().warning("强制清理玩家 " + player.getName() + " 的SagaLoreStats属性时出错: " + e.getMessage());
        } finally {
            // 无论如何都要清理本地记录
            playerTitleSources.remove(playerId);
        }
    }
    
    /**
     * 清理所有数据
     */
    public void clearAllData() {
        // 清理所有玩家的称号属性源
        for (Map.Entry<UUID, String> entry : playerTitleSources.entrySet()) {
            try {
                Player player = plugin.getServer().getPlayer(entry.getKey());
                if (player != null && player.isOnline()) {
                    callSagaLoreStatsMethod("removeTemporaryAttributes", player, entry.getValue());
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "清理玩家称号属性时出错", e);
            }
        }
        
        playerTitleSources.clear();
    }
    
    /**
     * 强制刷新玩家属性缓存
     * 
     * @param player 玩家
     */
    public void refreshPlayerCache(Player player) {
        if (!isEnabled() || player == null) {
            return;
        }
        
        try {
            callSagaLoreStatsMethod("refreshPlayerAttributes", player);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "刷新玩家属性缓存时出错", e);
        }
    }

    /**
     * 使用反射安全调用SagaLoreStats API方法
     *
     * @param methodName 方法名
     * @param args 参数
     * @return 调用结果
     */
    private Object callSagaLoreStatsMethod(String methodName, Object... args) {
        try {
            Class<?> apiClass = Class.forName("com.saga.sagalorestats.api.SagaLoreStatsAPI");

            // 尝试查找匹配的方法
            java.lang.reflect.Method targetMethod = null;
            java.lang.reflect.Method[] methods = apiClass.getMethods();

            for (java.lang.reflect.Method method : methods) {
                if (method.getName().equals(methodName) && method.getParameterCount() == args.length) {
                    Class<?>[] paramTypes = method.getParameterTypes();
                    boolean matches = true;

                    for (int i = 0; i < args.length; i++) {
                        if (args[i] == null) {
                            continue; // null可以匹配任何引用类型
                        }

                        Class<?> argType = args[i].getClass();
                        Class<?> paramType = paramTypes[i];

                        // 检查类型兼容性
                        if (!isCompatible(argType, paramType)) {
                            matches = false;
                            break;
                        }
                    }

                    if (matches) {
                        targetMethod = method;
                        break;
                    }
                }
            }

            if (targetMethod == null) {
                plugin.getLogger().warning("未找到匹配的SagaLoreStats API方法: " + methodName +
                    " (参数数量: " + args.length + ")");
                return null;
            }

            return targetMethod.invoke(null, args);

        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "调用SagaLoreStats API方法 " + methodName + " 时出错", e);
            return null;
        }
    }

    /**
     * 检查参数类型是否兼容
     *
     * @param argType 实际参数类型
     * @param paramType 方法参数类型
     * @return 是否兼容
     */
    private boolean isCompatible(Class<?> argType, Class<?> paramType) {
        // 完全匹配
        if (paramType.isAssignableFrom(argType)) {
            return true;
        }

        // 基本类型和包装类型的匹配
        if (paramType.isPrimitive()) {
            if (paramType == int.class && argType == Integer.class) return true;
            if (paramType == double.class && argType == Double.class) return true;
            if (paramType == float.class && argType == Float.class) return true;
            if (paramType == long.class && argType == Long.class) return true;
            if (paramType == boolean.class && argType == Boolean.class) return true;
        }

        // 特殊情况：List接口
        if (paramType == List.class || paramType == java.util.Collection.class) {
            return List.class.isAssignableFrom(argType);
        }

        return false;
    }

    /**
     * 记录SagaLoreStats API中可用的方法（调试用）
     *
     * @param apiClass API类
     */
    private void logAvailableMethods(Class<?> apiClass) {
        plugin.getLogger().info("SagaLoreStats API 可用方法:");
        java.lang.reflect.Method[] methods = apiClass.getMethods();
        for (java.lang.reflect.Method method : methods) {
            if (method.getDeclaringClass() == apiClass) { // 只显示API类自己的方法
                StringBuilder sb = new StringBuilder();
                sb.append("  ").append(method.getName()).append("(");
                Class<?>[] paramTypes = method.getParameterTypes();
                for (int i = 0; i < paramTypes.length; i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(paramTypes[i].getSimpleName());
                }
                sb.append(")");
                plugin.getLogger().info(sb.toString());
            }
        }
    }
}
