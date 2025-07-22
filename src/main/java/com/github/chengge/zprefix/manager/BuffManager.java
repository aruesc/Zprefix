package com.github.chengge.zprefix.manager;

import com.github.chengge.zprefix.ZPrefix;
import com.github.chengge.zprefix.data.TitleInfo;
import com.github.chengge.zprefix.integration.SagaLoreStatsIntegration;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.logging.Level;

/**
 * Buff管理器
 * 负责管理玩家的属性加成效果
 */
public class BuffManager {
    
    private final ZPrefix plugin;
    private final Map<UUID, Map<Attribute, AttributeModifier>> playerModifiers = new HashMap<>();
    private SagaLoreStatsIntegration sagaIntegration;

    // 属性修改器的命名空间
    private static final String MODIFIER_NAME = "zPrefix_title_buff";
    private static final UUID MODIFIER_UUID = UUID.fromString("12345678-1234-1234-1234-123456789abc");

    public BuffManager(ZPrefix plugin) {
        this.plugin = plugin;
    }

    /**
     * 设置SagaLoreStats集成
     *
     * @param sagaIntegration SagaLoreStats集成实例
     */
    public void setSagaIntegration(SagaLoreStatsIntegration sagaIntegration) {
        this.sagaIntegration = sagaIntegration;
    }
    
    /**
     * 应用称号的属性加成
     * 确保正确的应用顺序：先原版属性，后其他插件属性
     *
     * @param player 玩家
     * @param titleInfo 称号信息
     */
    public void applyTitleBuffs(Player player, TitleInfo titleInfo) {
        if (player == null || titleInfo == null) {
            return;
        }

        // 先完全清理所有现有的属性修改器
        removeTitleBuffs(player);

        // 确保玩家属性处于干净状态
        ensureCleanAttributeState(player);

        // 按正确顺序应用属性：
        // 1. 首先应用原版属性加成（基础属性）
        applyNativeAttributes(player, titleInfo);

        // 2. 然后应用SagaLoreStats属性加成（扩展属性）
        applySagaLoreStatsAttributes(player, titleInfo);

        // 只在调试模式下显示应用信息
        if (plugin.getConfigManager().getConfigValue("debug", false)) {
            plugin.getLogger().info("为玩家 " + player.getName() + " 应用称号 " + titleInfo.getDisplayName() + " 的属性加成");
        }
    }

