package com.github.chengge.zprefix.util;

import org.bukkit.attribute.Attribute;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;

import java.util.*;
import java.util.logging.Logger;

/**
 * 属性适配器
 * 提供跨版本的属性兼容性支持，避免硬编码属性类型
 * 支持Paper 1.20.1和1.21.7+的属性枚举差异
 */
public class AttributeAdapter {
    
    private static final Logger logger = Logger.getLogger(AttributeAdapter.class.getName());
    
    // 属性名称映射 - 1.20.1 -> 1.21.7+
    private static final Map<String, String> ATTRIBUTE_NAME_MAPPING = new HashMap<>();
    
    // 缓存所有可用的属性
    private static Set<Attribute> availableAttributes = null;
    
    // 缓存属性名称到属性对象的映射
    private static Map<String, Attribute> attributeByName = null;
    
    static {
        try {
            initializeAttributeMapping();
        } catch (Exception e) {
            logger.severe("AttributeAdapter静态初始化失败: " + e.getMessage());
            e.printStackTrace();
            // 不抛出异常，允许插件继续启动
        }
    }
    
    /**
     * 初始化属性名称映射
     * 支持1.20.1到1.21.7+的所有版本转换
     */
    private static void initializeAttributeMapping() {
        // 1.20.1 -> 1.21+ 属性名称映射（移除GENERIC_前缀）
        ATTRIBUTE_NAME_MAPPING.put("GENERIC_MAX_HEALTH", "MAX_HEALTH");
        ATTRIBUTE_NAME_MAPPING.put("GENERIC_FOLLOW_RANGE", "FOLLOW_RANGE");
        ATTRIBUTE_NAME_MAPPING.put("GENERIC_KNOCKBACK_RESISTANCE", "KNOCKBACK_RESISTANCE");
        ATTRIBUTE_NAME_MAPPING.put("GENERIC_MOVEMENT_SPEED", "MOVEMENT_SPEED");
        ATTRIBUTE_NAME_MAPPING.put("GENERIC_FLYING_SPEED", "FLYING_SPEED");
        ATTRIBUTE_NAME_MAPPING.put("GENERIC_ATTACK_DAMAGE", "ATTACK_DAMAGE");
        ATTRIBUTE_NAME_MAPPING.put("GENERIC_ATTACK_KNOCKBACK", "ATTACK_KNOCKBACK");
        ATTRIBUTE_NAME_MAPPING.put("GENERIC_ATTACK_SPEED", "ATTACK_SPEED");
        ATTRIBUTE_NAME_MAPPING.put("GENERIC_ARMOR", "ARMOR");
        ATTRIBUTE_NAME_MAPPING.put("GENERIC_ARMOR_TOUGHNESS", "ARMOR_TOUGHNESS");
        ATTRIBUTE_NAME_MAPPING.put("GENERIC_LUCK", "LUCK");

        // 1.21.4+ 新增属性（完整列表）
        ATTRIBUTE_NAME_MAPPING.put("GENERIC_SCALE", "SCALE");
        ATTRIBUTE_NAME_MAPPING.put("GENERIC_STEP_HEIGHT", "STEP_HEIGHT");
        ATTRIBUTE_NAME_MAPPING.put("GENERIC_GRAVITY", "GRAVITY");
        ATTRIBUTE_NAME_MAPPING.put("GENERIC_SAFE_FALL_DISTANCE", "SAFE_FALL_DISTANCE");
        ATTRIBUTE_NAME_MAPPING.put("GENERIC_FALL_DAMAGE_MULTIPLIER", "FALL_DAMAGE_MULTIPLIER");
        ATTRIBUTE_NAME_MAPPING.put("GENERIC_JUMP_STRENGTH", "JUMP_STRENGTH");
        ATTRIBUTE_NAME_MAPPING.put("GENERIC_BURNING_TIME", "BURNING_TIME");
        ATTRIBUTE_NAME_MAPPING.put("GENERIC_EXPLOSION_KNOCKBACK_RESISTANCE", "EXPLOSION_KNOCKBACK_RESISTANCE");
        ATTRIBUTE_NAME_MAPPING.put("GENERIC_MOVEMENT_EFFICIENCY", "MOVEMENT_EFFICIENCY");
        ATTRIBUTE_NAME_MAPPING.put("GENERIC_OXYGEN_BONUS", "OXYGEN_BONUS");
        ATTRIBUTE_NAME_MAPPING.put("GENERIC_WATER_MOVEMENT_EFFICIENCY", "WATER_MOVEMENT_EFFICIENCY");

        // 1.21.4+ 额外的新属性
        ATTRIBUTE_NAME_MAPPING.put("GENERIC_TEMPT_RANGE", "TEMPT_RANGE");
        ATTRIBUTE_NAME_MAPPING.put("GENERIC_BLOCK_INTERACTION_RANGE", "BLOCK_INTERACTION_RANGE");
        ATTRIBUTE_NAME_MAPPING.put("GENERIC_ENTITY_INTERACTION_RANGE", "ENTITY_INTERACTION_RANGE");
        ATTRIBUTE_NAME_MAPPING.put("GENERIC_BLOCK_BREAK_SPEED", "BLOCK_BREAK_SPEED");
        ATTRIBUTE_NAME_MAPPING.put("GENERIC_MINING_EFFICIENCY", "MINING_EFFICIENCY");
        ATTRIBUTE_NAME_MAPPING.put("GENERIC_SNEAKING_SPEED", "SNEAKING_SPEED");
        ATTRIBUTE_NAME_MAPPING.put("GENERIC_SUBMERGED_MINING_SPEED", "SUBMERGED_MINING_SPEED");
        ATTRIBUTE_NAME_MAPPING.put("GENERIC_SWEEPING_DAMAGE_RATIO", "SWEEPING_DAMAGE_RATIO");

        // 特殊实体属性映射
        ATTRIBUTE_NAME_MAPPING.put("HORSE_JUMP_STRENGTH", "JUMP_STRENGTH");
        ATTRIBUTE_NAME_MAPPING.put("ZOMBIE_SPAWN_REINFORCEMENTS", "SPAWN_REINFORCEMENTS");

        // 1.21.7+ 可能的新属性（预留）
        ATTRIBUTE_NAME_MAPPING.put("GENERIC_BLOCK_INTERACTION_RANGE", "BLOCK_INTERACTION_RANGE");
        ATTRIBUTE_NAME_MAPPING.put("GENERIC_ENTITY_INTERACTION_RANGE", "ENTITY_INTERACTION_RANGE");
        ATTRIBUTE_NAME_MAPPING.put("GENERIC_BLOCK_BREAK_SPEED", "BLOCK_BREAK_SPEED");
        ATTRIBUTE_NAME_MAPPING.put("GENERIC_MINING_EFFICIENCY", "MINING_EFFICIENCY");
        ATTRIBUTE_NAME_MAPPING.put("GENERIC_SNEAKING_SPEED", "SNEAKING_SPEED");
        ATTRIBUTE_NAME_MAPPING.put("GENERIC_SUBMERGED_MINING_SPEED", "SUBMERGED_MINING_SPEED");
        ATTRIBUTE_NAME_MAPPING.put("GENERIC_SWEEPING_DAMAGE_RATIO", "SWEEPING_DAMAGE_RATIO");

        logger.info("初始化了 " + ATTRIBUTE_NAME_MAPPING.size() + " 个属性名称映射");
    }
    
