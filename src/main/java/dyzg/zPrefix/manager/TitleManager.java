package dyzg.zPrefix.manager;

import dyzg.zPrefix.ZPrefix;
import dyzg.zPrefix.data.PlayerTitleData;
import dyzg.zPrefix.data.TitleInfo;
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
     *
     * @param player 玩家
     */
    public void onPlayerJoin(Player player) {
        PlayerTitleData playerData = getPlayerData(player);

        // 首先强制清理所有可能的遗留属性修改器
        buffManager.forceCleanupPlayerAttributes(player);

        // 检查是否需要给予默认称号
        if (!playerData.hasAnyUnlockedTitle()) {
            giveAutoUnlockTitles(player);
        }

        // 应用当前称号的属性加成
        String currentTitle = playerData.getCurrentTitle();
        if (currentTitle != null) {
            TitleInfo titleInfo = configManager.getTitleInfo(currentTitle);
            if (titleInfo != null) {
                buffManager.applyTitleBuffs(player, titleInfo);
            }
        }
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
