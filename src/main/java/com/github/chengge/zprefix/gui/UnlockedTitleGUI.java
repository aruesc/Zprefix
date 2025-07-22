package com.github.chengge.zprefix.gui;

import com.github.chengge.zprefix.ZPrefix;
import com.github.chengge.zprefix.data.PlayerTitleData;
import com.github.chengge.zprefix.data.TitleInfo;
import com.github.chengge.zprefix.integration.EconomyIntegration;
import com.github.chengge.zprefix.manager.ConfigManager;
import com.github.chengge.zprefix.manager.TitleManager;
import com.github.chengge.zprefix.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 未解锁称号GUI
 * 显示所有未解锁的称号，包括解锁条件和购买选项
 */
public class UnlockedTitleGUI {
    
    private static final int GUI_SIZE = 54;
    private static final int TITLES_PER_PAGE = 45;
    private static final int BUTTON_ROW_START = 45;
    
    // 按钮位置（与主界面保持一致）
    private static final int PREV_PAGE_SLOT = 45;
    private static final int BACK_TO_MAIN_SLOT = 46;
    private static final int CLOSE_SLOT = 47;
    private static final int PAGE_INFO_SLOT = 48;
    private static final int REFRESH_SLOT = 51;
    private static final int NEXT_PAGE_SLOT = 53;
    
    private final ZPrefix plugin;
    private final ConfigManager configManager;
    private final TitleManager titleManager;
    private final EconomyIntegration economyIntegration;
    
    // 玩家页面状态
    private final Map<UUID, Integer> playerPages = new ConcurrentHashMap<>();
    
