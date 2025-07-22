package com.github.chengge.zprefix.integration;

import com.github.chengge.zprefix.ZPrefix;
import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.logging.Level;

/**
 * 经济系统集成
 * 统一管理Vault和PlayerPoints的集成
 */
public class EconomyIntegration {
    
    private final ZPrefix plugin;
    private Economy vaultEconomy;
    private PlayerPointsAPI playerPointsAPI;
    private boolean vaultEnabled = false;
    private boolean playerPointsEnabled = false;
    
    public EconomyIntegration(ZPrefix plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 初始化经济系统集成
     */
    public void initialize() {
        initializeVault();
        initializePlayerPoints();
        
        if (!vaultEnabled && !playerPointsEnabled) {
            plugin.getLogger().info("未检测到经济系统插件，购买功能将被禁用");
        } else {
            plugin.getLogger().info("经济系统集成状态 - Vault: " + (vaultEnabled ? "启用" : "禁用") + 
                                  ", PlayerPoints: " + (playerPointsEnabled ? "启用" : "禁用"));
        }
    }
    
    /**
     * 初始化Vault经济系统
     */
    private void initializeVault() {
        try {
            if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
                RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
                if (rsp != null) {
                    vaultEconomy = rsp.getProvider();
                    vaultEnabled = true;
                    plugin.getLogger().info("成功集成 Vault 经济系统！");
                } else {
                    plugin.getLogger().warning("检测到 Vault 插件，但未找到经济系统提供者");
                }
            } else {
                plugin.getLogger().info("未检测到 Vault 插件");
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "初始化 Vault 经济系统时出错", e);
        }
    }
    
    /**
     * 初始化PlayerPoints系统
     */
    private void initializePlayerPoints() {
        try {
            if (Bukkit.getPluginManager().getPlugin("PlayerPoints") != null) {
                PlayerPoints playerPointsPlugin = (PlayerPoints) Bukkit.getPluginManager().getPlugin("PlayerPoints");
                if (playerPointsPlugin != null) {
                    playerPointsAPI = playerPointsPlugin.getAPI();
                    playerPointsEnabled = true;
                    plugin.getLogger().info("成功集成 PlayerPoints 系统！");
                } else {
                    plugin.getLogger().warning("检测到 PlayerPoints 插件，但获取API失败");
                }
            } else {
                plugin.getLogger().info("未检测到 PlayerPoints 插件");
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "初始化 PlayerPoints 系统时出错", e);
        }
    }
    
    /**
     * 检查是否有可用的经济系统
     * 
     * @return 是否有可用的经济系统
     */
    public boolean hasEconomySystem() {
        return vaultEnabled || playerPointsEnabled;
    }
    
    /**
     * 检查Vault经济系统是否可用
     * 
     * @return 是否可用
     */
    public boolean isVaultEnabled() {
        return vaultEnabled && vaultEconomy != null;
    }
    
    /**
     * 检查PlayerPoints系统是否可用
     * 
     * @return 是否可用
     */
    public boolean isPlayerPointsEnabled() {
        return playerPointsEnabled && playerPointsAPI != null;
    }
    
    /**
     * 获取玩家的金币余额
     * 
     * @param player 玩家
     * @return 金币余额
     */
    public double getPlayerMoney(Player player) {
        if (!isVaultEnabled()) {
            return 0.0;
        }
        
        try {
            return vaultEconomy.getBalance(player);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "获取玩家 " + player.getName() + " 金币余额时出错", e);
            return 0.0;
        }
    }
    
    /**
     * 获取玩家的点券余额
     * 
     * @param player 玩家
     * @return 点券余额
     */
    public int getPlayerPoints(Player player) {
        if (!isPlayerPointsEnabled()) {
            return 0;
        }
        
        try {
            return playerPointsAPI.look(player.getUniqueId());
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "获取玩家 " + player.getName() + " 点券余额时出错", e);
            return 0;
        }
    }
    
    /**
     * 扣除玩家金币
     * 
     * @param player 玩家
     * @param amount 金额
     * @return 是否成功
     */
    public boolean takeMoney(Player player, double amount) {
        if (!isVaultEnabled() || amount <= 0) {
            return false;
        }
        
        try {
            if (vaultEconomy.getBalance(player) >= amount) {
                return vaultEconomy.withdrawPlayer(player, amount).transactionSuccess();
            }
            return false;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "扣除玩家 " + player.getName() + " 金币时出错", e);
            return false;
        }
    }
    
    /**
     * 扣除玩家点券
     * 
     * @param player 玩家
     * @param amount 点券数量
     * @return 是否成功
     */
    public boolean takePoints(Player player, int amount) {
        if (!isPlayerPointsEnabled() || amount <= 0) {
            return false;
        }
        
        try {
            if (playerPointsAPI.look(player.getUniqueId()) >= amount) {
                return playerPointsAPI.take(player.getUniqueId(), amount);
            }
            return false;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "扣除玩家 " + player.getName() + " 点券时出错", e);
            return false;
        }
    }
    
    /**
     * 格式化金币显示
     * 
     * @param amount 金额
     * @return 格式化后的字符串
     */
    public String formatMoney(double amount) {
        if (isVaultEnabled()) {
            try {
                return vaultEconomy.format(amount);
            } catch (Exception e) {
                return String.format("%.2f", amount);
            }
        }
        return String.format("%.2f", amount);
    }
    
    /**
     * 格式化点券显示
     * 
     * @param amount 点券数量
     * @return 格式化后的字符串
     */
    public String formatPoints(int amount) {
        return String.valueOf(amount) + " 点券";
    }
}
