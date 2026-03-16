package com.jiedan.util;

import com.jiedan.dto.BudgetResponse;
import com.jiedan.entity.Requirement;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class BudgetCalculatorTest {

    private final BudgetCalculator calculator = new BudgetCalculator();

    @Test
    public void testCalculateWechatMiniprogram() {
        Requirement req = createRequirement("WECHAT_MINIPROGRAM", false, "NORMAL", 5000);

        BudgetResponse budget = calculator.calculate(req);

        assertNotNull(budget);
        assertEquals(0, budget.getAiDevelopmentFee().compareTo(new BigDecimal("5000")));
        assertEquals(0, budget.getPlatformServiceFee().compareTo(new BigDecimal("1000")));
        assertEquals(0, budget.getTotalBudget().compareTo(new BigDecimal("6000")));
    }

    @Test
    public void testCalculateWithUrgency() {
        Requirement req = createRequirement("WECHAT_MINIPROGRAM", false, "URGENT", 5000);

        BudgetResponse budget = calculator.calculate(req);

        assertNotNull(budget);
        // Base 5000 * 1.5 = 7500
        assertEquals(0, budget.getAiDevelopmentFee().compareTo(new BigDecimal("7500")));
    }

    @Test
    public void testCalculateWithOnline() {
        Requirement req = createRequirement("WECHAT_MINIPROGRAM", true, "NORMAL", 5000);

        BudgetResponse budget = calculator.calculate(req);

        assertNotNull(budget);
        // 5000 + 1000 + 2000 = 8000
        assertEquals(0, budget.getTotalBudget().compareTo(new BigDecimal("8000")));
    }

    @Test
    public void testCalculateWithHighTraffic() {
        Requirement req = createRequirement("WECHAT_MINIPROGRAM", false, "NORMAL", 25000);

        BudgetResponse budget = calculator.calculate(req);

        assertNotNull(budget);
        // Base 5000 + traffic 4000 = 9000
        assertEquals(0, budget.getAiDevelopmentFee().compareTo(new BigDecimal("9000")));
    }

    private Requirement createRequirement(String projectType, boolean needOnline, String urgency, int totalUsers) {
        Requirement req = new Requirement();
        req.setProjectType(projectType);
        req.setNeedOnline(needOnline);
        req.setUrgency(urgency);
        req.setTraffic(Map.of("total_users", totalUsers));
        return req;
    }
}