    /**
     * 应用原生属性加成
     *
     * @param player 玩家
     * @param titleInfo 称号信息
     */
    private void applyNativeAttributes(Player player, TitleInfo titleInfo) {
        Map<Attribute, Double> attributes = titleInfo.getAttributes();
        if (attributes.isEmpty()) {
            return;
        }

        UUID playerId = player.getUniqueId();
        
        Map<Attribute, AttributeModifier> modifiers = new HashMap<>();
        
        for (Map.Entry<Attribute, Double> entry : attributes.entrySet()) {
            Attribute attribute = entry.getKey();
            double value = entry.getValue();
            
            if (value == 0) {
                continue;
            }
            
            try {
                AttributeInstance attributeInstance = player.getAttribute(attribute);
                if (attributeInstance != null) {
                    // 强制清理所有可能的zPrefix修改器
                    forceRemoveZPrefixModifiers(attributeInstance);

                    // 创建新的UUID以避免冲突
                    UUID uniqueUUID = UUID.randomUUID();

                    // 创建属性修改器（使用临时修改器，不持久化）
                    AttributeModifier modifier = new AttributeModifier(
                        uniqueUUID,
                        MODIFIER_NAME + "_" + player.getName(), // 添加玩家名称以便识别
                        value,
                        AttributeModifier.Operation.ADD_NUMBER
                    );

                    // 应用修改器
                    attributeInstance.addModifier(modifier);
                    modifiers.put(attribute, modifier);

                    // 特殊处理最大生命值：调整当前生命值
                    handleHealthAttributeChange(player, attribute, value);

                    // 只在调试模式下显示详细信息
                    if (plugin.getConfigManager().getConfigValue("debug", false)) {
                        plugin.getLogger().info("为玩家 " + player.getName() + " 应用属性加成: " +
                                              attribute.name() + " +" + value);
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING,
                    "为玩家 " + player.getName() + " 应用属性 " + attribute.name() + " 时出错", e);
            }
        }
        
        // 保存修改器引用
        if (!modifiers.isEmpty()) {
            playerModifiers.put(playerId, modifiers);
        }
    }

    /**
     * 处理生命值属性变化
     * 当最大生命值改变时，智能调整当前生命值
     * 保持血量比例，而不是简单的数值增减
     *
     * @param player 玩家
     * @param attribute 属性类型
     * @param value 属性值变化
     */
    private void handleHealthAttributeChange(Player player, Attribute attribute, double value) {
        try {
            // 检查是否是最大生命值属性
            String attributeName = com.github.chengge.zprefix.util.AttributeAdapter.getAttributeName(attribute);
            if (attributeName != null &&
                (attributeName.contains("MAX_HEALTH") || attributeName.contains("HEALTH"))) {

                // 获取当前生命值和新的最大生命值
                double currentHealth = player.getHealth();
                double newMaxHealth = player.getAttribute(attribute).getValue();

                // 计算旧的最大生命值
                double oldMaxHealth = newMaxHealth - value;

                if (value > 0) {
                    // 增加了最大生命值的情况
                    if (oldMaxHealth > 0) {
                        // 计算原来的血量比例
                        double healthRatio = currentHealth / oldMaxHealth;

                        // 如果原来是满血状态（比例 >= 0.99），则保持满血
                        if (healthRatio >= 0.99) {
                            player.setHealth(newMaxHealth);

                            if (plugin.getConfigManager().getConfigValue("debug", false)) {
                                plugin.getLogger().info("为玩家 " + player.getName() + " 保持满血状态: " +
                                                      currentHealth + " -> " + newMaxHealth + " (原本满血)");
                            }
                        } else {
                            // 如果原来不是满血，按比例增加生命值
                            double newHealth = Math.min(currentHealth + value, newMaxHealth);
                            player.setHealth(newHealth);

                            if (plugin.getConfigManager().getConfigValue("debug", false)) {
                                plugin.getLogger().info("为玩家 " + player.getName() + " 按比例增加生命值: " +
                                                      currentHealth + " -> " + newHealth + " (比例: " + String.format("%.1f%%", healthRatio * 100) + ")");
                            }
                        }
                    }
                } else if (value < 0) {
                    // 减少了最大生命值的情况
                    // 确保当前生命值不超过新的最大值
                    if (currentHealth > newMaxHealth) {
                        player.setHealth(newMaxHealth);

                        if (plugin.getConfigManager().getConfigValue("debug", false)) {
                            plugin.getLogger().info("为玩家 " + player.getName() + " 限制生命值: " +
                                                  currentHealth + " -> " + newMaxHealth + " (超过新最大值)");
                        }
                    }
                    // 如果没有超过新最大值，保持原有生命值不变
                }
            }
        } catch (Exception e) {
            // 生命值调整失败不应该影响属性应用
            plugin.getLogger().warning("调整玩家 " + player.getName() + " 生命值时出错: " + e.getMessage());
        }
    }

    /**
     * 确保玩家属性处于干净状态
     * 移除所有可能的残留属性修改器
     *
     * @param player 玩家
     */
    private void ensureCleanAttributeState(Player player) {
        if (player == null) {
            return;
        }

        // 获取所有可用属性
        Set<Attribute> availableAttributes = com.github.chengge.zprefix.util.AttributeAdapter.getAllAvailableAttributes();

        int cleanedCount = 0;
        for (Attribute attribute : availableAttributes) {
            try {
                AttributeInstance attributeInstance = player.getAttribute(attribute);
                if (attributeInstance != null) {
                    // 移除所有zPrefix相关的修改器
                    List<AttributeModifier> toRemove = new ArrayList<>();
                    for (AttributeModifier modifier : attributeInstance.getModifiers()) {
                        if (modifier.getName() != null &&
                            (modifier.getName().contains("zPrefix") ||
                             modifier.getName().contains("SagaLoreStats") ||
                             modifier.getName().contains("title"))) {
                            toRemove.add(modifier);
                        }
                    }

                    for (AttributeModifier modifier : toRemove) {
                        try {
                            attributeInstance.removeModifier(modifier);
                            cleanedCount++;
                        } catch (Exception e) {
                            // 忽略移除错误
                        }
                    }
                }
            } catch (Exception e) {
                // 忽略属性访问错误
            }
        }

        // 只在有清理操作或调试模式时显示
        if (cleanedCount > 0 || plugin.getConfigManager().getConfigValue("debug", false)) {
            plugin.getLogger().info("为玩家 " + player.getName() + " 清理了 " + cleanedCount + " 个残留属性修改器");
        }
    }

    /**
     * 应用SagaLoreStats属性加成
     *
     * @param player 玩家
     * @param titleInfo 称号信息
     */
    private void applySagaLoreStatsAttributes(Player player, TitleInfo titleInfo) {
        if (sagaIntegration != null && sagaIntegration.isEnabled()) {
            sagaIntegration.applyTitleAttributes(player, titleInfo);
        }
    }
    
    /**
     * 移除玩家的称号属性加成
     * 确保完全清理所有相关属性修改器
     *
     * @param player 玩家
     */
    public void removeTitleBuffs(Player player) {
        if (player == null) {
            return;
        }

        UUID playerId = player.getUniqueId();
        Map<Attribute, AttributeModifier> modifiers = playerModifiers.get(playerId);

        // 1. 首先移除SagaLoreStats属性（优先处理，避免残留）
        removeSagaLoreStatsAttributes(player);

        // 2. 然后移除原版属性修改器
        if (modifiers != null && !modifiers.isEmpty()) {
            for (Map.Entry<Attribute, AttributeModifier> entry : modifiers.entrySet()) {
                Attribute attribute = entry.getKey();
                AttributeModifier modifier = entry.getValue();

                try {
                    AttributeInstance attributeInstance = player.getAttribute(attribute);
                    if (attributeInstance != null) {
                        // 在移除修改器前记录属性值变化
                        double oldValue = attributeInstance.getValue();

                        attributeInstance.removeModifier(modifier);

                        // 移除后检查生命值是否需要调整
                        double newValue = attributeInstance.getValue();
                        double valueChange = newValue - oldValue;
                        if (valueChange != 0) {
                            handleHealthAttributeChange(player, attribute, valueChange);
                        }

                        // 只在调试模式下显示移除信息
                        if (plugin.getConfigManager().getConfigValue("debug", false)) {
                            plugin.getLogger().info("为玩家 " + player.getName() + " 移除原版属性: " + attribute.name());
                        }
                    }
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING,
                        "为玩家 " + player.getName() + " 移除属性 " + attribute.name() + " 时出错", e);
                }
            }
        } else {
            // 如果没有缓存数据，尝试通过UUID移除可能存在的修改器
            removeModifiersByUUID(player);
        }

        // 3. 强制清理所有可能的残留修改器
        forceCleanupAllTitleModifiers(player);

        // 4. 清除修改器引用
        playerModifiers.remove(playerId);
    }

