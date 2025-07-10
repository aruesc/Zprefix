package com.github.chengge.zprefix.listener;

import com.github.chengge.zprefix.ZPrefix;
import com.github.chengge.zprefix.manager.VanillaStatsManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;

/**
 * 原版统计监听器
 * 监听原版统计数据变化，检查称号解锁条件
 */
public class VanillaStatsListener implements Listener {
    
    private final ZPrefix plugin;
    private final VanillaStatsManager vanillaStatsManager;
    private int checkTaskId = -1;
    
    public VanillaStatsListener(ZPrefix plugin, VanillaStatsManager vanillaStatsManager) {
        this.plugin = plugin;
        this.vanillaStatsManager = vanillaStatsManager;
        startPeriodicCheck();
    }
    
    /**
     * 玩家加入时检查解锁条件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // 检查是否启用加入时检查
        boolean checkOnJoin = plugin.getConfigManager().getConfigValue("progress.check-on-join", true);
        if (checkOnJoin) {
            // 延迟1秒检查，确保玩家完全加载
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                vanillaStatsManager.checkAllUnlockConditions(player);
            }, 20L);
        }
    }
    
    /**
     * 监听统计数据变化
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerStatisticIncrement(PlayerStatisticIncrementEvent event) {
        Player player = event.getPlayer();
        
        // 延迟检查，避免频繁触发
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            vanillaStatsManager.checkAllUnlockConditions(player);
        }, 1L);
        
        // 调试日志
        if (plugin.getConfigManager().getConfigValue("debug", false) &&
            plugin.getConfigManager().getConfigValue("progress.debug-logging", false)) {
            plugin.getLogger().info("玩家 " + player.getName() + " 统计数据更新: " +
                                  event.getStatistic().name() + " -> " + event.getNewValue());
        }
    }
    
    /**
     * 启动定期检查任务
     */
    private void startPeriodicCheck() {
        int checkInterval = plugin.getConfigManager().getConfigValue("progress.check-interval", 60);

        checkTaskId = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            // 为所有在线玩家检查解锁条件
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    vanillaStatsManager.checkAllUnlockConditions(player);
                });
            }
        }, checkInterval * 20L, checkInterval * 20L).getTaskId();

        plugin.getLogger().info("✓ 原版统计检查任务已启动，检查间隔: " + checkInterval + "秒");
    }
    
    /**
     * 停止定期检查任务
     */
    public void stopPeriodicCheck() {
        if (checkTaskId != -1) {
            plugin.getServer().getScheduler().cancelTask(checkTaskId);
            checkTaskId = -1;
            plugin.getLogger().info("✓ 原版统计检查任务已停止");
        }
    }
}
