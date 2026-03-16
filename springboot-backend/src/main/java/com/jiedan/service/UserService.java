package com.jiedan.service;

import com.jiedan.dto.*;
import com.jiedan.entity.*;
import com.jiedan.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PointRecordRepository pointRecordRepository;

    public User getCurrentUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    @Transactional
    public User updateUser(Long userId, UserUpdate dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (dto.getNickname() != null) {
            user.setNickname(dto.getNickname());
        }
        if (dto.getAvatar() != null) {
            user.setAvatar(dto.getAvatar());
        }
        
        return userRepository.save(user);
    }

    /**
     * 获取推荐树（三级）
     * 通过 users.referrer_id 字段递归查询
     */
    public ReferralTreeItem getReferralTree(Long userId) {
        // 查询当前用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 构建三级推荐树
        ReferralTreeItem root = new ReferralTreeItem();
        root.setUser(convertToUserResponse(user));
        root.setLevel(0);
        root.setChildren(new ArrayList<>());
        
        // 查询一级推荐（直接推荐）
        List<User> level1Users = userRepository.findByReferrerId(userId);
        for (User level1User : level1Users) {
            ReferralTreeItem level1Item = new ReferralTreeItem();
            level1Item.setUser(convertToUserResponse(level1User));
            level1Item.setLevel(1);
            level1Item.setChildren(new ArrayList<>());
            
            // 查询二级推荐
            List<User> level2Users = userRepository.findByReferrerId(level1User.getId());
            for (User level2User : level2Users) {
                ReferralTreeItem level2Item = new ReferralTreeItem();
                level2Item.setUser(convertToUserResponse(level2User));
                level2Item.setLevel(2);
                level2Item.setChildren(new ArrayList<>());
                
                // 查询三级推荐
                List<User> level3Users = userRepository.findByReferrerId(level2User.getId());
                for (User level3User : level3Users) {
                    ReferralTreeItem level3Item = new ReferralTreeItem();
                    level3Item.setUser(convertToUserResponse(level3User));
                    level3Item.setLevel(3);
                    level3Item.setChildren(new ArrayList<>()); // 三级不再展开
                    level2Item.getChildren().add(level3Item);
                }
                
                level1Item.getChildren().add(level2Item);
            }
            
            root.getChildren().add(level1Item);
        }
        
        return root;
    }

    /**
     * 获取推荐统计
     */
    public ReferralStats getReferralStats(Long userId) {
        int level1 = userRepository.countByReferrerId(userId);
        
        int level2 = 0;
        int level3 = 0;
        
        // 统计二级和三级
        List<User> level1Users = userRepository.findByReferrerId(userId);
        for (User level1User : level1Users) {
            List<User> level2Users = userRepository.findByReferrerId(level1User.getId());
            level2 += level2Users.size();
            
            for (User level2User : level2Users) {
                level3 += userRepository.countByReferrerId(level2User.getId());
            }
        }
        
        return new ReferralStats(level1, level2, level3);
    }

    public Page<PointRecord> getPointRecords(Long userId, Pageable pageable) {
        return pointRecordRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * 获取推荐树统计信息
     * 用于前端 Canvas 布局计算
     */
    public ReferralTreeStats getReferralTreeStats(Long userId) {
        // 获取推荐树
        ReferralTreeItem tree = getReferralTree(userId);
        
        // 统计每级节点数
        List<Integer> nodesPerLevel = new ArrayList<>();
        nodesPerLevel.add(1); // 根节点
        
        // 统计一级
        int level1Count = tree.getChildren() != null ? tree.getChildren().size() : 0;
        nodesPerLevel.add(level1Count);
        
        // 统计二级
        int level2Count = 0;
        if (tree.getChildren() != null) {
            for (ReferralTreeItem level1 : tree.getChildren()) {
                if (level1.getChildren() != null) {
                    level2Count += level1.getChildren().size();
                }
            }
        }
        nodesPerLevel.add(level2Count);
        
        // 统计三级
        int level3Count = 0;
        if (tree.getChildren() != null) {
            for (ReferralTreeItem level1 : tree.getChildren()) {
                if (level1.getChildren() != null) {
                    for (ReferralTreeItem level2 : level1.getChildren()) {
                        if (level2.getChildren() != null) {
                            level3Count += level2.getChildren().size();
                        }
                    }
                }
            }
        }
        nodesPerLevel.add(level3Count);
        
        // 计算总节点数
        int totalNodes = 1 + level1Count + level2Count + level3Count;
        
        // 找出最大节点数
        int maxNodesInLevel = nodesPerLevel.stream().max(Integer::compare).orElse(1);
        
        // 计算所需画布宽度（假设前端节点直径64px，间距40px）
        int nodeWidth = 64;
        int minSpacing = 40;
        int padding = 40;
        int requiredWidth = maxNodesInLevel * nodeWidth + 
                           (maxNodesInLevel - 1) * minSpacing + 
                           padding * 2;
        
        // 假设屏幕宽度375px（iPhone标准宽度）
        int screenWidth = 375;
        boolean needHorizontalScroll = requiredWidth > screenWidth;
        
        // 构建层级统计
        java.util.Map<String, Integer> levelStats = new java.util.HashMap<>();
        levelStats.put("level0", 1);
        levelStats.put("level1", level1Count);
        levelStats.put("level2", level2Count);
        levelStats.put("level3", level3Count);
        
        return new ReferralTreeStats(
            3, // 最大层级
            nodesPerLevel,
            totalNodes,
            levelStats,
            requiredWidth,
            needHorizontalScroll
        );
    }

    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setPhone(user.getPhone());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setReferralCode(user.getReferralCode());
        response.setReferrerId(user.getReferrerId() != null ? user.getReferrerId().toString() : null);
        response.setTotalPoints(user.getTotalPoints());
        response.setIsActive(user.getIsActive());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }
}