    /**
     * 安全地移除SagaLoreStats属性
     *
     * @param player 玩家
     */
    private void removeSagaLoreStatsAttributes(Player player) {
        try {
            if (sagaIntegration != null) {
                // 无论SagaLoreStats是否启用，都尝试清理
                sagaIntegration.removeTitleAttributes(player);
            }
        } catch (Exception e) {
            // SagaLoreStats插件可能有问题，记录警告但不影响原版属性清理
            plugin.getLogger().warning("清理SagaLoreStats属性时出错（插件可能未启动）: " + e.getMessage());
        }
    }

    /**
     * 强制清理所有称号相关的修改器
     *
     * @param player 玩家
     */
    private void forceCleanupAllTitleModifiers(Player player) {
        Set<Attribute> availableAttributes = com.github.chengge.zprefix.util.AttributeAdapter.getAllAvailableAttributes();

        for (Attribute attribute : availableAttributes) {
            try {
                AttributeInstance attributeInstance = player.getAttribute(attribute);
                if (attributeInstance != null) {
                    // 收集所有称号相关的修改器
                    List<AttributeModifier> toRemove = new ArrayList<>();
                    for (AttributeModifier modifier : attributeInstance.getModifiers()) {
                        if (modifier.getName() != null &&
                            (modifier.getName().contains("zPrefix") ||
                             modifier.getName().contains("title") ||
                             modifier.getName().contains("SagaLoreStats"))) {
                            toRemove.add(modifier);
                        }
                    }

                    // 移除收集到的修改器
                    for (AttributeModifier modifier : toRemove) {
                        try {
                            attributeInstance.removeModifier(modifier);
                        } catch (Exception e) {
                            // 忽略移除错误
                        }
                    }
                }
            } catch (Exception e) {
                // 忽略属性访问错误
            }
        }
    }

