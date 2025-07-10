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
     *
     * @param player 玩家
     * @param titleInfo 称号信息
     */
    public void applyTitleBuffs(Player player, TitleInfo titleInfo) {
        if (player == null || titleInfo == null) {
            return;
        }

        // 先移除现有的属性修改器
        removeTitleBuffs(player);

        // 应用原生属性加成
        applyNativeAttributes(player, titleInfo);

        // 应用SagaLoreStats属性加成
        applySagaLoreStatsAttributes(player, titleInfo);
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
     *
     * @param player 玩家
     */
    public void removeTitleBuffs(Player player) {
        if (player == null) {
            return;
        }

        UUID playerId = player.getUniqueId();
        Map<Attribute, AttributeModifier> modifiers = playerModifiers.get(playerId);

        // 如果有缓存的修改器，使用缓存数据移除
        if (modifiers != null && !modifiers.isEmpty()) {
            for (Map.Entry<Attribute, AttributeModifier> entry : modifiers.entrySet()) {
                Attribute attribute = entry.getKey();
                AttributeModifier modifier = entry.getValue();

                try {
                    AttributeInstance attributeInstance = player.getAttribute(attribute);
                    if (attributeInstance != null) {
                        attributeInstance.removeModifier(modifier);
                        // 只在调试模式下显示移除信息
                        if (plugin.getConfigManager().getConfigValue("debug", false)) {
                            plugin.getLogger().info("为玩家 " + player.getName() + " 移除属性加成: " + attribute.name());
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

        // 清除修改器引用
        playerModifiers.remove(playerId);

        // 移除SagaLoreStats属性
        if (sagaIntegration != null && sagaIntegration.isEnabled()) {
            sagaIntegration.removeTitleAttributes(player);
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
     * 通过名称移除可能存在的修改器
     *
     * @param player 玩家
     */
    private void removeModifiersByUUID(Player player) {
        // 遍历所有可能的属性类型
        Attribute[] attributes = {
            Attribute.GENERIC_MAX_HEALTH,
            Attribute.GENERIC_FOLLOW_RANGE,
            Attribute.GENERIC_KNOCKBACK_RESISTANCE,
            Attribute.GENERIC_MOVEMENT_SPEED,
            Attribute.GENERIC_FLYING_SPEED,
            Attribute.GENERIC_ATTACK_DAMAGE,
            Attribute.GENERIC_ATTACK_KNOCKBACK,
            Attribute.GENERIC_ATTACK_SPEED,
            Attribute.GENERIC_ARMOR,
            Attribute.GENERIC_ARMOR_TOUGHNESS,
            Attribute.GENERIC_LUCK
        };

        for (Attribute attribute : attributes) {
            try {
                AttributeInstance attributeInstance = player.getAttribute(attribute);
                if (attributeInstance != null) {
                    forceRemoveZPrefixModifiers(attributeInstance);
                }
            } catch (Exception e) {
                // 忽略清理时的错误
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

        // 遍历所有可能的属性类型
        Attribute[] attributes = {
            Attribute.GENERIC_MAX_HEALTH,
            Attribute.GENERIC_FOLLOW_RANGE,
            Attribute.GENERIC_KNOCKBACK_RESISTANCE,
            Attribute.GENERIC_MOVEMENT_SPEED,
            Attribute.GENERIC_FLYING_SPEED,
            Attribute.GENERIC_ATTACK_DAMAGE,
            Attribute.GENERIC_ATTACK_KNOCKBACK,
            Attribute.GENERIC_ATTACK_SPEED,
            Attribute.GENERIC_ARMOR,
            Attribute.GENERIC_ARMOR_TOUGHNESS,
            Attribute.GENERIC_LUCK
        };

        int removedCount = 0;
        for (Attribute attribute : attributes) {
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
     * 
     * @param attribute 属性
     * @return 显示名称
     */
    public String getAttributeDisplayName(Attribute attribute) {
        switch (attribute) {
            case GENERIC_MAX_HEALTH:
                return "生命值";
            case GENERIC_FOLLOW_RANGE:
                return "跟随范围";
            case GENERIC_KNOCKBACK_RESISTANCE:
                return "击退抗性";
            case GENERIC_MOVEMENT_SPEED:
                return "移动速度";
            case GENERIC_FLYING_SPEED:
                return "飞行速度";
            case GENERIC_ATTACK_DAMAGE:
                return "攻击力";
            case GENERIC_ATTACK_KNOCKBACK:
                return "攻击击退";
            case GENERIC_ATTACK_SPEED:
                return "攻击速度";
            case GENERIC_ARMOR:
                return "防御力";
            case GENERIC_ARMOR_TOUGHNESS:
                return "盔甲韧性";
            case GENERIC_LUCK:
                return "幸运值";
            default:
                return attribute.name();
        }
    }
    
    /**
     * 格式化属性值显示
     * 
     * @param attribute 属性
     * @param value 属性值
     * @return 格式化后的显示文本
     */
    public String formatAttributeValue(Attribute attribute, double value) {
        String displayName = getAttributeDisplayName(attribute);
        String sign = value > 0 ? "+" : "";
        
        // 根据属性类型决定显示格式
        switch (attribute) {
            case GENERIC_MOVEMENT_SPEED:
            case GENERIC_FLYING_SPEED:
            case GENERIC_KNOCKBACK_RESISTANCE:
                return String.format("§a%s %s%.2f", displayName, sign, value);
            case GENERIC_ATTACK_SPEED:
                return String.format("§a%s %s%.1f", displayName, sign, value);
            default:
                return String.format("§a%s %s%.0f", displayName, sign, value);
        }
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
