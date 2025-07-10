package com.github.chengge.zprefix.data;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 玩家称号数据类
 * 存储单个玩家的称号相关数据
 */
public class PlayerTitleData {
    
    private final UUID playerId;
    private String currentTitle;
    private final Set<String> unlockedTitles;
    private long lastSaveTime;
    
    /**
     * 构造函数
     * 
     * @param playerId 玩家UUID
     */
    public PlayerTitleData(UUID playerId) {
        this.playerId = playerId;
        this.currentTitle = null;
        this.unlockedTitles = new HashSet<>();
        this.lastSaveTime = System.currentTimeMillis();
    }
    
    /**
     * 构造函数（用于从存储加载数据）
     * 
     * @param playerId 玩家UUID
     * @param currentTitle 当前称号
     * @param unlockedTitles 已解锁的称号集合
     */
    public PlayerTitleData(UUID playerId, String currentTitle, Set<String> unlockedTitles) {
        this.playerId = playerId;
        this.currentTitle = currentTitle;
        this.unlockedTitles = new HashSet<>(unlockedTitles);
        this.lastSaveTime = System.currentTimeMillis();
    }
    
    /**
     * 获取玩家UUID
     * 
     * @return 玩家UUID
     */
    public UUID getPlayerId() {
        return playerId;
    }
    
    /**
     * 获取当前称号
     * 
     * @return 当前称号ID，如果没有则返回null
     */
    public String getCurrentTitle() {
        return currentTitle;
    }
    
    /**
     * 设置当前称号
     * 
     * @param titleId 称号ID
     */
    public void setCurrentTitle(String titleId) {
        this.currentTitle = titleId;
        updateSaveTime();
    }
    
    /**
     * 移除当前称号
     */
    public void removeCurrentTitle() {
        this.currentTitle = null;
        updateSaveTime();
    }
    
    /**
     * 获取已解锁的称号集合
     * 
     * @return 已解锁的称号ID集合
     */
    public Set<String> getUnlockedTitles() {
        return new HashSet<>(unlockedTitles);
    }
    
    /**
     * 解锁称号
     * 
     * @param titleId 称号ID
     * @return 是否成功解锁（如果已经解锁则返回false）
     */
    public boolean unlockTitle(String titleId) {
        boolean added = unlockedTitles.add(titleId);
        if (added) {
            updateSaveTime();
        }
        return added;
    }
    
    /**
     * 移除已解锁的称号
     * 
     * @param titleId 称号ID
     * @return 是否成功移除
     */
    public boolean removeUnlockedTitle(String titleId) {
        boolean removed = unlockedTitles.remove(titleId);
        if (removed) {
            updateSaveTime();
            // 如果移除的是当前使用的称号，则清空当前称号
            if (titleId.equals(currentTitle)) {
                currentTitle = null;
            }
        }
        return removed;
    }
    
    /**
     * 检查是否已解锁指定称号
     * 
     * @param titleId 称号ID
     * @return 是否已解锁
     */
    public boolean hasUnlockedTitle(String titleId) {
        return unlockedTitles.contains(titleId);
    }
    
    /**
     * 检查是否有任何已解锁的称号
     * 
     * @return 是否有已解锁的称号
     */
    public boolean hasAnyUnlockedTitle() {
        return !unlockedTitles.isEmpty();
    }
    
    /**
     * 获取已解锁称号的数量
     * 
     * @return 已解锁称号数量
     */
    public int getUnlockedTitleCount() {
        return unlockedTitles.size();
    }
    
    /**
     * 检查是否正在使用指定称号
     * 
     * @param titleId 称号ID
     * @return 是否正在使用
     */
    public boolean isUsingTitle(String titleId) {
        return titleId != null && titleId.equals(currentTitle);
    }
    
    /**
     * 检查是否有当前称号
     * 
     * @return 是否有当前称号
     */
    public boolean hasCurrentTitle() {
        return currentTitle != null && !currentTitle.isEmpty();
    }
    
    /**
     * 获取最后保存时间
     * 
     * @return 最后保存时间戳
     */
    public long getLastSaveTime() {
        return lastSaveTime;
    }
    
    /**
     * 更新保存时间
     */
    private void updateSaveTime() {
        this.lastSaveTime = System.currentTimeMillis();
    }
    
    /**
     * 清空所有数据
     */
    public void clear() {
        this.currentTitle = null;
        this.unlockedTitles.clear();
        updateSaveTime();
    }
    
    @Override
    public String toString() {
        return "PlayerTitleData{" +
                "playerId=" + playerId +
                ", currentTitle='" + currentTitle + '\'' +
                ", unlockedTitles=" + unlockedTitles.size() +
                '}';
    }
}
