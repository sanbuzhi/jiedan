package com.jiedan.service;

import com.jiedan.entity.PointRecord;
import com.jiedan.entity.Rule;
import com.jiedan.entity.User;
import com.jiedan.repository.PointRecordRepository;
import com.jiedan.repository.RuleRepository;
import com.jiedan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RuleService {

    private final RuleRepository ruleRepository;
    private final PointRecordRepository pointRecordRepository;
    private final UserRepository userRepository;

    public List<Rule> getRules() {
        return ruleRepository.findByIsActiveTrue();
    }

    @Transactional
    public void initializeRules() {
        // Initialize default rules if not exists
        if (ruleRepository.count() == 0) {
            createDefaultRules();
        }
    }

    private void createDefaultRules() {
        // Referral rule
        Rule referralRule = new Rule();
        referralRule.setName("推荐奖励");
        referralRule.setCode("REFERRAL");
        referralRule.setDescription("推荐新用户注册获得积分");
        referralRule.setType("REFERRAL");
        referralRule.setPoints(100);
        referralRule.setMaxLevel(3);
        referralRule.setIsActive(true);
        referralRule.setCreatedAt(LocalDateTime.now());
        referralRule.setUpdatedAt(LocalDateTime.now());
        ruleRepository.save(referralRule);

        // Order rule
        Rule orderRule = new Rule();
        orderRule.setName("订单奖励");
        orderRule.setCode("ORDER");
        orderRule.setDescription("完成订单获得积分");
        orderRule.setType("ORDER");
        orderRule.setPoints(50);
        orderRule.setMaxLevel(1);
        orderRule.setIsActive(true);
        orderRule.setCreatedAt(LocalDateTime.now());
        orderRule.setUpdatedAt(LocalDateTime.now());
        ruleRepository.save(orderRule);
    }

    @Transactional
    public void grantReferralPoints(User newUser) {
        Rule rule = ruleRepository.findByCode("REFERRAL").orElse(null);
        if (rule == null || rule.getIsActive() == null || !rule.getIsActive()) {
            return;
        }

        // Build referrer chain using referrer_id field
        List<ReferrerInfo> referrerChain = buildReferrerChain(newUser, rule.getMaxLevel());

        for (ReferrerInfo info : referrerChain) {
            User referrer = info.getUser();
            int level = info.getLevel();

            // Calculate points based on level (decrease by 50% each level)
            int points = rule.getPoints();
            for (int i = 1; i < level; i++) {
                points = points / 2;
            }

            if (points <= 0) {
                continue;
            }

            // Create point record
            PointRecord record = new PointRecord();
            record.setUserId(referrer.getId());
            record.setType("REFERRAL");
            record.setAmount(points);
            record.setBalance(referrer.getTotalPoints() + points);
            record.setDescription("推荐用户 " + newUser.getNickname() + " 注册奖励（" + getLevelText(level) + "）");
            record.setRelatedUserId(newUser.getId());
            record.setRuleId(rule.getId());
            record.setCreatedAt(LocalDateTime.now());
            pointRecordRepository.save(record);

            // Update user total points
            referrer.setTotalPoints(referrer.getTotalPoints() + points);
            userRepository.save(referrer);
        }
    }

    /**
     * Build referrer chain using referrer_id field
     */
    private List<ReferrerInfo> buildReferrerChain(User user, int maxLevel) {
        List<ReferrerInfo> chain = new ArrayList<>();
        Long currentUserId = user.getReferrerId();
        int level = 1;

        while (currentUserId != null && level <= maxLevel) {
            User referrer = userRepository.findById(currentUserId).orElse(null);
            if (referrer == null) {
                break;
            }
            chain.add(new ReferrerInfo(referrer, level));
            currentUserId = referrer.getReferrerId();
            level++;
        }

        return chain;
    }

    private String getLevelText(int level) {
        switch (level) {
            case 1: return "一级";
            case 2: return "二级";
            case 3: return "三级";
            default: return level + "级";
        }
    }

    /**
     * Helper class to hold referrer info with level
     */
    private static class ReferrerInfo {
        private final User user;
        private final int level;

        public ReferrerInfo(User user, int level) {
            this.user = user;
            this.level = level;
        }

        public User getUser() {
            return user;
        }

        public int getLevel() {
            return level;
        }
    }
}