    /**
     * 获取所有可用的属性
     * 使用反射和Registry API动态获取，避免硬编码
     *
     * @return 所有可用的属性集合
     */
    public static Set<Attribute> getAllAvailableAttributes() {
        if (availableAttributes == null) {
            availableAttributes = new HashSet<>();

            try {
                String version = detectPaperVersion();
                logger.info("开始初始化属性适配器... 检测到Paper版本: " + version);

                // 尝试使用Registry API (1.21+)
                if (isRegistryAvailable()) {
                    try {
                        logger.info("检测到Registry API可用，尝试使用...");
                        for (Attribute attribute : Registry.ATTRIBUTE) {
                            availableAttributes.add(attribute);
                        }
                        logger.info("使用Registry API成功加载了 " + availableAttributes.size() + " 个属性 (Paper " + version + ")");
                    } catch (Exception registryException) {
                        logger.warning("Registry API调用失败，回退到枚举方式: " + registryException.getMessage());
                        availableAttributes.clear(); // 清除可能的部分数据
                        loadAttributesFromEnum();
                    }
                } else {
                    // 回退到枚举方式 (1.20.1)
                    logger.info("Registry API不可用，使用枚举方式 (Paper " + version + ")");
                    loadAttributesFromEnum();
                }

                // 验证是否成功加载了属性
                if (availableAttributes.isEmpty()) {
                    logger.warning("未能加载任何属性，使用默认属性集合");
                    loadDefaultAttributes();
                }

                logger.info("属性适配器初始化完成，共加载 " + availableAttributes.size() + " 个属性 (Paper " + version + ")");

            } catch (Exception e) {
                logger.severe("属性适配器初始化严重失败: " + e.getMessage());
                e.printStackTrace();
                loadDefaultAttributes();
            }
        }

        return new HashSet<>(availableAttributes);
    }
    
