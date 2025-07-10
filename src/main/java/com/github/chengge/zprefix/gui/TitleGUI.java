package com.github.chengge.zprefix.gui;

import com.github.chengge.zprefix.ZPrefix;
import com.github.chengge.zprefix.data.PlayerTitleData;
import com.github.chengge.zprefix.data.TitleInfo;
import com.github.chengge.zprefix.manager.ConfigManager;
import com.github.chengge.zprefix.manager.TitleManager;
import com.github.chengge.zprefix.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * 称号GUI界面
 * 固定54槽位，前5行显示称号，最后一行为按钮区域
 */
public class TitleGUI {
    
    private final ZPrefix plugin;
    private final ConfigManager configManager;
    private final TitleManager titleManager;
    private final Map<UUID, Integer> playerPages = new HashMap<>();
    
    // GUI配置常量
    private static final int GUI_SIZE = 54;           // 固定6行
    private static final int TITLES_PER_PAGE = 45;   // 前5行用于显示称号
    private static final int BUTTON_ROW_START = 45;  // 最后一行开始位置
    
    // 按钮槽位
    private static final int PREV_PAGE_SLOT = 45;
    private static final int CLOSE_SLOT = 47;
    private static final int PAGE_INFO_SLOT = 49;
    private static final int REFRESH_SLOT = 51;
    private static final int NEXT_PAGE_SLOT = 53;
    
    public TitleGUI(ZPrefix plugin, ConfigManager configManager, TitleManager titleManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.titleManager = titleManager;
    }
    
    /**
     * 打开称号GUI
     */
    public void openGUI(Player player) {
        openGUI(player, 0);
    }
    
    /**
     * 打开指定页面的称号GUI
     */
    public void openGUI(Player player, int page) {
        PlayerTitleData playerData = titleManager.getPlayerData(player);
        List<String> unlockedTitles = new ArrayList<>(playerData.getUnlockedTitles());
        
        // 计算总页数
        int totalPages = Math.max(1, (int) Math.ceil((double) unlockedTitles.size() / TITLES_PER_PAGE));
        
        // 确保页面在有效范围内
        page = Math.max(0, Math.min(page, totalPages - 1));
        playerPages.put(player.getUniqueId(), page);

        // 调试信息
        if (plugin.getConfigManager().getConfigValue("debug", false)) {
            plugin.getLogger().info("保存玩家 " + player.getName() + " 页面状态: " + page);
        }
        
        // 创建GUI
        String title = MessageUtil.colorize(configManager.getConfigValue("gui.title", "§6§l称号系统"));
        Inventory gui = Bukkit.createInventory(null, GUI_SIZE, title + " §7(" + (page + 1) + "/" + totalPages + ")");
        
        // 填充称号
        fillTitles(gui, player, unlockedTitles, page);
        
        // 填充按钮行
        fillButtonRow(gui, player, page, totalPages, unlockedTitles.size(), playerData.getUnlockedTitleCount());
        
        player.openInventory(gui);
    }
    
    /**
     * 填充称号到GUI中
     */
    private void fillTitles(Inventory gui, Player player, List<String> unlockedTitles, int page) {
        PlayerTitleData playerData = titleManager.getPlayerData(player);
        String currentTitle = playerData.getCurrentTitle();
        
        int startIndex = page * TITLES_PER_PAGE;
        int endIndex = Math.min(startIndex + TITLES_PER_PAGE, unlockedTitles.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            String titleId = unlockedTitles.get(i);
            TitleInfo titleInfo = configManager.getTitleInfo(titleId);
            
            if (titleInfo != null) {
                boolean isCurrent = titleId.equals(currentTitle);
                ItemStack titleItem = titleInfo.createGuiItem(true, isCurrent);
                gui.setItem(i - startIndex, titleItem);
            }
        }
    }
    
