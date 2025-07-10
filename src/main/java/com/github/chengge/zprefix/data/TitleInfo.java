package com.github.chengge.zprefix.data;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

/**
 * 称号信息类
 * 存储称号的所有相关信息
 */
public class TitleInfo {
    
    private final String id;
    private final String displayName;
    private final ItemStack guiItem;
    private final Map<Attribute, Double> attributes;
    private final Map<String, Object> sagaLoreStatsAttributes;
    private final Map<String, Object> unlockConditions;
    private final boolean isDefault;
    
    /**
     * 构造函数
     *
     * @param id 称号ID
     * @param displayName 显示名称
     * @param guiItem GUI中显示的物品
     * @param attributes 原生属性加成
     * @param sagaLoreStatsAttributes SagaLoreStats属性加成
     * @param unlockConditions 解锁条件
     * @param isDefault 是否为默认称号
     */
    public TitleInfo(String id, String displayName, ItemStack guiItem,
                    Map<Attribute, Double> attributes,
                    Map<String, Object> sagaLoreStatsAttributes,
                    Map<String, Object> unlockConditions, boolean isDefault) {
        this.id = id;
        this.displayName = displayName;
        this.guiItem = guiItem;
        this.attributes = attributes;
        this.sagaLoreStatsAttributes = sagaLoreStatsAttributes;
        this.unlockConditions = unlockConditions;
        this.isDefault = isDefault;
    }
    
    /**
     * 获取称号ID
     * 
     * @return 称号ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * 获取显示名称
     * 
     * @return 显示名称
     */
    public String getDisplayName() {
        return displayName;
    }
    

    
    /**
     * 获取GUI物品
     * 
     * @return GUI中显示的物品
     */
    public ItemStack getGuiItem() {
        return guiItem.clone();
    }
    
    /**
     * 获取原生属性加成
     *
     * @return 原生属性加成映射
     */
    public Map<Attribute, Double> getAttributes() {
        return attributes;
    }

    /**
     * 获取SagaLoreStats属性加成
     *
     * @return SagaLoreStats属性加成映射
     */
    public Map<String, Object> getSagaLoreStatsAttributes() {
        return sagaLoreStatsAttributes;
    }
    
    /**
     * 获取解锁条件
     * 
     * @return 解锁条件映射
     */
    public Map<String, Object> getUnlockConditions() {
        return unlockConditions;
    }
    
    /**
     * 是否为默认称号
     * 
     * @return 是否为默认称号
     */
    public boolean isDefault() {
        return isDefault;
    }
    
    /**
     * 检查是否自动解锁
     * 
     * @return 是否自动解锁
     */
    public boolean isAutoUnlock() {
        return unlockConditions.containsKey("auto-unlock") && 
               (Boolean) unlockConditions.get("auto-unlock");
    }
    
    /**
     * 检查是否仅管理员可给予
     * 
     * @return 是否仅管理员可给予
     */
    public boolean isAdminOnly() {
        return unlockConditions.containsKey("admin-only") && 
               (Boolean) unlockConditions.get("admin-only");
    }
    
    /**
     * 创建用于GUI显示的物品（包含状态信息）
     * 
     * @param isUnlocked 是否已解锁
     * @param isCurrent 是否为当前使用的称号
     * @return 带状态的GUI物品
     */
    public ItemStack createGuiItem(boolean isUnlocked, boolean isCurrent) {
        ItemStack item = getGuiItem();
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            List<String> lore = meta.getLore();
            if (lore != null) {
                if (isCurrent) {
                    lore.add("");
                    lore.add("§a§l当前使用中");
                } else if (isUnlocked) {
                    lore.add("");
                    lore.add("§e点击切换称号");
                } else {
                    lore.add("");
                    lore.add("§c§l未解锁");
                    lore.add("§c需要解锁才能使用");
                }
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    @Override
    public String toString() {
        return "TitleInfo{" +
                "id='" + id + '\'' +
                ", displayName='" + displayName + '\'' +
                ", isDefault=" + isDefault +
                '}';
    }
}