    /**
     * 检查Registry API是否可用
     *
     * @return Registry API是否可用
     */
    private static boolean isRegistryAvailable() {
        try {
            // 检查Registry类是否存在
            Class.forName("org.bukkit.Registry");
            // 检查ATTRIBUTE字段是否存在且可访问
            Registry.ATTRIBUTE.iterator().hasNext();
            return true;
        } catch (Exception | NoClassDefFoundError | NoSuchFieldError e) {
            logger.info("Registry API不可用，使用枚举方式: " + e.getClass().getSimpleName());
            return false;
        }
    }

    /**
     * 检测Paper版本范围
     * 增强版本检测，支持更精确的版本识别
     *
     * @return 版本类型
     */
    private static String detectPaperVersion() {
        try {
            // 检查是否有Registry API
            if (isRegistryAvailable()) {
                // 检查1.21.4+特有的新属性
                try {
                    Set<String> newAttributes = Set.of("SCALE", "STEP_HEIGHT", "GRAVITY",
                        "SAFE_FALL_DISTANCE", "FALL_DAMAGE_MULTIPLIER", "BURNING_TIME",
                        "EXPLOSION_KNOCKBACK_RESISTANCE", "MOVEMENT_EFFICIENCY", "OXYGEN_BONUS");

                    int foundNewAttributes = 0;
                    for (Attribute attr : Registry.ATTRIBUTE) {
                        String name = getAttributeNameDirect(attr);
                        if (name != null && newAttributes.contains(name)) {
                            foundNewAttributes++;
                        }
                    }

                    if (foundNewAttributes >= 3) {
                        return "1.21.4+";
                    } else if (foundNewAttributes > 0) {
                        return "1.21.1-1.21.3";
                    } else {
                        return "1.21.0";
                    }
                } catch (Exception e) {
                    return "1.21.0+";
                }
            } else {
                // 没有Registry API，应该是1.20.x
                return "1.20.x";
            }
        } catch (Exception e) {
            return "unknown";
        }
    }
    
    /**
     * 从枚举加载属性 (1.20.1兼容)
     */
    @SuppressWarnings("deprecation")
    private static void loadAttributesFromEnum() {
        try {
            // 使用反射获取所有枚举值
            Attribute[] attributes = Attribute.values();
            availableAttributes.addAll(Arrays.asList(attributes));
        } catch (Exception e) {
            logger.warning("从枚举加载属性失败: " + e.getMessage());
            loadDefaultAttributes();
        }
    }
    