    /**
     * 填充按钮行
     */
    private void fillButtonRow(Inventory gui, Player player, int currentPage, int totalPages,
                              int totalTitles, int unlockedTitles) {

        // 调试信息
        if (plugin.getConfigManager().getConfigValue("debug", false)) {
            plugin.getLogger().info("填充按钮行 - 当前页: " + currentPage + ", 总页数: " + totalPages +
                                  ", 总称号: " + totalTitles + ", 已解锁: " + unlockedTitles);
        }

        // 先填充装饰物品到整个按钮行
        ItemStack fillItem = createFillItem();
        for (int i = BUTTON_ROW_START; i < GUI_SIZE; i++) {
            gui.setItem(i, fillItem);
        }

        // 然后设置具体的按钮（会覆盖装饰物品）

        // 分页按钮逻辑优化
        if (totalPages > 1) {
            // 多页时显示分页按钮

            // 上一页按钮
            if (currentPage > 0) {
                gui.setItem(PREV_PAGE_SLOT, createPreviousPageButton(currentPage, totalPages));
                if (plugin.getConfigManager().getConfigValue("debug", false)) {
                    plugin.getLogger().info("设置上一页按钮在槽位: " + PREV_PAGE_SLOT);
                }
            } else {
                // 第一页时显示禁用的按钮
                gui.setItem(PREV_PAGE_SLOT, createDisabledButton("§7◀ 上一页", "§c已经是第一页"));
            }

            // 下一页按钮
            if (currentPage < totalPages - 1) {
                gui.setItem(NEXT_PAGE_SLOT, createNextPageButton(currentPage, totalPages));
                if (plugin.getConfigManager().getConfigValue("debug", false)) {
                    plugin.getLogger().info("设置下一页按钮在槽位: " + NEXT_PAGE_SLOT);
                }
            } else {
                // 最后一页时显示禁用的按钮
                gui.setItem(NEXT_PAGE_SLOT, createDisabledButton("§7下一页 ▶", "§c已经是最后一页"));
            }
        } else {
            // 单页时隐藏分页按钮，保持装饰物品
            if (plugin.getConfigManager().getConfigValue("debug", false)) {
                plugin.getLogger().info("只有一页，隐藏分页按钮");
            }
        }

        // 其他按钮总是显示
        gui.setItem(CLOSE_SLOT, createCloseButton());
        gui.setItem(PAGE_INFO_SLOT, createPageInfoButton(currentPage, totalPages, totalTitles, unlockedTitles));
        gui.setItem(REFRESH_SLOT, createRefreshButton());
    }
    
    /**
     * 创建填充物品
     */
    private ItemStack createFillItem() {
        String materialName = configManager.getConfigValue("gui.fill-item.material", "GRAY_STAINED_GLASS_PANE");
        String itemName = configManager.getConfigValue("gui.fill-item.name", " ");
        
        Material material;
        try {
            material = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            material = Material.GRAY_STAINED_GLASS_PANE;
        }
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageUtil.colorize(itemName));
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * 创建上一页按钮
     */
    private ItemStack createPreviousPageButton(int currentPage, int totalPages) {
        String materialName = configManager.getConfigValue("gui.buttons.previous-page.material", "ARROW");
        String name = configManager.getConfigValue("gui.buttons.previous-page.name", "§a◀ 上一页");
        List<String> lore = configManager.getConfigValue("gui.buttons.previous-page.lore", 
            Arrays.asList("§7点击查看上一页称号", "§7当前页: §e{current_page}", "§7总页数: §e{total_pages}"));
        
        return createButton(materialName, name, lore, currentPage, totalPages, 0, 0);
    }
    
    /**
     * 创建下一页按钮
     */
    private ItemStack createNextPageButton(int currentPage, int totalPages) {
        String materialName = configManager.getConfigValue("gui.buttons.next-page.material", "ARROW");
        String name = configManager.getConfigValue("gui.buttons.next-page.name", "§a下一页 ▶");
        List<String> lore = configManager.getConfigValue("gui.buttons.next-page.lore", 
            Arrays.asList("§7点击查看下一页称号", "§7当前页: §e{current_page}", "§7总页数: §e{total_pages}"));
        
        return createButton(materialName, name, lore, currentPage, totalPages, 0, 0);
    }
    
    /**
     * 创建页面信息按钮
     */
    private ItemStack createPageInfoButton(int currentPage, int totalPages, int totalTitles, int unlockedTitles) {
        String materialName = configManager.getConfigValue("gui.buttons.page-info.material", "BOOK");
        String name = configManager.getConfigValue("gui.buttons.page-info.name", "§6页面信息");
        List<String> lore = configManager.getConfigValue("gui.buttons.page-info.lore", 
            Arrays.asList("§7当前页: §e{current_page} §7/ §e{total_pages}", 
                         "§7称号总数: §e{total_titles}", "§7已解锁: §a{unlocked_titles}"));
        
        return createButton(materialName, name, lore, currentPage, totalPages, totalTitles, unlockedTitles);
    }
    