    /**
     * 强制移除所有zPrefix相关的修改器
     *
     * @param attributeInstance 属性实例
     */
    private void forceRemoveZPrefixModifiers(AttributeInstance attributeInstance) {
        // 先收集需要移除的修改器，避免在遍历时修改集合
        List<AttributeModifier> toRemove = new ArrayList<>();
        for (AttributeModifier modifier : attributeInstance.getModifiers()) {
            // 检查修改器名称是否包含zPrefix标识
            if (modifier.getName() != null && modifier.getName().contains("zPrefix")) {
                toRemove.add(modifier);
            }
        }

        // 然后移除收集到的修改器
        for (AttributeModifier modifier : toRemove) {
            try {
                attributeInstance.removeModifier(modifier);
            } catch (Exception e) {
                // 忽略移除时的错误
            }
        }
    }

    /**
     * 移除已存在的同名修改器
     *
     * @param attributeInstance 属性实例
     * @param modifierName 修改器名称
     */
    private void removeExistingModifiers(AttributeInstance attributeInstance, String modifierName) {
        // 先收集需要移除的修改器，避免在遍历时修改集合
        List<AttributeModifier> toRemove = new ArrayList<>();
        for (AttributeModifier modifier : attributeInstance.getModifiers()) {
            if (modifierName.equals(modifier.getName())) {
                toRemove.add(modifier);
            }
        }

        // 然后移除收集到的修改器
        for (AttributeModifier modifier : toRemove) {
            try {
                attributeInstance.removeModifier(modifier);
            } catch (Exception e) {
                // 忽略移除时的错误
            }
        }
    }

    /**
     * 通过UUID移除可能存在的修改器
     * 使用AttributeAdapter动态获取所有可用属性，避免硬编码
     *
     * @param player 玩家
     */
    private void removeModifiersByUUID(Player player) {
        // 使用AttributeAdapter动态获取所有可用属性
        Set<Attribute> availableAttributes = com.github.chengge.zprefix.util.AttributeAdapter.getAllAvailableAttributes();

        for (Attribute attribute : availableAttributes) {
            try {
                AttributeInstance attributeInstance = player.getAttribute(attribute);
                if (attributeInstance != null) {
                    forceRemoveZPrefixModifiers(attributeInstance);
                }
            } catch (Exception e) {
                // 忽略清理时的错误，某些属性可能不适用于玩家
            }
        }
    }
    
    /**
     * 刷新玩家的称号属性加成
     * 
     * @param player 玩家
     * @param titleInfo 新的称号信息
     */
    public void refreshTitleBuffs(Player player, TitleInfo titleInfo) {
        removeTitleBuffs(player);
        if (titleInfo != null) {
            applyTitleBuffs(player, titleInfo);
        }
    }
    
    /**
     * 检查玩家是否有称号属性加成
     * 
     * @param player 玩家
     * @return 是否有属性加成
     */
    public boolean hasBuffs(Player player) {
        if (player == null) {
            return false;
        }
        
        UUID playerId = player.getUniqueId();
        Map<Attribute, AttributeModifier> modifiers = playerModifiers.get(playerId);
        return modifiers != null && !modifiers.isEmpty();
    }
    
    /**
     * 获取玩家当前的属性修改器
     * 
     * @param player 玩家
     * @return 属性修改器映射
     */
    public Map<Attribute, AttributeModifier> getPlayerModifiers(Player player) {
        if (player == null) {
            return new HashMap<>();
        }
        
        UUID playerId = player.getUniqueId();
        Map<Attribute, AttributeModifier> modifiers = playerModifiers.get(playerId);
        return modifiers != null ? new HashMap<>(modifiers) : new HashMap<>();
    }
    
    /**
     * 清理玩家数据（玩家离线时调用）
     *
     * @param playerId 玩家UUID
     */
    public void cleanupPlayerData(UUID playerId) {
        playerModifiers.remove(playerId);

        // 清理SagaLoreStats数据
        if (sagaIntegration != null) {
            sagaIntegration.cleanupPlayerData(playerId);
        }
    }

