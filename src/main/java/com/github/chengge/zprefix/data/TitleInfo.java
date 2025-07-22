package com.github.chengge.zprefix.data;

import com.github.chengge.zprefix.util.MessageUtil;
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
    private final int sortOrder;
    private final boolean isHidden;
    private final Map<String, Object> purchaseOptions;
    
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
     * @param sortOrder 排序顺序
     * @param isHidden 是否隐藏
     * @param purchaseOptions 购买选项
     */
    public TitleInfo(String id, String displayName, ItemStack guiItem,
                    Map<Attribute, Double> attributes,
                    Map<String, Object> sagaLoreStatsAttributes,
                    Map<String, Object> unlockConditions, boolean isDefault,
                    int sortOrder, boolean isHidden, Map<String, Object> purchaseOptions) {
        this.id = id;
        this.displayName = displayName;
        this.guiItem = guiItem;
        this.attributes = attributes;
        this.sagaLoreStatsAttributes = sagaLoreStatsAttributes;
        this.unlockConditions = unlockConditions;
        this.isDefault = isDefault;
        this.sortOrder = sortOrder;
        this.isHidden = isHidden;
        this.purchaseOptions = purchaseOptions != null ? purchaseOptions : new java.util.HashMap<>();
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
     * 获取排序顺序
     *
     * @return 排序顺序
     */
    public int getSortOrder() {
        return sortOrder;
    }

    /**
     * 是否为隐藏称号
     *
     * @return 是否隐藏
     */
    public boolean isHidden() {
        return isHidden;
    }

    /**
     * 获取购买选项
     *
     * @return 购买选项映射
     */
    public Map<String, Object> getPurchaseOptions() {
        return purchaseOptions;
    }

    /**
     * 是否可购买
     *
     * @return 是否可购买
     */
    public boolean isPurchasable() {
        return purchaseOptions != null && !purchaseOptions.isEmpty() &&
               (purchaseOptions.containsKey("money") || purchaseOptions.containsKey("points"));
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

    /**
     * 创建未解锁称号的GUI物品（显示解锁条件或购买选项）
     *
     * @param showUnlockInfo 是否显示解锁信息
     * @return 未解锁称号的GUI物品
     */
    public ItemStack createUnlockedGuiItem(boolean showUnlockInfo) {
        ItemStack item = new ItemStack(guiItem.getType());
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(displayName);

            List<String> lore = new java.util.ArrayList<>();

            if (showUnlockInfo) {
                // 显示解锁条件或购买信息
                if (isPurchasable()) {
                    lore.add("§6§l可购买解锁");
                    lore.add("");

                    if (purchaseOptions.containsKey("money")) {
                        double money = ((Number) purchaseOptions.get("money")).doubleValue();
                        lore.add("§e金币价格: §f" + String.format("%.2f", money));
                    }

                    if (purchaseOptions.containsKey("points")) {
                        int points = ((Number) purchaseOptions.get("points")).intValue();
                        lore.add("§b点券价格: §f" + points);
                    }

                    lore.add("");
                    lore.add("§a点击购买解锁");
                } else {
                    lore.add("§c§l需要满足条件解锁");
                    lore.add("");
                    lore.add("§7解锁条件:");

                    // 显示解锁条件
                    addUnlockConditionsToLore(lore);
                }
            } else {
                lore.add("§c§l未解锁");
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * 添加解锁条件到lore中
     *
     * @param lore lore列表
     */
    private void addUnlockConditionsToLore(List<String> lore) {
        for (Map.Entry<String, Object> entry : unlockConditions.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if ("auto-unlock".equals(key)) {
                continue; // 跳过自动解锁标记
            }

            String conditionText = formatUnlockCondition(key, value);
            if (conditionText != null) {
                lore.add("§7  " + conditionText);
            }
        }
    }

    /**
     * 格式化解锁条件文本
     *
     * @param key 条件键
     * @param value 条件值
     * @return 格式化后的文本
     */
    private String formatUnlockCondition(String key, Object value) {
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

        // 尝试从消息配置中获取格式化文本
        String messageKey = "title.unlock-condition." + key;
        String template = MessageUtil.getMessage(messageKey);

        // 检查是否找到了有效的配置（不是错误消息）
        if (template != null && !template.startsWith("§c消息配置错误:")) {
            // 找到了配置的模板，替换占位符
            return template.replace("{value}", String.valueOf(value));
        }

        // 如果没有找到配置，使用默认格式
        return key + ": " + value;
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