    /**
     * 创建关闭按钮
     */
    private ItemStack createCloseButton() {
        String materialName = configManager.getConfigValue("gui.buttons.close.material", "BARRIER");
        String name = configManager.getConfigValue("gui.buttons.close.name", "§c✖ 关闭");
        List<String> lore = configManager.getConfigValue("gui.buttons.close.lore", 
            Arrays.asList("§7点击关闭界面"));
        
        return createButton(materialName, name, lore, 0, 0, 0, 0);
    }
    
    /**
     * 创建刷新按钮
     */
    private ItemStack createRefreshButton() {
        String materialName = configManager.getConfigValue("gui.buttons.refresh.material", "EMERALD");
        String name = configManager.getConfigValue("gui.buttons.refresh.name", "§a⟲ 刷新");
        List<String> lore = configManager.getConfigValue("gui.buttons.refresh.lore",
            Arrays.asList("§7点击刷新界面", "§7重新加载称号数据", "§7清理无效的称号数据"));

        return createButton(materialName, name, lore, 0, 0, 0, 0);
    }

    /**
     * 创建禁用按钮
     */
    private ItemStack createDisabledButton(String name, String reason) {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageUtil.colorize(name));
            meta.setLore(Arrays.asList(MessageUtil.colorize(reason)));
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * 创建按钮物品
     */
    private ItemStack createButton(String materialName, String name, List<String> lore, 
                                  int currentPage, int totalPages, int totalTitles, int unlockedTitles) {
        Material material;
        try {
            material = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            material = Material.STONE;
        }
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // 设置名称
            meta.setDisplayName(MessageUtil.colorize(name));
            
            // 设置lore并替换占位符
            List<String> processedLore = new ArrayList<>();
            for (String line : lore) {
                String processed = line
                    .replace("{current_page}", String.valueOf(currentPage + 1))
                    .replace("{total_pages}", String.valueOf(totalPages))
                    .replace("{total_titles}", String.valueOf(totalTitles))
                    .replace("{unlocked_titles}", String.valueOf(unlockedTitles));
                processedLore.add(MessageUtil.colorize(processed));
            }
            meta.setLore(processedLore);
            
            item.setItemMeta(meta);
        }
        
