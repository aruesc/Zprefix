package com.github.chengge.zprefix.util;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 材质适配器
 * 处理不同Paper版本间的材质名称差异
 * 提供跨版本的材质兼容性支持
 */
public class MaterialAdapter {
    
    private static final Logger logger = Logger.getLogger(MaterialAdapter.class.getName());
    
    // 材质名称映射 - 旧版本 -> 新版本
    private static final Map<String, String> MATERIAL_MAPPING = new HashMap<>();
    
    // 缓存已解析的材质
    private static final Map<String, Material> materialCache = new HashMap<>();
    
    static {
        initializeMaterialMapping();
    }
    
    /**
     * 初始化材质名称映射
     */
    private static void initializeMaterialMapping() {
        // 1.13+ 材质名称变化
        MATERIAL_MAPPING.put("STAINED_GLASS_PANE", "GRAY_STAINED_GLASS_PANE");
        MATERIAL_MAPPING.put("WOOL", "WHITE_WOOL");
        MATERIAL_MAPPING.put("STAINED_CLAY", "TERRACOTTA");
        MATERIAL_MAPPING.put("HARD_CLAY", "TERRACOTTA");
        MATERIAL_MAPPING.put("WORKBENCH", "CRAFTING_TABLE");
        MATERIAL_MAPPING.put("WOOD_DOOR", "OAK_DOOR");
        MATERIAL_MAPPING.put("IRON_FENCE", "IRON_BARS");
        MATERIAL_MAPPING.put("THIN_GLASS", "GLASS_PANE");
        MATERIAL_MAPPING.put("STONE_PLATE", "STONE_PRESSURE_PLATE");
        MATERIAL_MAPPING.put("IRON_PLATE", "HEAVY_WEIGHTED_PRESSURE_PLATE");
        MATERIAL_MAPPING.put("GOLD_PLATE", "LIGHT_WEIGHTED_PRESSURE_PLATE");
        MATERIAL_MAPPING.put("WOOD_PLATE", "OAK_PRESSURE_PLATE");
        
        // 1.14+ 材质名称变化
        MATERIAL_MAPPING.put("SIGN", "OAK_SIGN");
        MATERIAL_MAPPING.put("WALL_SIGN", "OAK_WALL_SIGN");
        MATERIAL_MAPPING.put("BOAT", "OAK_BOAT");
        
        // 1.15+ 材质名称变化
        MATERIAL_MAPPING.put("BEE_NEST", "BEE_NEST");
        
        // 1.16+ 材质名称变化
        MATERIAL_MAPPING.put("ZOMBIE_PIGMAN_SPAWN_EGG", "ZOMBIFIED_PIGLIN_SPAWN_EGG");
        
        // 1.17+ 材质名称变化
        MATERIAL_MAPPING.put("GRASS_PATH", "DIRT_PATH");
        
        // 常见的GUI材质映射
        MATERIAL_MAPPING.put("SKULL_ITEM", "PLAYER_HEAD");
        MATERIAL_MAPPING.put("SKULL", "PLAYER_HEAD");
        MATERIAL_MAPPING.put("GOLDEN_APPLE", "GOLDEN_APPLE");
        MATERIAL_MAPPING.put("ENCHANTED_GOLDEN_APPLE", "ENCHANTED_GOLDEN_APPLE");
        
        logger.info("初始化了 " + MATERIAL_MAPPING.size() + " 个材质名称映射");
    }
    
    /**
     * 获取材质，支持跨版本兼容
     * 
     * @param materialName 材质名称
     * @return 材质对象，如果找不到则返回null
     */
    public static Material getMaterial(String materialName) {
        if (materialName == null || materialName.trim().isEmpty()) {
            return null;
        }
        
        String upperName = materialName.toUpperCase().trim();
        
        // 检查缓存
        if (materialCache.containsKey(upperName)) {
            return materialCache.get(upperName);
        }
        
        Material material = findMaterial(upperName);
        
        // 缓存结果（包括null）
        materialCache.put(upperName, material);
        
        return material;
    }
    
    /**
     * 查找材质的核心逻辑
     * 
     * @param materialName 材质名称（已转换为大写）
     * @return 材质对象，如果找不到则返回null
     */
    private static Material findMaterial(String materialName) {
        try {
            // 1. 尝试直接获取
            return Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            // 2. 尝试使用映射
            String mappedName = MATERIAL_MAPPING.get(materialName);
            if (mappedName != null) {
                try {
                    return Material.valueOf(mappedName);
                } catch (IllegalArgumentException e2) {
                    // 映射的材质也不存在
                }
            }
            
            // 3. 尝试常见的前缀/后缀变体
            Material material = tryCommonVariants(materialName);
            if (material != null) {
                return material;
            }
            
            // 4. 记录警告并返回null
            logger.warning("无法找到材质: " + materialName + 
                          (mappedName != null ? " (尝试映射: " + mappedName + ")" : ""));
            return null;
        }
    }
    
    /**
     * 尝试常见的材质名称变体
     * 
     * @param materialName 原始材质名称
     * @return 找到的材质，如果没有找到则返回null
     */
    private static Material tryCommonVariants(String materialName) {
        // 尝试添加常见前缀
        String[] prefixes = {"WHITE_", "OAK_", "STONE_", "IRON_"};
        for (String prefix : prefixes) {
            if (!materialName.startsWith(prefix)) {
                try {
                    return Material.valueOf(prefix + materialName);
                } catch (IllegalArgumentException e) {
                    // 继续尝试下一个
                }
            }
        }
        
        // 尝试移除常见前缀
        for (String prefix : prefixes) {
            if (materialName.startsWith(prefix)) {
                try {
                    return Material.valueOf(materialName.substring(prefix.length()));
                } catch (IllegalArgumentException e) {
                    // 继续尝试下一个
                }
            }
        }
        
        // 尝试添加常见后缀
        String[] suffixes = {"_BLOCK", "_ITEM", "_ORE"};
        for (String suffix : suffixes) {
            if (!materialName.endsWith(suffix)) {
                try {
                    return Material.valueOf(materialName + suffix);
                } catch (IllegalArgumentException e) {
                    // 继续尝试下一个
                }
            }
        }
        
        return null;
    }
    
    /**
     * 获取材质，如果找不到则返回默认材质
     * 
     * @param materialName 材质名称
     * @param defaultMaterial 默认材质
     * @return 材质对象
     */
    public static Material getMaterialOrDefault(String materialName, Material defaultMaterial) {
        Material material = getMaterial(materialName);
        return material != null ? material : defaultMaterial;
    }
    
    /**
     * 检查材质是否存在
     * 
     * @param materialName 材质名称
     * @return 是否存在
     */
    public static boolean isMaterialAvailable(String materialName) {
        return getMaterial(materialName) != null;
    }
    
    /**
     * 获取所有可用的材质映射
     * 
     * @return 材质映射的副本
     */
    public static Map<String, String> getAllMappings() {
        return new HashMap<>(MATERIAL_MAPPING);
    }
    
    /**
     * 清理缓存（用于测试或重新加载）
     */
    public static void clearCache() {
        materialCache.clear();
        logger.info("材质缓存已清理");
    }
}
