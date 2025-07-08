package dyzg.zPrefix.listener;

import dyzg.zPrefix.ZPrefix;
import dyzg.zPrefix.gui.TitleGUI;
import dyzg.zPrefix.util.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * GUI事件监听器
 * 处理GUI界面的点击和关闭事件
 */
public class GUIListener implements Listener {
    
    private final ZPrefix plugin;
    private final TitleGUI titleGUI;
    
    public GUIListener(ZPrefix plugin, TitleGUI titleGUI) {
        this.plugin = plugin;
        this.titleGUI = titleGUI;
    }
    
    /**
     * 处理GUI点击事件
     *
     * @param event GUI点击事件
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        // 检查是否为玩家
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();

        // 检查是否为称号GUI
        if (!isTitleGUI(inventory)) {
            return;
        }

        // 立即取消事件，防止任何物品操作
        event.setCancelled(true);

        // 检查点击类型，只允许左键和右键点击
        ClickType clickType = event.getClick();
        if (clickType != ClickType.LEFT && clickType != ClickType.RIGHT) {
            return;
        }

        // 检查是否点击了GUI内的槽位
        if (event.getSlot() < 0 || event.getSlot() >= inventory.getSize()) {
            return;
        }

        // 防止shift点击等特殊操作
        InventoryAction action = event.getAction();
        if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY ||
            action == InventoryAction.COLLECT_TO_CURSOR ||
            action == InventoryAction.HOTBAR_MOVE_AND_READD ||
            action == InventoryAction.HOTBAR_SWAP) {
            return;
        }

        try {
            int slot = event.getSlot();
            ItemStack clickedItem = event.getCurrentItem();

            // 处理点击
            boolean handled = titleGUI.handleGUIClick(player, slot, clickedItem);

            if (!handled && plugin.getConfigManager().getConfigValue("debug", false)) {
                plugin.getLogger().info("玩家 " + player.getName() + " 点击了GUI中的空白区域，槽位: " + slot);
            }

        } catch (Exception e) {
            plugin.getLogger().warning("处理玩家 " + player.getName() + " 的GUI点击事件时出错: " + e.getMessage());
            player.closeInventory();
            MessageUtil.sendPrefixedMessage(player, "common.error", "error", "GUI操作出错，请重试");
        }
    }

    /**
     * 处理GUI拖拽事件
     *
     * @param event GUI拖拽事件
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        // 检查是否为玩家
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Inventory inventory = event.getInventory();

        // 检查是否为称号GUI
        if (!isTitleGUI(inventory)) {
            return;
        }

        // 完全禁止拖拽操作
        event.setCancelled(true);
    }
    
    /**
     * 处理GUI关闭事件
     * 
     * @param event GUI关闭事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        // 检查是否为玩家
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        Inventory inventory = event.getInventory();
        
        // 检查是否为称号GUI
        if (!isTitleGUI(inventory)) {
            return;
        }
        
        try {
            // 清理玩家GUI相关数据
            titleGUI.cleanupPlayerData(player.getUniqueId());
            
        } catch (Exception e) {
            plugin.getLogger().warning("处理玩家 " + player.getName() + " 的GUI关闭事件时出错: " + e.getMessage());
        }
    }
    
    /**
     * 检查是否为称号GUI
     *
     * @param inventory 库存界面
     * @return 是否为称号GUI
     */
    private boolean isTitleGUI(Inventory inventory) {
        if (inventory == null) {
            return false;
        }

        // 使用 InventoryView 来获取标题
        if (inventory.getViewers().isEmpty()) {
            return false;
        }

        String title = inventory.getViewers().get(0).getOpenInventory().getTitle();
        String expectedTitlePrefix = MessageUtil.colorize("§6§l称号系统");

        // 检查标题是否以"§6§l称号系统"开头（因为现在包含页码信息）
        return title != null && title.startsWith(expectedTitlePrefix);
    }
}
