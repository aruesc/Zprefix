package dyzg.zPrefix.manager;

import dyzg.zPrefix.ZPrefix;
import dyzg.zPrefix.data.TitleInfo;
import dyzg.zPrefix.util.MessageUtil;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

/**
 * 配置管理器
 * 统一管理所有配置文件的加载和访问
 */
public class ConfigManager {
    
    private final ZPrefix plugin;
    private FileConfiguration config;
    private FileConfiguration titlesConfig;
    private final Map<String, TitleInfo> titleInfoMap = new HashMap<>();
    
    public ConfigManager(ZPrefix plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 初始化配置管理器
     */
    public void initialize() {
        loadConfigs();
        loadTitles();
    }
    
    /**
     * 加载配置文件
     */
    public void loadConfigs() {
        // 保存默认配置文件
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
        
        // 加载称号配置文件
        File titlesFile = new File(plugin.getDataFolder(), "titles.yml");
        if (!titlesFile.exists()) {
            plugin.saveResource("titles.yml", false);
        }
        titlesConfig = YamlConfiguration.loadConfiguration(titlesFile);
        
        // 初始化消息工具
        MessageUtil.initialize(plugin);
    }
    
    /**
     * 重新加载所有配置
     */
    public void reloadConfigs() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        
        File titlesFile = new File(plugin.getDataFolder(), "titles.yml");
        titlesConfig = YamlConfiguration.loadConfiguration(titlesFile);
        
        MessageUtil.loadMessages();
        loadTitles();
    }
    
    /**
     * 加载称号配置
     */
    private void loadTitles() {
        titleInfoMap.clear();
        
        ConfigurationSection titlesSection = titlesConfig.getConfigurationSection("titles");
        if (titlesSection == null) {
            plugin.getLogger().warning("称号配置文件中没有找到 'titles' 节点！");
            return;
        }
        
        for (String titleId : titlesSection.getKeys(false)) {
            try {
                TitleInfo titleInfo = loadTitleInfo(titleId, titlesSection.getConfigurationSection(titleId));
                if (titleInfo != null) {
                    titleInfoMap.put(titleId, titleInfo);
                    plugin.getLogger().info("成功加载称号: " + titleId);
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "加载称号 " + titleId + " 时出错", e);
            }
        }
        
        plugin.getLogger().info("总共加载了 " + titleInfoMap.size() + " 个称号");
    }
    
    /**
     * 加载单个称号信息
     * 
     * @param titleId 称号ID
     * @param section 配置节点
     * @return 称号信息对象
     */
    private TitleInfo loadTitleInfo(String titleId, ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        
        // 基本信息
        String displayName = MessageUtil.colorize(section.getString("display-name", titleId));

        // GUI物品
        ItemStack guiItem = createGuiItem(section.getConfigurationSection("item"));
        
        // 原生属性加成
        Map<Attribute, Double> attributes = loadAttributes(section.getConfigurationSection("attributes"));

        // SagaLoreStats属性加成
        Map<String, Object> sagaAttributes = loadSagaLoreStatsAttributes(section.getConfigurationSection("saga-attributes"));

        // 解锁条件
        Map<String, Object> unlockConditions = loadUnlockConditions(section.getConfigurationSection("unlock-conditions"));

        // 是否默认称号
        boolean isDefault = section.getBoolean("default", false);

        return new TitleInfo(titleId, displayName, guiItem, attributes, sagaAttributes, unlockConditions, isDefault);
    }
    