    /**
     * 加载默认属性集合（最后的回退方案）
     * 避免循环依赖，直接尝试创建属性对象
     */
    private static void loadDefaultAttributes() {
        availableAttributes = new HashSet<>();

        // 基础属性列表（确保在所有版本中都存在）
        String[] defaultAttributeNames = {
            "GENERIC_MAX_HEALTH", "MAX_HEALTH",
            "GENERIC_FOLLOW_RANGE", "FOLLOW_RANGE",
            "GENERIC_KNOCKBACK_RESISTANCE", "KNOCKBACK_RESISTANCE",
            "GENERIC_MOVEMENT_SPEED", "MOVEMENT_SPEED",
            "GENERIC_FLYING_SPEED", "FLYING_SPEED",
            "GENERIC_ATTACK_DAMAGE", "ATTACK_DAMAGE",
            "GENERIC_ATTACK_KNOCKBACK", "ATTACK_KNOCKBACK",
            "GENERIC_ATTACK_SPEED", "ATTACK_SPEED",
            "GENERIC_ARMOR", "ARMOR",
            "GENERIC_ARMOR_TOUGHNESS", "ARMOR_TOUGHNESS",
            "GENERIC_LUCK", "LUCK"
        };

        // 尝试通过反射获取属性，避免循环依赖
        for (String attributeName : defaultAttributeNames) {
            try {
                // 尝试通过枚举方式获取（1.20.1）
                try {
                    Attribute attribute = Attribute.valueOf(attributeName);
                    availableAttributes.add(attribute);
                    continue;
                } catch (Exception e) {
                    // 枚举方式失败，尝试其他方式
                }

                // 尝试通过Registry方式获取（1.21+）
                try {
                    if (isRegistryAvailable()) {
                        for (Attribute attr : Registry.ATTRIBUTE) {
                            String name = getAttributeNameDirect(attr);
                            if (attributeName.equalsIgnoreCase(name)) {
                                availableAttributes.add(attr);
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    // Registry方式失败，继续下一个
                }

            } catch (Exception e) {
                // 忽略单个属性的失败
            }
        }

        logger.info("加载了 " + availableAttributes.size() + " 个默认属性");
    }

    /**
     * 直接获取属性名称，避免循环依赖
     */
    private static String getAttributeNameDirect(Attribute attribute) {
        if (attribute == null) {
            return null;
        }

        try {
            // 尝试getKey方法
            NamespacedKey key = attribute.getKey();
            if (key != null) {
                return key.getKey().toUpperCase();
            }
        } catch (Exception e) {
            // 忽略
        }

        try {
            // 尝试枚举name方法
            if (attribute.getClass().isEnum()) {
                return ((Enum<?>) attribute).name();
            }
        } catch (Exception e) {
            // 忽略
        }

        // 回退到toString
        String result = attribute.toString();
        if (result != null && result.contains(":")) {
            String[] parts = result.split(":");
            if (parts.length > 1) {
                return parts[1].toUpperCase();
            }
        }
        return result != null ? result.toUpperCase() : null;
    }
    
    /**
     * 通过名称获取属性对象
     * 支持跨版本的属性名称映射
     * 
     * @param attributeName 属性名称
     * @return 属性对象，如果不存在则返回null
     */
    public static Attribute getAttributeByName(String attributeName) {
        if (attributeName == null || attributeName.trim().isEmpty()) {
            return null;
        }
        
        // 初始化属性名称映射缓存
        if (attributeByName == null) {
            initializeAttributeNameCache();
        }
        
        // 直接查找
        Attribute attribute = attributeByName.get(attributeName.toUpperCase());
        if (attribute != null) {
            return attribute;
        }
        
        // 尝试映射查找
        String mappedName = ATTRIBUTE_NAME_MAPPING.get(attributeName.toUpperCase());
        if (mappedName != null) {
            attribute = attributeByName.get(mappedName);
            if (attribute != null) {
                return attribute;
            }
        }
        
        // 反向映射查找
        for (Map.Entry<String, String> entry : ATTRIBUTE_NAME_MAPPING.entrySet()) {
            if (entry.getValue().equals(attributeName.toUpperCase())) {
                attribute = attributeByName.get(entry.getKey());
                if (attribute != null) {
                    return attribute;
                }
            }
        }

        // 尝试直接通过枚举获取（1.20.1兼容）
        try {
            return Attribute.valueOf(attributeName.toUpperCase());
        } catch (Exception e) {
            // 枚举方式失败，继续其他尝试
        }

        // 尝试添加GENERIC_前缀（1.20.1兼容）
        if (!attributeName.toUpperCase().startsWith("GENERIC_")) {
            try {
                return Attribute.valueOf("GENERIC_" + attributeName.toUpperCase());
            } catch (Exception e) {
                // 忽略
            }
        }

        // 尝试移除GENERIC_前缀（1.21+兼容）
        if (attributeName.toUpperCase().startsWith("GENERIC_")) {
            String withoutPrefix = attributeName.substring(8);
            try {
                return Attribute.valueOf(withoutPrefix.toUpperCase());
            } catch (Exception e) {
                // 忽略
            }
        }

        // 最后尝试通过Registry查找（如果可用）
        if (isRegistryAvailable()) {
            try {
                for (Attribute attr : Registry.ATTRIBUTE) {
                    String name = getAttributeNameDirect(attr);
                    if (attributeName.equalsIgnoreCase(name)) {
                        return attr;
                    }
                }
            } catch (Exception e) {
                // 忽略
            }
        }

        return null;
    }
    
    /**
     * 初始化属性名称缓存
     */
    private static void initializeAttributeNameCache() {
        attributeByName = new HashMap<>();
        
        try {
            // 尝试使用Registry API
            if (isRegistryAvailable()) {
                for (Attribute attribute : Registry.ATTRIBUTE) {
                    String name = getAttributeName(attribute);
                    if (name != null) {
                        attributeByName.put(name.toUpperCase(), attribute);
                    }
                }
            } else {
                // 回退到枚举方式
                loadAttributeNamesFromEnum();
            }
        } catch (Exception e) {
            logger.warning("初始化属性名称缓存失败: " + e.getMessage());
            loadDefaultAttributeNames();
        }
    }
    
    /**
     * 从枚举加载属性名称
     */
    @SuppressWarnings("deprecation")
    private static void loadAttributeNamesFromEnum() {
        try {
            Attribute[] attributes = Attribute.values();
            for (Attribute attribute : attributes) {
                String name = getAttributeName(attribute);
                if (name != null) {
                    attributeByName.put(name.toUpperCase(), attribute);
                }
            }
        } catch (Exception e) {
            logger.warning("从枚举加载属性名称失败: " + e.getMessage());
            loadDefaultAttributeNames();
        }
    }
    
    /**
     * 加载默认属性名称
     */
    private static void loadDefaultAttributeNames() {
        // 这里只能加载确定存在的属性
        // 具体实现需要根据实际情况调整
    }
    
    /**
     * 获取属性的名称
     * 兼容不同版本的API
     *
     * @param attribute 属性对象
     * @return 属性名称
     */
    public static String getAttributeName(Attribute attribute) {
        if (attribute == null) {
            return null;
        }

        try {
            // 优先尝试使用getKey()方法 (Registry方式，适用于1.21+)
            try {
                NamespacedKey key = attribute.getKey();
                if (key != null) {
                    return key.getKey().toUpperCase();
                }
            } catch (Exception e) {
                // getKey()方法不可用，继续尝试其他方法
            }

            // 尝试使用name()方法 (枚举方式，适用于1.20.1)
            try {
                // 使用反射安全地检查是否为枚举
                Class<?> clazz = attribute.getClass();
                if (clazz.isEnum()) {
                    return ((Enum<?>) attribute).name();
                }
            } catch (Exception e) {
                // 枚举方式不可用，继续尝试其他方法
            }

            // 最后回退到toString
            String result = attribute.toString();
            if (result != null && result.contains(":")) {
                // 如果是 "minecraft:max_health" 格式，提取后半部分
                String[] parts = result.split(":");
                if (parts.length > 1) {
                    return parts[1].toUpperCase();
                }
            }
            return result != null ? result.toUpperCase() : null;

        } catch (Exception e) {
            logger.warning("获取属性名称失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 检查属性是否存在
     * 
     * @param attributeName 属性名称
     * @return 属性是否存在
     */
    public static boolean isAttributeAvailable(String attributeName) {
        return getAttributeByName(attributeName) != null;
    }
    
    /**
     * 获取属性的显示名称
     * 
     * @param attribute 属性对象
     * @return 显示名称
     */
    public static String getDisplayName(Attribute attribute) {
        if (attribute == null) {
            return "未知属性";
        }
        
        String name = getAttributeName(attribute);
        if (name == null) {
            return "未知属性";
        }
        
        // 移除GENERIC_前缀并格式化
        String displayName = name.replace("GENERIC_", "");
        
        // 转换为友好的显示名称
        switch (displayName) {
            case "MAX_HEALTH": return "生命值";
            case "MOVEMENT_SPEED": return "移动速度";
            case "ATTACK_DAMAGE": return "攻击力";
            case "ATTACK_SPEED": return "攻击速度";
            case "ARMOR": return "防御力";
            case "ARMOR_TOUGHNESS": return "护甲韧性";
            case "KNOCKBACK_RESISTANCE": return "击退抗性";
            case "LUCK": return "幸运值";
            case "FOLLOW_RANGE": return "跟随范围";
            case "FLYING_SPEED": return "飞行速度";
            case "ATTACK_KNOCKBACK": return "攻击击退";
            default: return displayName.toLowerCase().replace("_", " ");
        }
    }
    
    /**
     * 清除缓存（用于重新加载）
     */
    public static void clearCache() {
        availableAttributes = null;
        attributeByName = null;
    }
}
