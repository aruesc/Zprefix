package com.github.chengge.zprefix;

import com.github.chengge.zprefix.command.TitleCommand;
import com.github.chengge.zprefix.gui.TitleGUI;
import com.github.chengge.zprefix.integration.PlaceholderAPIExpansion;
import com.github.chengge.zprefix.integration.SagaLoreStatsIntegration;
import com.github.chengge.zprefix.listener.GUIListener;
import com.github.chengge.zprefix.listener.PlayerListener;
import com.github.chengge.zprefix.listener.VanillaStatsListener;
import com.github.chengge.zprefix.manager.BuffManager;
import com.github.chengge.zprefix.manager.ConfigManager;
import com.github.chengge.zprefix.manager.TitleManager;
import com.github.chengge.zprefix.manager.VanillaStatsManager;
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
            // 第一阶段：初始化核心管理器
            if (!initializeManagers()) {
                throw new RuntimeException("管理器初始化失败");
            }

            // 第二阶段：注册命令
            if (!registerCommands()) {
                throw new RuntimeException("命令注册失败");
            }

            // 第三阶段：注册事件监听器
            if (!registerListeners()) {
                throw new RuntimeException("事件监听器注册失败");
            }

            // 第四阶段：注册PlaceholderAPI扩展（可选）
            registerPlaceholderAPI();

            // 第五阶段：启动自动保存任务
            if (!startAutoSaveTask()) {
                getLogger().warning("自动保存任务启动失败，但不影响插件运行");
            }

            getLogger().info("zPrefix 插件启动成功！");

        } catch (Exception e) {
            getLogger().severe("zPrefix 插件启动失败: " + e.getMessage());
            e.printStackTrace();

            // 确保在失败时进行清理
            try {
                cleanup();
            } catch (Exception cleanupException) {
                getLogger().severe("清理资源时也出错: " + cleanupException.getMessage());
            }

            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("zPrefix 插件正在关闭...");

        try {
            // 第一步：停止定时任务
            getLogger().info("正在停止定时任务...");
            getServer().getScheduler().cancelTasks(this);

            // 第二步：停止统计监听器的定期检查
            if (vanillaStatsListener != null) {
                try {
                    vanillaStatsListener.stopPeriodicCheck();
                    getLogger().info("✓ 统计监听器已停止");
                } catch (Exception e) {
                    getLogger().warning("停止统计监听器时出错: " + e.getMessage());
                }
            }

            // 第三步：保存所有数据
            if (titleManager != null) {
                try {
                    getLogger().info("正在保存玩家数据...");
                    titleManager.shutdown();
                    getLogger().info("✓ 玩家数据保存完成");
                } catch (Exception e) {
                    getLogger().warning("保存玩家数据时出错: " + e.getMessage());
                }
            }

            // 第四步：取消注册PlaceholderAPI扩展
            if (placeholderExpansion != null) {
                try {
                    placeholderExpansion.unregister();
                    getLogger().info("✓ PlaceholderAPI扩展已取消注册");
                } catch (Exception e) {
                    getLogger().warning("取消注册PlaceholderAPI扩展时出错: " + e.getMessage());
                }
            }

            // 第五步：清理资源
            try {
                cleanup();
                getLogger().info("✓ 资源清理完成");
            } catch (Exception e) {
                getLogger().warning("清理资源时出错: " + e.getMessage());
            }

            getLogger().info("zPrefix 插件已安全关闭");

        } catch (Exception e) {
            getLogger().severe("关闭插件时出现严重错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 初始化所有管理器
     *
     * @return 是否初始化成功
     */
    private boolean initializeManagers() {
        getLogger().info("正在初始化管理器...");

        try {
            // 第一步：配置管理器（最重要，其他都依赖它）
            getLogger().info("初始化配置管理器...");
            configManager = new ConfigManager(this);
            configManager.initialize();
            getLogger().info("✓ 配置管理器初始化完成");

            // 第二步：Buff管理器
            getLogger().info("初始化属性管理器...");
            buffManager = new BuffManager(this);
            getLogger().info("✓ 属性管理器初始化完成");

            // 第三步：SagaLoreStats集成（可选，失败不影响主要功能）
            getLogger().info("初始化SagaLoreStats集成...");
            try {
                sagaIntegration = new SagaLoreStatsIntegration(this);
                sagaIntegration.initialize();
                buffManager.setSagaIntegration(sagaIntegration);
                getLogger().info("✓ SagaLoreStats集成初始化完成");
            } catch (Exception e) {
                getLogger().warning("SagaLoreStats集成初始化失败，将使用原版属性系统: " + e.getMessage());
                sagaIntegration = null;
            }

            // 第四步：称号管理器
            getLogger().info("初始化称号管理器...");
            titleManager = new TitleManager(this, configManager, buffManager);
            titleManager.initialize();
            getLogger().info("✓ 称号管理器初始化完成");

            // 第五步：统计管理器
            getLogger().info("初始化统计管理器...");
            vanillaStatsManager = new VanillaStatsManager(this, configManager, titleManager);
            getLogger().info("✓ 统计管理器初始化完成 (使用Minecraft原版统计数据系统)");

            // 第六步：GUI管理器
            getLogger().info("初始化GUI管理器...");
            titleGUI = new TitleGUI(this, configManager, titleManager);
            getLogger().info("✓ GUI管理器初始化完成");

            getLogger().info("所有管理器初始化完成");
            return true;

        } catch (Exception e) {
            getLogger().severe("管理器初始化过程中出错: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 注册命令
     *
     * @return 是否注册成功
     */
    private boolean registerCommands() {
        getLogger().info("正在注册命令...");

        try {
            // 验证必要的管理器是否已初始化
            if (configManager == null || titleManager == null || buffManager == null || titleGUI == null) {
                throw new IllegalStateException("管理器未正确初始化，无法注册命令");
            }

            titleCommand = new TitleCommand(this, configManager, titleManager, buffManager, titleGUI);

            PluginCommand command = getCommand("title");
            if (command != null) {
                command.setExecutor(titleCommand);
                command.setTabCompleter(titleCommand);
                getLogger().info("✓ title 命令注册完成");
            } else {
                throw new RuntimeException("无法获取 title 命令实例，请检查 plugin.yml 配置");
            }

            getLogger().info("命令注册完成");
            return true;

        } catch (Exception e) {
            getLogger().severe("命令注册失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 注册事件监听器
     *
     * @return 是否注册成功
     */
    private boolean registerListeners() {
        getLogger().info("正在注册事件监听器...");

        try {
            // 验证必要的管理器是否已初始化
            if (titleManager == null || titleGUI == null || vanillaStatsManager == null) {
                throw new IllegalStateException("管理器未正确初始化，无法注册事件监听器");
            }

            // 创建监听器实例
            playerListener = new PlayerListener(this, titleManager);
            guiListener = new GUIListener(this, titleGUI);
            vanillaStatsListener = new VanillaStatsListener(this, vanillaStatsManager);

            // 注册监听器
            getServer().getPluginManager().registerEvents(playerListener, this);
            getLogger().info("✓ 玩家事件监听器注册完成");

            getServer().getPluginManager().registerEvents(guiListener, this);
            getLogger().info("✓ GUI事件监听器注册完成");

            getServer().getPluginManager().registerEvents(vanillaStatsListener, this);
            getLogger().info("✓ 统计事件监听器注册完成");

            getLogger().info("事件监听器注册完成");
            return true;

        } catch (Exception e) {
            getLogger().severe("事件监听器注册失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
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
     *
     * @return 是否启动成功
     */
    private boolean startAutoSaveTask() {
        try {
            // 检查配置管理器和称号管理器是否可用
            if (configManager == null || titleManager == null) {
                getLogger().warning("管理器未初始化，跳过自动保存任务");
                return false;
            }

            boolean autoSave = configManager.getConfigValue("defaults.auto-save", true);
            if (!autoSave) {
                getLogger().info("自动保存功能已禁用");
                return true; // 禁用不算失败
            }

            int saveInterval = configManager.getConfigValue("defaults.save-interval", 300);

            // 验证保存间隔的合理性
            if (saveInterval < 60) {
                getLogger().warning("自动保存间隔过短 (" + saveInterval + "秒)，调整为60秒");
                saveInterval = 60;
            }

            getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
                try {
                    if (titleManager != null) {
                        titleManager.savePlayerData();
                        getLogger().info("自动保存玩家数据完成");
                    }
                } catch (Exception e) {
                    getLogger().warning("自动保存时出错: " + e.getMessage());
                }
            }, saveInterval * 20L, saveInterval * 20L);

            getLogger().info("自动保存任务已启动，间隔: " + saveInterval + " 秒");
            return true;

        } catch (Exception e) {
            getLogger().warning("启动自动保存任务失败: " + e.getMessage());
            return false;
        }
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
