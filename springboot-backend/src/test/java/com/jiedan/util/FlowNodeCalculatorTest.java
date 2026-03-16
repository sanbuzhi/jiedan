package com.jiedan.util;

import com.jiedan.dto.FlowNode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FlowNodeCalculatorTest {

    private final FlowNodeCalculator calculator = new FlowNodeCalculator();

    @Test
    public void testCalculatePending() {
        List<FlowNode> nodes = calculator.calculate("PENDING", false, 1);

        assertNotNull(nodes);
        assertEquals(10, nodes.size());
        assertEquals("active", nodes.get(0).getStatus());
        assertEquals("pending", nodes.get(1).getStatus());
    }

    @Test
    public void testCalculateProcessing() {
        List<FlowNode> nodes = calculator.calculate("PROCESSING", false, 2);

        assertEquals("completed", nodes.get(0).getStatus());
        assertEquals("active", nodes.get(1).getStatus());
        assertEquals("pending", nodes.get(2).getStatus());
    }

    @Test
    public void testCalculateWithOnline() {
        List<FlowNode> nodes = calculator.calculate("PENDING", true, 1);

        assertEquals("上线部署", nodes.get(7).getDesc());
    }

    @Test
    public void testCalculateWithoutOnline() {
        List<FlowNode> nodes = calculator.calculate("PENDING", false, 1);

        assertEquals("项目交付", nodes.get(7).getDesc());
    }
}