    public UnlockedTitleGUI(ZPrefix plugin, ConfigManager configManager, 
                           TitleManager titleManager, EconomyIntegration economyIntegration) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.titleManager = titleManager;
        this.economyIntegration = economyIntegration;
    }
    
    /**
     * 打开未解锁称号GUI
     */
    public void openGUI(Player player) {
        openGUI(player, 0);
    }
    
    /**
     * 打开指定页面的未解锁称号GUI
     */
    public void openGUI(Player player, int page) {
        PlayerTitleData playerData = titleManager.getPlayerData(player);
        
        // 获取所有未解锁的可见称号
        List<TitleInfo> unlockedTitles = getUnlockedVisibleTitles(playerData);
        
        // 计算总页数
        int totalPages = Math.max(1, (int) Math.ceil((double) unlockedTitles.size() / TITLES_PER_PAGE));
        
        // 确保页面在有效范围内
        page = Math.max(0, Math.min(page, totalPages - 1));
        playerPages.put(player.getUniqueId(), page);
        
        // 创建GUI
        String title = MessageUtil.colorize("§c§l未解锁称号");
        Inventory gui = Bukkit.createInventory(null, GUI_SIZE, title + " §7(" + (page + 1) + "/" + totalPages + ")");
        
        // 填充称号
        fillUnlockedTitles(gui, player, unlockedTitles, page);
        
        // 填充按钮行
        fillButtonRow(gui, player, page, totalPages, unlockedTitles.size());
        
        player.openInventory(gui);
    }
    
    /**
     * 获取未解锁的可见称号列表
     */
    private List<TitleInfo> getUnlockedVisibleTitles(PlayerTitleData playerData) {
        List<TitleInfo> unlockedTitles = new ArrayList<>();
        
        // 获取排序后的可见称号
        List<TitleInfo> sortedTitles = configManager.getSortedTitles(false);
        
        for (TitleInfo titleInfo : sortedTitles) {
            // 只显示未解锁的称号
            if (!playerData.hasUnlockedTitle(titleInfo.getId())) {
                unlockedTitles.add(titleInfo);
            }
        }
        
        return unlockedTitles;
    }
    
    /**
     * 填充未解锁称号到GUI中
     */
    private void fillUnlockedTitles(Inventory gui, Player player, List<TitleInfo> unlockedTitles, int page) {
        int startIndex = page * TITLES_PER_PAGE;
        int endIndex = Math.min(startIndex + TITLES_PER_PAGE, unlockedTitles.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            TitleInfo titleInfo = unlockedTitles.get(i);
            ItemStack titleItem = titleInfo.createUnlockedGuiItem(true);
            gui.setItem(i - startIndex, titleItem);
        }
    }
    
    /**
     * 填充按钮行
     */
    private void fillButtonRow(Inventory gui, Player player, int currentPage, int totalPages, int totalTitles) {
        // 填充装饰物品
        fillDecorationItems(gui);

        // 分页按钮
        if (totalPages > 1) {
            if (currentPage > 0) {
                gui.setItem(PREV_PAGE_SLOT, createConfigurableButton("previous-page", currentPage, totalPages, totalTitles, 0));
            }
            if (currentPage < totalPages - 1) {
                gui.setItem(NEXT_PAGE_SLOT, createConfigurableButton("next-page", currentPage, totalPages, totalTitles, 0));
            }
        }

        // 其他按钮
        gui.setItem(BACK_TO_MAIN_SLOT, createConfigurableButton("back-to-main", currentPage, totalPages, totalTitles, 0));
        gui.setItem(CLOSE_SLOT, createConfigurableButton("close", currentPage, totalPages, totalTitles, 0));
        gui.setItem(PAGE_INFO_SLOT, createUnlockedPageInfoButton(currentPage, totalPages, totalTitles));
        gui.setItem(REFRESH_SLOT, createConfigurableButton("refresh", currentPage, totalPages, totalTitles, 0));
    }

    /**
     * 填充装饰物品
     */
    private void fillDecorationItems(Inventory gui) {
        String materialName = configManager.getConfigValue("gui.fill-item.material", "GRAY_STAINED_GLASS_PANE");
        String itemName = configManager.getConfigValue("gui.fill-item.name", " ");

        try {
            Material material = Material.valueOf(materialName.toUpperCase());
            ItemStack fillItem = new ItemStack(material);
            ItemMeta meta = fillItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(MessageUtil.colorize(itemName));
                fillItem.setItemMeta(meta);
            }

            // 填充最后一行的空白位置
            for (int i = BUTTON_ROW_START; i < GUI_SIZE; i++) {
                if (gui.getItem(i) == null) {
                    gui.setItem(i, fillItem);
                }
            }
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("无效的填充物品材质: " + materialName);
        }
    }
    
    /**
     * 创建可配置的按钮
     */
    private ItemStack createConfigurableButton(String buttonType, int currentPage, int totalPages, int totalTitles, int unlockedTitles) {
        String configPath = "gui.buttons." + buttonType;

        String materialName = configManager.getConfigValue(configPath + ".material", "STONE");
        String name = configManager.getConfigValue(configPath + ".name", "§7按钮");
        List<String> lore = configManager.getConfigValue(configPath + ".lore", List.of("§7点击使用"));

        try {
            Material material = Material.valueOf(materialName.toUpperCase());
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                // 替换占位符
                meta.setDisplayName(MessageUtil.colorize(replacePlaceholders(name, currentPage, totalPages, totalTitles, unlockedTitles)));

                List<String> processedLore = new ArrayList<>();
                for (String line : lore) {
                    processedLore.add(MessageUtil.colorize(replacePlaceholders(line, currentPage, totalPages, totalTitles, unlockedTitles)));
                }
                meta.setLore(processedLore);

                item.setItemMeta(meta);
            }

            return item;
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("无效的按钮材质: " + materialName + " (按钮类型: " + buttonType + ")");
            return new ItemStack(Material.STONE);
        }
    }

    /**
     * 创建未解锁页面信息按钮（特殊处理）
     */
    private ItemStack createUnlockedPageInfoButton(int currentPage, int totalPages, int totalTitles) {
        String configPath = "gui.unlocked-gui.buttons.page-info";

        String materialName = configManager.getConfigValue(configPath + ".material", "BOOK");
        String name = configManager.getConfigValue(configPath + ".name", "§e页面信息");
        List<String> lore = configManager.getConfigValue(configPath + ".lore",
            List.of("§7当前页: §e{current_page} §7/ §e{total_pages}", "§7未解锁称号总数: §e{total_titles}"));

        try {
            Material material = Material.valueOf(materialName.toUpperCase());
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                meta.setDisplayName(MessageUtil.colorize(replacePlaceholders(name, currentPage, totalPages, totalTitles, 0)));

                List<String> processedLore = new ArrayList<>();
                for (String line : lore) {
                    String processedLine = replacePlaceholders(line, currentPage, totalPages, totalTitles, 0);

                    // 处理经济系统状态占位符
                    if (processedLine.contains("{vault_status}")) {
                        processedLine = processedLine.replace("{vault_status}",
                            economyIntegration.isVaultEnabled() ? "已启用" : "未启用");
                    }
                    if (processedLine.contains("{points_status}")) {
                        processedLine = processedLine.replace("{points_status}",
                            economyIntegration.isPlayerPointsEnabled() ? "已启用" : "未启用");
                    }

                    processedLore.add(MessageUtil.colorize(processedLine));
                }
                meta.setLore(processedLore);

                item.setItemMeta(meta);
            }

            return item;
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("无效的页面信息按钮材质: " + materialName);
            return new ItemStack(Material.BOOK);
        }
    }

    /**
     * 替换占位符
     */
    private String replacePlaceholders(String text, int currentPage, int totalPages, int totalTitles, int unlockedTitles) {
        return text.replace("{current_page}", String.valueOf(currentPage + 1))
                  .replace("{total_pages}", String.valueOf(totalPages))
                  .replace("{total_titles}", String.valueOf(totalTitles))
                  .replace("{unlocked_titles}", String.valueOf(unlockedTitles));
    }
    
    /**
     * 处理GUI点击事件
     */
    public boolean handleGUIClick(Player player, int slot, ItemStack clickedItem) {
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return false;
        }
        
        // 检查是否点击了按钮行
        if (slot >= BUTTON_ROW_START) {
            return handleButtonClick(player, slot, clickedItem);
        }
        
        // 检查是否点击了称号
        if (slot < TITLES_PER_PAGE) {
            return handleTitleClick(player, slot, clickedItem);
        }
        
        return false;
    }
    
    /**
     * 处理按钮点击
     */
    private boolean handleButtonClick(Player player, int slot, ItemStack clickedItem) {
        switch (slot) {
            case PREV_PAGE_SLOT:
                int currentPage = playerPages.getOrDefault(player.getUniqueId(), 0);
                if (currentPage > 0) {
                    openGUI(player, currentPage - 1);
                }
                return true;

            case NEXT_PAGE_SLOT:
                currentPage = playerPages.getOrDefault(player.getUniqueId(), 0);
                openGUI(player, currentPage + 1);
                return true;

            case CLOSE_SLOT:
                player.closeInventory();
                return true;

            case BACK_TO_MAIN_SLOT:
                // 返回主称号界面
                plugin.getTitleGUI().openGUI(player);
                return true;

            case PAGE_INFO_SLOT:
                // 页面信息按钮，不做任何操作
                return true;

            case REFRESH_SLOT:
                // 刷新界面
                currentPage = playerPages.getOrDefault(player.getUniqueId(), 0);
                openGUI(player, currentPage);
                MessageUtil.sendPrefixedMessage(player, "title.refresh-success");
                return true;

            default:
                return false;
        }
    }
    
    /**
     * 处理称号点击
     */
    private boolean handleTitleClick(Player player, int slot, ItemStack clickedItem) {
        // 获取当前页面的未解锁称号列表
        PlayerTitleData playerData = titleManager.getPlayerData(player);
        List<TitleInfo> unlockedTitles = getUnlockedVisibleTitles(playerData);
        
        int currentPage = playerPages.getOrDefault(player.getUniqueId(), 0);
        int titleIndex = currentPage * TITLES_PER_PAGE + slot;
        
        if (titleIndex >= unlockedTitles.size()) {
            return false;
        }
        
        TitleInfo titleInfo = unlockedTitles.get(titleIndex);
        
        // 检查是否可购买
        if (titleInfo.isPurchasable() && economyIntegration.hasEconomySystem()) {
            return handleTitlePurchase(player, titleInfo);
        } else {
            // 对于非购买称号，不做任何操作，不显示任何提示
            return true;
        }
    }
    
    /**
     * 处理称号购买
     */
    private boolean handleTitlePurchase(Player player, TitleInfo titleInfo) {
        Map<String, Object> purchaseOptions = titleInfo.getPurchaseOptions();

        // 获取价格信息
        Double moneyPrice = purchaseOptions.containsKey("money") ?
            ((Number) purchaseOptions.get("money")).doubleValue() : null;
        Integer pointsPrice = purchaseOptions.containsKey("points") ?
            ((Number) purchaseOptions.get("points")).intValue() : null;

        // 如果同时配置了金币和点券价格，优先使用金币
        if (moneyPrice != null && economyIntegration.isVaultEnabled()) {
            if (economyIntegration.getPlayerMoney(player) >= moneyPrice) {
                // 金币足够，直接使用金币购买
                if (economyIntegration.takeMoney(player, moneyPrice)) {
                    return completePurchase(player, titleInfo, economyIntegration.formatMoney(moneyPrice));
                } else {
                    MessageUtil.sendPrefixedMessage(player, "title.purchase-failed");
                    return true;
                }
            } else if (pointsPrice != null && economyIntegration.isPlayerPointsEnabled()) {
                // 金币不够，尝试使用点券
                if (economyIntegration.getPlayerPoints(player) >= pointsPrice) {
                    if (economyIntegration.takePoints(player, pointsPrice)) {
                        return completePurchase(player, titleInfo, economyIntegration.formatPoints(pointsPrice));
                    } else {
                        MessageUtil.sendPrefixedMessage(player, "title.purchase-failed");
                        return true;
                    }
                } else {
                    // 金币和点券都不够
                    MessageUtil.sendPrefixedMessage(player, "title.insufficient-both",
                        Map.of("money", economyIntegration.formatMoney(moneyPrice),
                               "points", economyIntegration.formatPoints(pointsPrice)));
                    return true;
                }
            } else {
                // 只有金币价格，但金币不够
                MessageUtil.sendPrefixedMessage(player, "title.insufficient-money",
                    "price", economyIntegration.formatMoney(moneyPrice));
                return true;
            }
        } else if (pointsPrice != null && economyIntegration.isPlayerPointsEnabled()) {
            // 只有点券价格或金币系统不可用
            if (economyIntegration.getPlayerPoints(player) >= pointsPrice) {
                if (economyIntegration.takePoints(player, pointsPrice)) {
                    return completePurchase(player, titleInfo, economyIntegration.formatPoints(pointsPrice));
                } else {
                    MessageUtil.sendPrefixedMessage(player, "title.purchase-failed");
                    return true;
                }
            } else {
                MessageUtil.sendPrefixedMessage(player, "title.insufficient-points",
                    "price", economyIntegration.formatPoints(pointsPrice));
                return true;
            }
        }

        MessageUtil.sendPrefixedMessage(player, "title.no-purchase-option");
        return true;
    }

    /**
     * 完成购买流程
     */
    private boolean completePurchase(Player player, TitleInfo titleInfo, String priceText) {
        // 解锁称号
        titleManager.givePlayerTitle(player, titleInfo.getId());

        Map<String, String> placeholders = new java.util.HashMap<>();
        placeholders.put("title", titleInfo.getDisplayName());
        placeholders.put("price", priceText);
        MessageUtil.sendPrefixedMessage(player, "title.purchase-success", placeholders);

        // 刷新界面
        openGUI(player, playerPages.getOrDefault(player.getUniqueId(), 0));
        return true;
    }
    
    /**
     * 清理玩家数据
     */
    public void cleanupPlayerData(UUID playerId) {
        playerPages.remove(playerId);
    }
}
