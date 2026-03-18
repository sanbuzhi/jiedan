package com.jiedan.service.ai;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 版本收集器
 * 收集并行任务生成的所有版本及其验证结果
 */
@Slf4j
@Component
public class VersionCollector {

    /**
     * 版本信息（精简版）
     * 只保留核心字段：版本ID、内容、验证决策、时间戳
     */
    @Data
    public static class VersionInfo {
        private String versionId;           // V1-1, V1-2, V2-1...
        private String content;             // 完整文档内容
        private String validationDecision;  // ALLOW/REPAIR/REJECT
        private long timestamp;             // 生成时间戳

        public VersionInfo(String versionId) {
            this.versionId = versionId;
            this.timestamp = System.currentTimeMillis();
        }

        /**
         * 获取版本号用于文件保存
         */
        public String getVersionForFile() {
            return versionId;
        }
    }

    // 项目ID -> 版本列表
    private final Map<String, List<VersionInfo>> projectVersions = new ConcurrentHashMap<>();
    
    // 读写锁
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * 初始化项目版本收集
     */
    public void initProject(String projectId) {
        lock.writeLock().lock();
        try {
            projectVersions.put(projectId, new CopyOnWriteArrayList<>());
            log.info("初始化版本收集器, projectId: {}, 当前收集器项目数: {}", projectId, projectVersions.size());
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 添加版本信息
     */
    public void addVersion(String projectId, VersionInfo versionInfo) {
        lock.writeLock().lock();
        try {
            List<VersionInfo> list = projectVersions.computeIfAbsent(projectId, k -> new CopyOnWriteArrayList<>());
            list.add(versionInfo);
            log.info("添加版本信息, projectId: {}, versionId: {}, decision: {}, 当前列表大小: {}",
                    projectId, versionInfo.getVersionId(), versionInfo.getValidationDecision(), list.size());
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 获取项目的所有版本
     */
    public List<VersionInfo> getAllVersions(String projectId) {
        lock.readLock().lock();
        try {
            List<VersionInfo> list = projectVersions.get(projectId);
            if (list == null) {
                log.warn("获取版本列表失败, projectId: {} 不存在, available projects: {}", projectId, projectVersions.keySet());
                return new CopyOnWriteArrayList<>();
            }
            return new CopyOnWriteArrayList<>(list);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 获取指定状态的版本
     */
    public List<VersionInfo> getVersionsByDecision(String projectId, String decision) {
        return getAllVersions(projectId).stream()
                .filter(v -> decision.equals(v.getValidationDecision()))
                .toList();
    }

    /**
     * 获取ALLOW状态的版本
     */
    public List<VersionInfo> getAllowVersions(String projectId) {
        return getVersionsByDecision(projectId, "ALLOW");
    }

    /**
     * 获取REPAIR状态的版本
     */
    public List<VersionInfo> getRepairVersions(String projectId) {
        return getVersionsByDecision(projectId, "REPAIR");
    }

    /**
     * 获取REJECT状态的版本
     */
    public List<VersionInfo> getRejectVersions(String projectId) {
        return getVersionsByDecision(projectId, "REJECT");
    }

    /**
     * 获取最佳版本（按优先级）
     * 1. ALLOW状态的版本
     * 2. REPAIR状态的版本
     * 3. REJECT状态的版本
     */
    public Optional<VersionInfo> getBestVersion(String projectId) {
        List<VersionInfo> allVersions = getAllVersions(projectId);

        if (allVersions.isEmpty()) {
            return Optional.empty();
        }

        // 优先选择ALLOW状态的最新版本
        List<VersionInfo> allowVersions = getAllowVersions(projectId);
        if (!allowVersions.isEmpty()) {
            return Optional.of(getLatestVersion(allowVersions));
        }

        // 其次选择REPAIR状态的最新版本
        List<VersionInfo> repairVersions = getRepairVersions(projectId);
        if (!repairVersions.isEmpty()) {
            return Optional.of(getLatestVersion(repairVersions));
        }

        // 最后选择REJECT状态的最新版本
        List<VersionInfo> rejectVersions = getRejectVersions(projectId);
        if (!rejectVersions.isEmpty()) {
            return Optional.of(getLatestVersion(rejectVersions));
        }

        return Optional.empty();
    }

    /**
     * 获取最新版本（按时间戳）
     */
    private VersionInfo getLatestVersion(List<VersionInfo> versions) {
        return versions.stream()
                .max(Comparator.comparingLong(VersionInfo::getTimestamp))
                .orElse(versions.get(0));
    }

    /**
     * 清理项目版本数据
     */
    public void clearProject(String projectId) {
        lock.writeLock().lock();
        try {
            projectVersions.remove(projectId);
            log.info("清理版本收集器, projectId: {}", projectId);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 获取版本统计信息
     */
    public Map<String, Object> getVersionStats(String projectId) {
        List<VersionInfo> versions = getAllVersions(projectId);
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalCount", versions.size());
        stats.put("allowCount", getAllowVersions(projectId).size());
        stats.put("repairCount", getRepairVersions(projectId).size());
        stats.put("rejectCount", getRejectVersions(projectId).size());

        return stats;
    }

    /**
     * 构建版本内容Map（供任务决策者使用）
     */
    public Map<String, String> buildVersionContentMap(String projectId) {
        Map<String, String> contentMap = new LinkedHashMap<>();
        List<VersionInfo> versions = getAllVersions(projectId);

        // 按版本号排序
        versions.sort(Comparator.comparing(VersionInfo::getVersionId));

        for (VersionInfo version : versions) {
            contentMap.put(version.getVersionId(), version.getContent());
        }

        return contentMap;
    }
}