        return item;
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
            return handleTitleClick(player, clickedItem);
        }

        return false;
    }

    /**
     * 处理按钮点击
     */
    private boolean handleButtonClick(Player player, int slot, ItemStack clickedItem) {
        UUID playerId = player.getUniqueId();

        // 从GUI标题中解析当前页面（更可靠的方法）
        int currentPage = getCurrentPageFromGUI(player);

        // 调试信息
        if (plugin.getConfigManager().getConfigValue("debug", false)) {
            plugin.getLogger().info("玩家 " + player.getName() + " 点击按钮槽位: " + slot);
            plugin.getLogger().info("从映射获取的页面: " + playerPages.getOrDefault(playerId, 0));
            plugin.getLogger().info("从GUI标题解析的页面: " + currentPage);
        }

        switch (slot) {
            case PREV_PAGE_SLOT:
                if (plugin.getConfigManager().getConfigValue("debug", false)) {
                    plugin.getLogger().info("点击上一页按钮，当前页: " + currentPage);
                }
                if (currentPage > 0) {
                    openGUI(player, currentPage - 1);
                } else {
                    if (plugin.getConfigManager().getConfigValue("debug", false)) {
                        plugin.getLogger().info("已经是第一页，无法返回上一页");
                    }
                }
                return true;

            case NEXT_PAGE_SLOT:
                PlayerTitleData playerData = titleManager.getPlayerData(player);
                int totalPages = Math.max(1, (int) Math.ceil((double) playerData.getUnlockedTitleCount() / TITLES_PER_PAGE));
                if (plugin.getConfigManager().getConfigValue("debug", false)) {
                    plugin.getLogger().info("点击下一页按钮，当前页: " + currentPage + ", 总页数: " + totalPages);
                }
                if (currentPage < totalPages - 1) {
                    openGUI(player, currentPage + 1);
                } else {
                    if (plugin.getConfigManager().getConfigValue("debug", false)) {
                        plugin.getLogger().info("已经是最后一页，无法前往下一页");
                    }
                }
                return true;

            case CLOSE_SLOT:
                player.closeInventory();
                return true;

            case REFRESH_SLOT:
                // 刷新时清理无效的称号数据
                PlayerTitleData refreshPlayerData = titleManager.getPlayerData(player);
                int cleanedCount = cleanupInvalidTitles(player, refreshPlayerData);

                // 显示清理结果
                if (cleanedCount > 0) {
                    MessageUtil.sendPrefixedMessage(player, "title.cleanup-success",
                        "count", String.valueOf(cleanedCount));
                } else {
                    MessageUtil.sendPrefixedMessage(player, "title.refresh-success");
                }

                // 重新打开GUI
                openGUI(player, currentPage);
                return true;

            case PAGE_INFO_SLOT:
                // 页面信息按钮，不做任何操作
                return true;

            default:
                if (plugin.getConfigManager().getConfigValue("debug", false)) {
                    plugin.getLogger().info("点击了未处理的按钮槽位: " + slot);
                }
                return false;
        }
    }

    /**
     * 处理称号点击
     */
    private boolean handleTitleClick(Player player, ItemStack clickedItem) {
        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null || meta.getDisplayName() == null) {
            return false;
        }

        String itemName = meta.getDisplayName();
        PlayerTitleData playerData = titleManager.getPlayerData(player);

        // 查找匹配的称号
        for (String titleId : playerData.getUnlockedTitles()) {
            TitleInfo titleInfo = configManager.getTitleInfo(titleId);
            if (titleInfo != null) {
                ItemStack guiItem = titleInfo.getGuiItem();
                if (guiItem != null && guiItem.hasItemMeta()) {
                    ItemMeta guiMeta = guiItem.getItemMeta();
                    if (guiMeta != null && guiMeta.getDisplayName() != null) {
                        if (guiMeta.getDisplayName().equals(itemName)) {
                            // 检查是否已经是当前称号
                            if (titleId.equals(playerData.getCurrentTitle())) {
                                MessageUtil.sendPrefixedMessage(player, "title.same-title");
                                return true;
                            }

                            // 切换称号
                            if (titleManager.setPlayerTitle(player, titleId)) {
                                MessageUtil.sendPrefixedMessage(player, "title.title-set", "title", titleInfo.getDisplayName());
                                player.closeInventory();
                            } else {
                                MessageUtil.sendPrefixedMessage(player, "title.title-not-unlocked");
                            }
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * 清理无效的称号数据
     * 检查玩家拥有的称号是否在配置文件中存在，如果不存在则移除
     *
     * @param player 玩家
     * @param playerData 玩家称号数据
     * @return 清理的无效称号数量
     */
    private int cleanupInvalidTitles(Player player, PlayerTitleData playerData) {
        return titleManager.cleanupInvalidTitles(player);
    }

    /**
     * 清理玩家数据
     */
    public void cleanupPlayerData(UUID playerId) {
        playerPages.remove(playerId);
    }

    /**
     * 获取玩家当前页面
     */
    public int getPlayerPage(Player player) {
        return playerPages.getOrDefault(player.getUniqueId(), 0);
    }

    /**
     * 从GUI标题中解析当前页面
     */
    private int getCurrentPageFromGUI(Player player) {
        try {
            String title = player.getOpenInventory().getTitle();
            // 标题格式: "§6§l称号系统 §7(2/3)"
            if (title.contains("(") && title.contains("/") && title.contains(")")) {
                String pageInfo = title.substring(title.indexOf("(") + 1, title.indexOf(")"));
                String[] parts = pageInfo.split("/");
                if (parts.length == 2) {
                    int currentPage = Integer.parseInt(parts[0].trim()) - 1; // 转换为0基索引
                    return Math.max(0, currentPage);
                }
            }
        } catch (Exception e) {
            // 解析失败，使用映射中的值
            if (plugin.getConfigManager().getConfigValue("debug", false)) {
                plugin.getLogger().warning("解析GUI标题失败: " + e.getMessage());
            }
        }

        // 如果解析失败，回退到映射中的值
        return playerPages.getOrDefault(player.getUniqueId(), 0);
    }
}
