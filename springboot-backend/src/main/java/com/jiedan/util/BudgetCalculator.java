package com.jiedan.util;

import com.jiedan.dto.BudgetResponse;
import com.jiedan.entity.Requirement;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class BudgetCalculator {

    private static final Map<String, Integer> BASE_FEES = Map.of(
            "WECHAT_MINIPROGRAM", 5000,
            "DOUYIN_MINIPROGRAM", 5000,
            "WEBSITE", 8000,
            "CRAWLER", 6000,
            "UNCLEAR", 10000,
            "OTHER", 10000
    );

    private static final Map<String, BigDecimal> URGENCY_MULTIPLIERS = Map.of(
            "LOW", new BigDecimal("1.0"),
            "NORMAL", new BigDecimal("1.0"),
            "HIGH", new BigDecimal("1.2"),
            "URGENT", new BigDecimal("1.5")
    );

    private static final BigDecimal TRAFFIC_FEE_PER_10000 = new BigDecimal("2000");
    private static final BigDecimal INFRASTRUCTURE_FEE = new BigDecimal("2000");
    private static final int TRAFFIC_THRESHOLD = 10000;

    public BudgetResponse calculate(Requirement requirement) {
        // Base fee
        BigDecimal baseFee = new BigDecimal(BASE_FEES.getOrDefault(requirement.getProjectType(), 10000));

        // Traffic fee
        BigDecimal trafficFee = BigDecimal.ZERO;
        if (requirement.getTraffic() != null) {
            Integer totalUsers = (Integer) requirement.getTraffic().get("total_users");
            if (totalUsers != null && totalUsers > TRAFFIC_THRESHOLD) {
                int units = totalUsers / TRAFFIC_THRESHOLD;
                trafficFee = TRAFFIC_FEE_PER_10000.multiply(new BigDecimal(units));
            }
        }

        // AI development fee (base + traffic)
        BigDecimal aiDevelopmentFee = baseFee.add(trafficFee);

        // Urgency multiplier
        BigDecimal urgencyMultiplier = URGENCY_MULTIPLIERS.getOrDefault(requirement.getUrgency(), BigDecimal.ONE);
        aiDevelopmentFee = aiDevelopmentFee.multiply(urgencyMultiplier);

        // Platform service fee (20% of AI development fee)
        BigDecimal platformServiceFee = aiDevelopmentFee.multiply(new BigDecimal("0.2"));

        // Infrastructure fee
        BigDecimal infrastructureFee = requirement.getNeedOnline() ? INFRASTRUCTURE_FEE : BigDecimal.ZERO;

        // Total budget
        BigDecimal totalBudget = aiDevelopmentFee.add(platformServiceFee).add(infrastructureFee);

        BudgetResponse response = new BudgetResponse();
        response.setAiDevelopmentFee(aiDevelopmentFee);
        response.setPlatformServiceFee(platformServiceFee);
        response.setTotalBudget(totalBudget);
        response.setCurrency("CNY");

        return response;
    }
}
