package com.github.chengge.zprefix.manager;

import com.github.chengge.zprefix.ZPrefix;
import com.github.chengge.zprefix.data.PlayerTitleData;
import com.github.chengge.zprefix.data.TitleInfo;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * 称号管理器
 * 负责管理玩家称号数据的存储和访问
 */
public class TitleManager {
    
    private final ZPrefix plugin;
    private final ConfigManager configManager;
    private final BuffManager buffManager;
    private final Map<UUID, PlayerTitleData> playerDataMap = new ConcurrentHashMap<>();
    private File dataFile;
    private FileConfiguration dataConfig;
    
    public TitleManager(ZPrefix plugin, ConfigManager configManager, BuffManager buffManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.buffManager = buffManager;
    }
    
    /**
     * 初始化称号管理器
     */
    public void initialize() {
        setupDataFile();
        loadPlayerData();
    }
    
    /**
     * 设置数据文件
     */
    private void setupDataFile() {
        String filePath = configManager.getConfigValue("database.file-path", "data/player_titles.yml");
        dataFile = new File(plugin.getDataFolder(), filePath);
        
        // 确保目录存在
        File parentDir = dataFile.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        // 创建文件（如果不存在）
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "无法创建数据文件: " + dataFile.getPath(), e);
            }
        }
        
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }
    
    /**
     * 加载玩家数据
     */
    private void loadPlayerData() {
        if (dataConfig.getConfigurationSection("players") == null) {
            return;
        }
        
        for (String uuidString : dataConfig.getConfigurationSection("players").getKeys(false)) {
            try {
                UUID playerId = UUID.fromString(uuidString);
                String currentTitle = dataConfig.getString("players." + uuidString + ".current-title");
                List<String> unlockedTitles = dataConfig.getStringList("players." + uuidString + ".unlocked-titles");
                
                PlayerTitleData playerData = new PlayerTitleData(playerId, currentTitle, new HashSet<>(unlockedTitles));
                playerDataMap.put(playerId, playerData);
                
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("无效的UUID格式: " + uuidString);
            }
        }
        
        plugin.getLogger().info("成功加载 " + playerDataMap.size() + " 个玩家的称号数据");
    }
    
    /**
     * 保存玩家数据
     */
    public void savePlayerData() {
        for (Map.Entry<UUID, PlayerTitleData> entry : playerDataMap.entrySet()) {
            UUID playerId = entry.getKey();
            PlayerTitleData playerData = entry.getValue();
            
            String path = "players." + playerId.toString();
            dataConfig.set(path + ".current-title", playerData.getCurrentTitle());
            dataConfig.set(path + ".unlocked-titles", new ArrayList<>(playerData.getUnlockedTitles()));
        }
        
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "保存玩家数据时出错", e);
        }
    }
    
    /**
     * 获取玩家称号数据
     * 
     * @param playerId 玩家UUID
     * @return 玩家称号数据
     */
    public PlayerTitleData getPlayerData(UUID playerId) {
        return playerDataMap.computeIfAbsent(playerId, PlayerTitleData::new);
    }
    
    /**
     * 获取玩家称号数据
     * 
     * @param player 玩家
     * @return 玩家称号数据
     */
    public PlayerTitleData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }
    
    /**
     * 设置玩家当前称号
     * 
     * @param player 玩家
     * @param titleId 称号ID
     * @return 是否成功设置
     */
    public boolean setPlayerTitle(Player player, String titleId) {
        if (titleId != null && !configManager.titleExists(titleId)) {
            return false;
        }
        
        PlayerTitleData playerData = getPlayerData(player);
        
        // 检查是否已解锁
        if (titleId != null && !playerData.hasUnlockedTitle(titleId)) {
            return false;
        }
        
        // 设置称号
        playerData.setCurrentTitle(titleId);
        
        // 应用属性加成
        TitleInfo titleInfo = titleId != null ? configManager.getTitleInfo(titleId) : null;
        buffManager.refreshTitleBuffs(player, titleInfo);

        // 显示简化的成功信息
        if (titleInfo != null) {
            plugin.getLogger().info("玩家 " + player.getName() + " 切换称号为: " + titleInfo.getDisplayName());
        } else {
            plugin.getLogger().info("玩家 " + player.getName() + " 移除了当前称号");
        }

        return true;
    }
    
    /**
     * 移除玩家当前称号
     * 
     * @param player 玩家
     */
    public void removePlayerTitle(Player player) {
        PlayerTitleData playerData = getPlayerData(player);
        playerData.removeCurrentTitle();
        
        // 移除属性加成
        buffManager.removeTitleBuffs(player);
    }
    
    /**
     * 给予玩家称号
     * 
     * @param playerId 玩家UUID
     * @param titleId 称号ID
     * @return 是否成功给予
     */
    public boolean givePlayerTitle(UUID playerId, String titleId) {
        if (!configManager.titleExists(titleId)) {
            return false;
        }
        
        PlayerTitleData playerData = getPlayerData(playerId);
        return playerData.unlockTitle(titleId);
    }
    
    /**
     * 给予玩家称号
     * 
     * @param player 玩家
     * @param titleId 称号ID
     * @return 是否成功给予
     */
    public boolean givePlayerTitle(Player player, String titleId) {
        boolean result = givePlayerTitle(player.getUniqueId(), titleId);
        
        // 如果是当前在线玩家且没有当前称号，自动设置为当前称号
        if (result) {
            PlayerTitleData playerData = getPlayerData(player);
            if (!playerData.hasCurrentTitle()) {
                setPlayerTitle(player, titleId);
            }
        }
        
        return result;
    }
    
    /**
     * 移除玩家称号
     * 
     * @param playerId 玩家UUID
     * @param titleId 称号ID
     * @return 是否成功移除
     */
    public boolean takePlayerTitle(UUID playerId, String titleId) {
        PlayerTitleData playerData = getPlayerData(playerId);
        return playerData.removeUnlockedTitle(titleId);
    }
    
    /**
     * 移除玩家称号
     * 
     * @param player 玩家
     * @param titleId 称号ID
     * @return 是否成功移除
     */
    public boolean takePlayerTitle(Player player, String titleId) {
        PlayerTitleData playerData = getPlayerData(player);
        boolean result = playerData.removeUnlockedTitle(titleId);
        
        // 如果移除的是当前使用的称号，需要刷新属性加成
        if (result && titleId.equals(playerData.getCurrentTitle())) {
            buffManager.removeTitleBuffs(player);
        }
        
        return result;
    }
    
    /**
     * 检查玩家是否拥有称号
     * 
     * @param playerId 玩家UUID
     * @param titleId 称号ID
     * @return 是否拥有
     */
    public boolean hasTitle(UUID playerId, String titleId) {
        PlayerTitleData playerData = getPlayerData(playerId);
        return playerData.hasUnlockedTitle(titleId);
    }
    
    /**
     * 检查玩家是否拥有称号
     * 
     * @param player 玩家
     * @param titleId 称号ID
     * @return 是否拥有
     */
    public boolean hasTitle(Player player, String titleId) {
        return hasTitle(player.getUniqueId(), titleId);
    }
    
    /**
     * 获取玩家当前称号
     * 
     * @param playerId 玩家UUID
     * @return 当前称号ID，如果没有则返回null
     */
    public String getCurrentTitle(UUID playerId) {
        PlayerTitleData playerData = getPlayerData(playerId);
        return playerData.getCurrentTitle();
    }
    
    /**
     * 获取玩家当前称号
     * 
     * @param player 玩家
     * @return 当前称号ID，如果没有则返回null
     */
    public String getCurrentTitle(Player player) {
        return getCurrentTitle(player.getUniqueId());
    }
    
    /**
     * 获取玩家已解锁的称号列表
     * 
     * @param playerId 玩家UUID
     * @return 已解锁的称号ID集合
     */
    public Set<String> getUnlockedTitles(UUID playerId) {
        PlayerTitleData playerData = getPlayerData(playerId);
        return playerData.getUnlockedTitles();
    }
    
    /**
     * 获取玩家已解锁的称号列表
     * 
     * @param player 玩家
     * @return 已解锁的称号ID集合
     */
    public Set<String> getUnlockedTitles(Player player) {
        return getUnlockedTitles(player.getUniqueId());
    }
    
    /**
     * 玩家加入时的处理
     * 确保玩家属性状态正确初始化
     *
     * @param player 玩家
     */
    public void onPlayerJoin(Player player) {
        try {
            PlayerTitleData playerData = getPlayerData(player);

            // 第一步：强制重置玩家属性为默认状态
            resetPlayerAttributesToDefault(player);

            // 第二步：检查是否需要给予默认称号
            if (!playerData.hasAnyUnlockedTitle()) {
                giveAutoUnlockTitles(player);
                // 重新获取数据，因为可能有新的称号被解锁
                playerData = getPlayerData(player);
            }

            // 第三步：按正确顺序应用当前称号的属性加成
            String currentTitle = playerData.getCurrentTitle();
            if (currentTitle != null) {
                TitleInfo titleInfo = configManager.getTitleInfo(currentTitle);
                if (titleInfo != null) {
                    // 延迟应用属性，确保玩家完全加载
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        applyTitleAttributesWithOrder(player, titleInfo);

                        // 再次延迟确保生命值正确（不强制满血，保持原有血量）
                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                            ensurePlayerHealthIsCorrect(player);
                        }, 3L); // 再延迟0.15秒
                    }, 5L); // 延迟0.25秒
                } else {
                    // 称号配置不存在，清除无效的当前称号
                    playerData.removeCurrentTitle();
                    plugin.getLogger().warning("玩家 " + player.getName() + " 的当前称号 " + currentTitle + " 不存在，已清除");
                }
            }

            // 只在调试模式下显示详细信息
            if (plugin.getConfigManager().getConfigValue("debug", false)) {
                plugin.getLogger().info("玩家 " + player.getName() + " 加入处理完成，当前称号: " +
                    (currentTitle != null ? currentTitle : "无"));
            }

        } catch (Exception e) {
            plugin.getLogger().warning("处理玩家 " + player.getName() + " 加入时出错: " + e.getMessage());
        }
    }

    /**
     * 重置玩家属性为默认状态
     * 移除所有可能的属性修改器，确保干净的起始状态
     *
     * @param player 玩家
     */
    private void resetPlayerAttributesToDefault(Player player) {
        // 强制清理所有可能的遗留属性修改器
        buffManager.forceCleanupPlayerAttributes(player);

        // 确保SagaLoreStats相关的属性也被清理
        if (plugin.getSagaIntegration() != null) {
            try {
                plugin.getSagaIntegration().cleanupPlayerData(player.getUniqueId());
            } catch (Exception e) {
                // SagaLoreStats可能未启动，忽略错误
                plugin.getLogger().info("SagaLoreStats清理失败（插件可能未启动）: " + e.getMessage());
            }
        }

        // 只在调试模式下显示信息
        if (plugin.getConfigManager().getConfigValue("debug", false)) {
            plugin.getLogger().info("已重置玩家 " + player.getName() + " 的属性为默认状态");
        }
    }

    /**
     * 按正确顺序应用称号属性
     * 先应用原版属性，再应用其他插件属性
     *
     * @param player 玩家
     * @param titleInfo 称号信息
     */
    private void applyTitleAttributesWithOrder(Player player, TitleInfo titleInfo) {
        try {
            // 应用称号属性（BuffManager会按正确顺序处理）
            buffManager.applyTitleBuffs(player, titleInfo);

            // 只在调试模式下显示详细信息
            if (plugin.getConfigManager().getConfigValue("debug", false)) {
                plugin.getLogger().info("为玩家 " + player.getName() + " 应用称号 " + titleInfo.getDisplayName() + " 的属性");
            }

        } catch (Exception e) {
            plugin.getLogger().warning("为玩家 " + player.getName() + " 应用称号属性时出错: " + e.getMessage());
        }
    }

    /**
     * 确保玩家生命值正确
     * 在属性应用完成后调用，确保玩家有正确的生命值状态
     * 保持玩家下线时的血量比例，而不是强制满血
     *
     * @param player 玩家
     */
    private void ensurePlayerHealthIsCorrect(Player player) {
        try {
            // 获取玩家的最大生命值属性
            Attribute healthAttribute = getMaxHealthAttribute();

            if (healthAttribute != null) {
                AttributeInstance healthInstance = player.getAttribute(healthAttribute);
                if (healthInstance != null) {
                    double newMaxHealth = healthInstance.getValue();
                    double currentHealth = player.getHealth();

                    // 只有当当前生命值超过新的最大生命值时才调整
                    // 这种情况通常发生在移除了增加生命值的称号时
                    if (currentHealth > newMaxHealth) {
                        player.setHealth(newMaxHealth);

                        // 只在调试模式下显示详细信息
                        if (plugin.getConfigManager().getConfigValue("debug", false)) {
                            plugin.getLogger().info("为玩家 " + player.getName() + " 限制生命值: " +
                                                  currentHealth + " -> " + newMaxHealth + " (超过最大值)");
                        }
                    } else {
                        // 正常情况：保持玩家当前的血量，不做任何调整
                        if (plugin.getConfigManager().getConfigValue("debug", false)) {
                            plugin.getLogger().info("玩家 " + player.getName() + " 生命值正常: " +
                                                  currentHealth + "/" + newMaxHealth + " (保持原有血量)");
                        }
                    }
                }
            }

        } catch (Exception e) {
            // 生命值检查失败不应该影响其他功能
            plugin.getLogger().warning("为玩家 " + player.getName() + " 检查生命值时出错: " + e.getMessage());
        }
    }

    /**
     * 获取最大生命值属性（兼容不同版本）
     *
     * @return 最大生命值属性，如果找不到则返回null
     */
    private Attribute getMaxHealthAttribute() {
        // 尝试获取最大生命值属性（兼容不同版本）
        try {
            return Attribute.valueOf("GENERIC_MAX_HEALTH");
        } catch (Exception e) {
            try {
                return Attribute.valueOf("MAX_HEALTH");
            } catch (Exception e2) {
                // 如果都获取不到，尝试通过AttributeAdapter
                Set<Attribute> availableAttributes = com.github.chengge.zprefix.util.AttributeAdapter.getAllAvailableAttributes();
                for (Attribute attr : availableAttributes) {
                    String name = com.github.chengge.zprefix.util.AttributeAdapter.getAttributeName(attr);
                    if (name != null && name.contains("MAX_HEALTH")) {
                        return attr;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * 玩家离开时的处理
     *
     * @param player 玩家
     */
    public void onPlayerQuit(Player player) {
        // 先移除所有属性修改器
        buffManager.removeTitleBuffs(player);

        // 然后清理缓存数据
        buffManager.cleanupPlayerData(player.getUniqueId());

        // 清理GUI数据
        plugin.getTitleGUI().cleanupPlayerData(player.getUniqueId());

        // 只在调试模式下显示详细信息
        if (plugin.getConfigManager().getConfigValue("debug", false)) {
            plugin.getLogger().info("玩家 " + player.getName() + " 离开，已清理所有称号属性");
        }
    }
    
    /**
     * 给予自动解锁的称号
     * 
     * @param player 玩家
     */
    private void giveAutoUnlockTitles(Player player) {
        for (TitleInfo titleInfo : configManager.getAllTitles().values()) {
            if (titleInfo.isAutoUnlock()) {
                givePlayerTitle(player, titleInfo.getId());
            }
        }
    }
    
    /**
     * 关闭管理器时的清理工作
     */
    public void shutdown() {
        plugin.getLogger().info("正在关闭称号管理器，清理所有在线玩家的属性修改器...");

        // 清理所有在线玩家的属性修改器
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            try {
                buffManager.removeTitleBuffs(player);
                // 只在调试模式下显示详细信息
                if (plugin.getConfigManager().getConfigValue("debug", false)) {
                    plugin.getLogger().info("已清理玩家 " + player.getName() + " 的称号属性");
                }
            } catch (Exception e) {
                plugin.getLogger().warning("清理玩家 " + player.getName() + " 属性时出错: " + e.getMessage());
            }
        }

        // 保存数据并清理缓存
        savePlayerData();
        playerDataMap.clear();

        plugin.getLogger().info("称号管理器关闭完成");
    }

    /**
     * 清理玩家的无效称号数据
     * 检查并移除不存在于配置文件中的称号
     *
     * @param player 玩家
     * @return 清理的无效称号数量
     */
    public int cleanupInvalidTitles(Player player) {
        PlayerTitleData playerData = getPlayerData(player);
        Set<String> unlockedTitles = new HashSet<>(playerData.getUnlockedTitles());
        Set<String> invalidTitles = new HashSet<>();
        boolean currentTitleInvalid = false;
        String currentTitle = playerData.getCurrentTitle();

        // 检查已解锁的称号是否仍然存在于配置中
        for (String titleId : unlockedTitles) {
            if (!configManager.titleExists(titleId)) {
                invalidTitles.add(titleId);
            }
        }

        // 检查当前使用的称号是否有效
        if (currentTitle != null && !configManager.titleExists(currentTitle)) {
            currentTitleInvalid = true;
        }

        // 移除无效的称号数据
        if (!invalidTitles.isEmpty() || currentTitleInvalid) {
            boolean debugEnabled = plugin.getConfigManager().getConfigValue("debug", false);

            // 移除无效的已解锁称号
            for (String invalidTitle : invalidTitles) {
                playerData.removeUnlockedTitle(invalidTitle);
                if (debugEnabled) {
                    plugin.getLogger().info("已移除玩家 " + player.getName() + " 的无效称号: " + invalidTitle);
                }
            }

            // 如果当前称号无效，清空当前称号
            if (currentTitleInvalid) {
                playerData.removeCurrentTitle();
                buffManager.removeTitleBuffs(player);
                if (debugEnabled) {
                    plugin.getLogger().info("已清空玩家 " + player.getName() + " 的无效当前称号: " + currentTitle);
                }
            }

            plugin.getLogger().info("已为玩家 " + player.getName() + " 清理了 " +
                                  invalidTitles.size() + " 个无效称号" +
                                  (currentTitleInvalid ? "，并清空了无效的当前称号" : ""));
        }

        return invalidTitles.size() + (currentTitleInvalid ? 1 : 0);
    }

    /**
     * 获取属性管理器
     *
     * @return 属性管理器实例
     */
    public BuffManager getBuffManager() {
        return buffManager;
    }
}