    /**
     * 创建GUI物品
     * 
     * @param section 物品配置节点
     * @return GUI物品
     */
    private ItemStack createGuiItem(ConfigurationSection section) {
        if (section == null) {
            return new ItemStack(Material.PAPER);
        }
        
        String materialName = section.getString("material", "PAPER");
        Material material;
        try {
            material = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("无效的材料类型: " + materialName + "，使用默认材料 PAPER");
            material = Material.PAPER;
        }
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // 设置名称
            String name = section.getString("name");
            if (name != null) {
                meta.setDisplayName(MessageUtil.colorize(name));
            }
            
            // 设置描述
            List<String> lore = section.getStringList("lore");
            if (!lore.isEmpty()) {
                List<String> coloredLore = new ArrayList<>();
                for (String line : lore) {
                    coloredLore.add(MessageUtil.colorize(line));
                }
                meta.setLore(coloredLore);
            }
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * 加载属性配置
     * 
     * @param section 属性配置节点
     * @return 属性映射
     */
    private Map<Attribute, Double> loadAttributes(ConfigurationSection section) {
        Map<Attribute, Double> attributes = new HashMap<>();
        
        if (section != null) {
            for (String attributeName : section.getKeys(false)) {
                try {
                    Attribute attribute = Attribute.valueOf(attributeName.toUpperCase());
                    double value = section.getDouble(attributeName);
                    attributes.put(attribute, value);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("无效的属性类型: " + attributeName);
                }
            }
        }
        
        return attributes;
    }
    
    /**
     * 加载SagaLoreStats属性配置
     *
     * @param section SagaLoreStats属性配置节点
     * @return SagaLoreStats属性映射
     */
    private Map<String, Object> loadSagaLoreStatsAttributes(ConfigurationSection section) {
        Map<String, Object> sagaAttributes = new HashMap<>();

        if (section != null) {
            for (String attributeName : section.getKeys(false)) {
                Object value = section.get(attributeName);
                if (value instanceof Number) {
                    sagaAttributes.put(attributeName, ((Number) value).doubleValue());
                } else if (value instanceof String) {
                    try {
                        double doubleValue = Double.parseDouble((String) value);
                        sagaAttributes.put(attributeName, doubleValue);
                    } catch (NumberFormatException e) {
                        plugin.getLogger().warning("无效的SagaLoreStats属性值: " + attributeName + " = " + value);
                    }
                }
            }
        }

        return sagaAttributes;
    }

    /**
     * 加载解锁条件
     *
     * @param section 解锁条件配置节点
     * @return 解锁条件映射
     */
    private Map<String, Object> loadUnlockConditions(ConfigurationSection section) {
        Map<String, Object> conditions = new HashMap<>();
        
        if (section != null) {
            for (String key : section.getKeys(false)) {
                conditions.put(key, section.get(key));
            }
        }
        
        return conditions;
    }
    
    /**
     * 获取所有称号信息
     * 
     * @return 称号信息映射
     */
    public Map<String, TitleInfo> getAllTitles() {
        return new HashMap<>(titleInfoMap);
    }
    
    /**
     * 获取指定称号信息
     * 
     * @param titleId 称号ID
     * @return 称号信息，如果不存在则返回null
     */
    public TitleInfo getTitleInfo(String titleId) {
        return titleInfoMap.get(titleId);
    }
    
    /**
     * 检查称号是否存在
     * 
     * @param titleId 称号ID
     * @return 是否存在
     */
    public boolean titleExists(String titleId) {
        return titleInfoMap.containsKey(titleId);
    }
    
    /**
     * 获取默认称号
     * 
     * @return 默认称号ID，如果没有则返回null
     */
    public String getDefaultTitle() {
        for (TitleInfo titleInfo : titleInfoMap.values()) {
            if (titleInfo.isDefault()) {
                return titleInfo.getId();
            }
        }
        return null;
    }
    
    /**
     * 获取主配置
     * 
     * @return 主配置对象
     */
    public FileConfiguration getConfig() {
        return config;
    }
    
    /**
     * 获取称号配置
     * 
     * @return 称号配置对象
     */
    public FileConfiguration getTitlesConfig() {
        return titlesConfig;
    }
    
    /**
     * 获取配置值
     * 
     * @param path 配置路径
     * @param defaultValue 默认值
     * @return 配置值
     */
    public <T> T getConfigValue(String path, T defaultValue) {
        return (T) config.get(path, defaultValue);
    }
}