    /**
     * 强制清理玩家的所有属性修改器（用于玩家加入时）
     *
     * @param player 玩家
     */
    public void forceCleanupPlayerAttributes(Player player) {
        if (player == null) {
            return;
        }

        // 只在调试模式下显示清理信息
        if (plugin.getConfigManager().getConfigValue("debug", false)) {
            plugin.getLogger().info("正在为玩家 " + player.getName() + " 强制清理所有zPrefix属性修改器...");
        }

        // 使用AttributeAdapter动态获取所有可用属性，避免硬编码
        Set<Attribute> availableAttributes = com.github.chengge.zprefix.util.AttributeAdapter.getAllAvailableAttributes();

        int removedCount = 0;
        for (Attribute attribute : availableAttributes) {
            try {
                AttributeInstance attributeInstance = player.getAttribute(attribute);
                if (attributeInstance != null) {
                    // 收集所有zPrefix相关的修改器
                    List<AttributeModifier> toRemove = new ArrayList<>();
                    for (AttributeModifier modifier : attributeInstance.getModifiers()) {
                        if (modifier.getName() != null && modifier.getName().contains("zPrefix")) {
                            toRemove.add(modifier);
                        }
                    }

                    // 移除收集到的修改器
                    for (AttributeModifier modifier : toRemove) {
                        try {
                            attributeInstance.removeModifier(modifier);
                            removedCount++;
                            // 只在调试模式下显示详细移除信息
                            if (plugin.getConfigManager().getConfigValue("debug", false)) {
                                plugin.getLogger().info("移除遗留修改器: " + attribute.name() + " - " + modifier.getName());
                            }
                        } catch (Exception e) {
                            // 忽略移除错误
                        }
                    }
                }
            } catch (Exception e) {
                // 忽略属性访问错误
            }
        }

        // 清除该玩家的缓存数据
        playerModifiers.remove(player.getUniqueId());

        // 清理SagaLoreStats数据
        if (sagaIntegration != null) {
            sagaIntegration.cleanupPlayerData(player.getUniqueId());
        }

        // 只在有清理操作或调试模式时显示
        if (removedCount > 0 || plugin.getConfigManager().getConfigValue("debug", false)) {
            plugin.getLogger().info("为玩家 " + player.getName() + " 清理了 " + removedCount + " 个遗留属性修改器");
        }
    }

    /**
     * 清理所有数据
     */
    public void clearAllData() {
        playerModifiers.clear();

        // 清理SagaLoreStats数据
        if (sagaIntegration != null) {
            sagaIntegration.clearAllData();
        }
    }
    
    /**
     * 获取属性的显示名称
     * 使用AttributeAdapter进行跨版本兼容
     *
     * @param attribute 属性
     * @return 显示名称
     */
    public String getAttributeDisplayName(Attribute attribute) {
        // 使用AttributeAdapter获取显示名称，支持跨版本兼容
        return com.github.chengge.zprefix.util.AttributeAdapter.getDisplayName(attribute);
    }
    
    /**
     * 格式化属性值显示
     * 使用AttributeAdapter进行跨版本兼容的格式化
     *
     * @param attribute 属性
     * @param value 属性值
     * @return 格式化后的显示文本
     */
    public String formatAttributeValue(Attribute attribute, double value) {
        String displayName = getAttributeDisplayName(attribute);
        String sign = value > 0 ? "+" : "";

        // 使用属性名称进行智能格式化，避免硬编码枚举值
        String attributeName = com.github.chengge.zprefix.util.AttributeAdapter.getAttributeName(attribute);
        if (attributeName != null) {
            attributeName = attributeName.toUpperCase();

            // 根据属性名称特征决定显示格式
            if (attributeName.contains("SPEED") ||
                attributeName.contains("RESISTANCE") ||
                attributeName.contains("KNOCKBACK")) {
                // 速度、抗性类属性显示2位小数
                return String.format("§a%s %s%.2f", displayName, sign, value);
            } else if (attributeName.contains("ATTACK_SPEED")) {
                // 攻击速度显示1位小数
                return String.format("§a%s %s%.1f", displayName, sign, value);
            }
        }

        // 默认显示整数
        return String.format("§a%s %s%.0f", displayName, sign, value);
    }
    
    /**
     * 获取称号的属性加成描述
     * 
     * @param titleInfo 称号信息
     * @return 属性加成描述列表
     */
    public List<String> getTitleBuffDescription(TitleInfo titleInfo) {
        List<String> description = new ArrayList<>();
        
        if (titleInfo == null || titleInfo.getAttributes().isEmpty()) {
            return description;
        }
        
        for (Map.Entry<Attribute, Double> entry : titleInfo.getAttributes().entrySet()) {
            Attribute attribute = entry.getKey();
            double value = entry.getValue();
            
            if (value != 0) {
                description.add(formatAttributeValue(attribute, value));
            }
        }
        
        return description;
    }
}
