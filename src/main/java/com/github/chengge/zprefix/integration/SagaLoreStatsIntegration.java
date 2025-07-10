package com.github.chengge.zprefix.integration;

import com.saga.sagalorestats.api.SagaLoreStatsAPI;
import com.github.chengge.zprefix.ZPrefix;
import com.github.chengge.zprefix.data.TitleInfo;
import org.bukkit.entity.Player;

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
            // 检查SagaLoreStats插件是否可用
            if (SagaLoreStatsAPI.isAvailable()) {
                plugin.getLogger().info("成功集成 SagaLoreStats 插件！");
            } else {
                plugin.getLogger().info("未检测到 SagaLoreStats 插件，将使用原生属性系统");
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "初始化 SagaLoreStats 集成时出错", e);
        }
    }
    
    /**
     * 检查集成是否可用
     * 
     * @return 是否可用
     */
    public boolean isEnabled() {
        return SagaLoreStatsAPI.isAvailable();
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
            SagaLoreStatsAPI.addTemporaryAttributes(player, sourceName, attributeList);
            
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
     * 
     * @param player 玩家
     */
    public void removeTitleAttributes(Player player) {
        if (!isEnabled() || player == null) {
            return;
        }
        
        try {
            UUID playerId = player.getUniqueId();
            String sourceName = playerTitleSources.get(playerId);
            
            if (sourceName != null) {
                // 移除临时属性
                SagaLoreStatsAPI.removeTemporaryAttributes(player, sourceName);
                
                // 清除记录
                playerTitleSources.remove(playerId);
                
                // 只在调试模式下显示详细信息
                if (plugin.getConfigManager().getConfigValue("debug", false)) {
                    plugin.getLogger().info("移除玩家 " + player.getName() + " 的称号SagaLoreStats属性");
                }
            }
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "移除称号SagaLoreStats属性时出错", e);
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
            return SagaLoreStatsAPI.getPlayerAttributeValue(player, attributeKey);
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
            return SagaLoreStatsAPI.getPlayerAttributes(player);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "获取玩家所有属性时出错", e);
            return Map.of();
        }
    }
    
    /**
     * 清理玩家数据
     * 
     * @param playerId 玩家UUID
     */
    public void cleanupPlayerData(UUID playerId) {
        playerTitleSources.remove(playerId);
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
                    SagaLoreStatsAPI.removeTemporaryAttributes(player, entry.getValue());
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
            SagaLoreStatsAPI.refreshPlayerAttributes(player);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "刷新玩家属性缓存时出错", e);
        }
    }
}
