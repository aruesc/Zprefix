package dyzg.zPrefix;

import dyzg.zPrefix.command.TitleCommand;
import dyzg.zPrefix.gui.TitleGUI;
import dyzg.zPrefix.integration.PlaceholderAPIExpansion;
import dyzg.zPrefix.integration.SagaLoreStatsIntegration;
import dyzg.zPrefix.listener.GUIListener;
import dyzg.zPrefix.listener.PlayerListener;
import dyzg.zPrefix.listener.VanillaStatsListener;
import dyzg.zPrefix.manager.BuffManager;
import dyzg.zPrefix.manager.ConfigManager;
import dyzg.zPrefix.manager.TitleManager;
import dyzg.zPrefix.manager.VanillaStatsManager;
import dyzg.zPrefix.util.MessageUtil;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * zPrefix 插件主类
 * 玩家称号系统插件，支持称号切换和属性加成
 *
 * @author dyzg
 * @version 1.0
 */
public final class ZPrefix extends JavaPlugin {

    // 管理器实例
    private ConfigManager configManager;
    private BuffManager buffManager;
    private TitleManager titleManager;
    private VanillaStatsManager vanillaStatsManager;
    private TitleGUI titleGUI;
    private SagaLoreStatsIntegration sagaIntegration;
    private PlaceholderAPIExpansion placeholderExpansion;

    // 监听器实例
    private PlayerListener playerListener;
    private GUIListener guiListener;
    private VanillaStatsListener vanillaStatsListener;

    // 命令处理器实例
    private TitleCommand titleCommand;

    @Override
    public void onEnable() {
        getLogger().info("zPrefix 插件正在启动...");

        try {
            // 初始化管理器
            initializeManagers();

            // 注册命令
            registerCommands();

            // 注册事件监听器
            registerListeners();

            // 注册PlaceholderAPI扩展
            registerPlaceholderAPI();

            // 启动自动保存任务
            startAutoSaveTask();

            getLogger().info("zPrefix 插件启动成功！");

        } catch (Exception e) {
            getLogger().severe("zPrefix 插件启动失败: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("zPrefix 插件正在关闭...");

        try {
            // 保存数据
            if (titleManager != null) {
                titleManager.shutdown();
            }

            if (vanillaStatsListener != null) {
                vanillaStatsListener.stopPeriodicCheck();
            }

            // 取消注册PlaceholderAPI扩展
            if (placeholderExpansion != null) {
                placeholderExpansion.unregister();
            }

            // 清理资源
            cleanup();

            getLogger().info("zPrefix 插件已安全关闭");

        } catch (Exception e) {
            getLogger().warning("关闭插件时出错: " + e.getMessage());
        }
    }

    /**
     * 初始化所有管理器
     */
    private void initializeManagers() {
        getLogger().info("正在初始化管理器...");

        // 配置管理器
        configManager = new ConfigManager(this);
        configManager.initialize();

        // Buff管理器
        buffManager = new BuffManager(this);

        // SagaLoreStats集成
        sagaIntegration = new SagaLoreStatsIntegration(this);
        sagaIntegration.initialize();
        buffManager.setSagaIntegration(sagaIntegration);

        // 称号管理器
        titleManager = new TitleManager(this, configManager, buffManager);
        titleManager.initialize();

        // 原版统计管理器
        vanillaStatsManager = new VanillaStatsManager(this, configManager, titleManager);
        getLogger().info("✓ 使用Minecraft原版统计数据系统");

        // GUI管理器
        titleGUI = new TitleGUI(this, configManager, titleManager);

        getLogger().info("管理器初始化完成");
    }

    /**
     * 注册命令
     */
    private void registerCommands() {
        getLogger().info("正在注册命令...");

        titleCommand = new TitleCommand(this, configManager, titleManager, buffManager, titleGUI);

        PluginCommand command = getCommand("title");
        if (command != null) {
            command.setExecutor(titleCommand);
            command.setTabCompleter(titleCommand);
        } else {
            getLogger().warning("无法注册 title 命令！");
        }

        getLogger().info("命令注册完成");
    }

    /**
     * 注册事件监听器
     */
    private void registerListeners() {
        getLogger().info("正在注册事件监听器...");

        playerListener = new PlayerListener(this, titleManager);
        guiListener = new GUIListener(this, titleGUI);
        vanillaStatsListener = new VanillaStatsListener(this, vanillaStatsManager);

        getServer().getPluginManager().registerEvents(playerListener, this);
        getServer().getPluginManager().registerEvents(guiListener, this);
        getServer().getPluginManager().registerEvents(vanillaStatsListener, this);

        getLogger().info("事件监听器注册完成");
    }

    /**
     * 注册PlaceholderAPI扩展
     */
    private void registerPlaceholderAPI() {
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderExpansion = new PlaceholderAPIExpansion(this);
            if (placeholderExpansion.register()) {
                getLogger().info("成功注册 PlaceholderAPI 扩展！");
            } else {
                getLogger().warning("注册 PlaceholderAPI 扩展失败！");
            }
        } else {
            getLogger().info("未检测到 PlaceholderAPI 插件，跳过占位符注册");
        }
    }

    /**
     * 启动自动保存任务
     */
    private void startAutoSaveTask() {
        boolean autoSave = configManager.getConfigValue("defaults.auto-save", true);
        if (!autoSave) {
            return;
        }

        int saveInterval = configManager.getConfigValue("defaults.save-interval", 300);

        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            try {
                titleManager.savePlayerData();
                getLogger().info("自动保存玩家数据完成");
            } catch (Exception e) {
                getLogger().warning("自动保存时出错: " + e.getMessage());
            }
        }, saveInterval * 20L, saveInterval * 20L);

        getLogger().info("自动保存任务已启动，间隔: " + saveInterval + " 秒");
    }

    /**
     * 清理资源
     */
    private void cleanup() {
        // 清理Buff管理器
        if (buffManager != null) {
            buffManager.clearAllData();
        }

        // 取消所有任务
        getServer().getScheduler().cancelTasks(this);
    }

    // Getter 方法，供其他类访问管理器实例

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public BuffManager getBuffManager() {
        return buffManager;
    }

    public TitleManager getTitleManager() {
        return titleManager;
    }

    public VanillaStatsManager getVanillaStatsManager() {
        return vanillaStatsManager;
    }

    public TitleGUI getTitleGUI() {
        return titleGUI;
    }

    public SagaLoreStatsIntegration getSagaIntegration() {
        return sagaIntegration;
    }
}
