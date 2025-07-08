package dyzg.zPrefix.integration;

import dyzg.zPrefix.ZPrefix;
import dyzg.zPrefix.data.PlayerTitleData;
import dyzg.zPrefix.data.TitleInfo;
import dyzg.zPrefix.manager.TitleManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * zPrefix PlaceholderAPI 扩展类
 * 提供称号系统相关的占位符支持
 *
 * 支持的占位符格式：
 * %zprefix_current% - 当前称号显示名称
 * %zprefix_current_raw% - 当前称号ID
 * %zprefix_prefix% - 当前称号前缀（带格式）
 * %zprefix_count% - 已解锁称号数量
 * %zprefix_has_<称号ID>% - 是否拥有指定称号
 * %zprefix_list% - 已解锁称号ID列表
 * %zprefix_list_display% - 已解锁称号显示名称列表
 */
public class PlaceholderAPIExpansion extends PlaceholderExpansion {
    
    private final ZPrefix plugin;
    private final TitleManager titleManager;

    public PlaceholderAPIExpansion(ZPrefix plugin) {
        this.plugin = plugin;
        this.titleManager = plugin.getTitleManager();
    }
    
    @Override
    public @NotNull String getIdentifier() {
        return "zprefix";
    }
    
    @Override
    public @NotNull String getAuthor() {
        return "dyzg";
    }
    
    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }
    
    @Override
    public boolean persist() {
        return true; // 持久化扩展，不会被卸载
    }
    
    @Override
    public boolean canRegister() {
        return true; // 允许注册
    }
    
    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }
        
        // 获取玩家称号数据
        PlayerTitleData playerData = titleManager.getPlayerData(player);
        
        // 解析占位符参数
        String[] args = params.toLowerCase().split("_");
        String mainParam = args[0];
        
        switch (mainParam) {
            case "current":
                return handleCurrentTitle(playerData, args);

            case "prefix":
                return handleTitlePrefix(playerData);

            case "count":
                return handleTitleCount(playerData);

            case "has":
                return handleHasTitle(playerData, args);

            case "list":
                return handleTitleList(playerData, args);

            default:
                return "";
        }
    }
    
    /**
     * 处理当前称号相关占位符
     * %zprefix_current% - 当前称号显示名称
     * %zprefix_current_raw% - 当前称号ID
     */
    private String handleCurrentTitle(PlayerTitleData playerData, String[] args) {
        String currentTitleId = playerData.getCurrentTitle();
        if (currentTitleId == null || currentTitleId.isEmpty()) {
            return "";
        }
        
        TitleInfo titleInfo = plugin.getConfigManager().getTitleInfo(currentTitleId);
        if (titleInfo == null) {
            return "";
        }
        
        // 检查是否要求原始ID
        if (args.length > 1 && "raw".equals(args[1])) {
            return currentTitleId;
        }
        
        return titleInfo.getDisplayName();
    }
    
    /**
     * 处理称号前缀占位符
     * %zprefix_prefix% - 当前称号前缀（带格式）
     */
    private String handleTitlePrefix(PlayerTitleData playerData) {
        String currentTitleId = playerData.getCurrentTitle();
        if (currentTitleId == null || currentTitleId.isEmpty()) {
            return "";
        }

        TitleInfo titleInfo = plugin.getConfigManager().getTitleInfo(currentTitleId);
        if (titleInfo == null) {
            return "";
        }

        // 使用默认的前缀格式
        return "§7[" + titleInfo.getDisplayName() + "§7] ";
    }
    
    /**
     * 处理称号数量占位符
     * %zprefix_count% - 已解锁称号数量
     */
    private String handleTitleCount(PlayerTitleData playerData) {
        return String.valueOf(playerData.getUnlockedTitleCount());
    }
    
    /**
     * 处理是否拥有称号占位符
     * %zprefix_has_<称号ID>% - 是否拥有指定称号
     */
    private String handleHasTitle(PlayerTitleData playerData, String[] args) {
        if (args.length < 2) {
            return "false";
        }
        
        String titleId = args[1];
        boolean hasTitle = playerData.hasUnlockedTitle(titleId);
        return hasTitle ? "true" : "false";
    }
    

    
    /**
     * 处理称号列表占位符
     * %zprefix_list% - 已解锁称号列表（逗号分隔）
     * %zprefix_list_display% - 已解锁称号显示名称列表
     */
    private String handleTitleList(PlayerTitleData playerData, String[] args) {
        Set<String> unlockedTitles = playerData.getUnlockedTitles();
        if (unlockedTitles.isEmpty()) {
            return "";
        }
        
        boolean useDisplayName = args.length > 1 && "display".equals(args[1]);
        
        StringBuilder result = new StringBuilder();
        boolean first = true;
        
        for (String titleId : unlockedTitles) {
            if (!first) {
                result.append(", ");
            }
            
            if (useDisplayName) {
                TitleInfo titleInfo = plugin.getConfigManager().getTitleInfo(titleId);
                if (titleInfo != null) {
                    result.append(titleInfo.getDisplayName());
                } else {
                    result.append(titleId);
                }
            } else {
                result.append(titleId);
            }
            
            first = false;
        }
        
        return result.toString();
    }
}
