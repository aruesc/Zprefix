package dyzg.zPrefix.listener;

import dyzg.zPrefix.ZPrefix;
import dyzg.zPrefix.manager.TitleManager;
import dyzg.zPrefix.util.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * 玩家事件监听器
 * 处理玩家加入、离开等事件
 */
public class PlayerListener implements Listener {
    
    private final ZPrefix plugin;
    private final TitleManager titleManager;
    
    public PlayerListener(ZPrefix plugin, TitleManager titleManager) {
        this.plugin = plugin;
        this.titleManager = titleManager;
    }
    
    /**
     * 玩家加入事件
     * 
     * @param event 玩家加入事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        try {
            // 延迟处理，确保玩家完全加载
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                titleManager.onPlayerJoin(player);
                
                // 如果是第一次加入且启用了欢迎信息，显示欢迎信息
                if (!player.hasPlayedBefore() && isWelcomeMessageEnabled()) {
                    showWelcomeMessage(player);
                }
            }, 20L); // 延迟1秒
            
        } catch (Exception e) {
            plugin.getLogger().warning("处理玩家 " + player.getName() + " 加入事件时出错: " + e.getMessage());
        }
    }
    
    /**
     * 玩家离开事件
     * 
     * @param event 玩家离开事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        try {
            titleManager.onPlayerQuit(player);
        } catch (Exception e) {
            plugin.getLogger().warning("处理玩家 " + player.getName() + " 离开事件时出错: " + e.getMessage());
        }
    }
    
    /**
     * 检查是否启用欢迎信息
     *
     * @return 是否启用欢迎信息
     */
    private boolean isWelcomeMessageEnabled() {
        return plugin.getConfigManager().getConfigValue("player.show-welcome-message", false);
    }

    /**
     * 显示欢迎信息
     *
     * @param player 玩家
     */
    private void showWelcomeMessage(Player player) {
        // 延迟发送欢迎信息，确保玩家已完全加载
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            player.sendMessage("");
            player.sendMessage("§6§l=== 欢迎来到服务器 ===");
            player.sendMessage("§e你已自动获得新手称号！");
            player.sendMessage("§e使用 §a/title §e命令打开称号界面");
            player.sendMessage("§e完成各种任务可以解锁更多称号！");
            player.sendMessage("§6§l=====================");
            player.sendMessage("");
        }, 40L); // 延迟2秒
    }
}
