package com.jiedan.util;

import com.jiedan.dto.FlowNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FlowNodeCalculator {

    private static final List<FlowNode> DEFAULT_NODES = List.of(
            createNode("明确需求", "客户提交初步需求"),
            createNode("AI明确需求", "AI分析并完善需求"),
            createNode("客户验收", "确认需求文档"),
            createNode("AI拆分任务", "自动拆分子任务"),
            createNode("AI开发", "智能编码实现"),
            createNode("AI自测", "自动化测试"),
            createNode("客户验收", "功能验收测试"),
            createNode("打包交付", "项目交付"),
            createNode("客户验收", "最终验收"),
            createNode("项目完成", "项目结束")
    );

    private static FlowNode createNode(String title, String desc) {
        FlowNode node = new FlowNode();
        node.setTitle(title);
        node.setDesc(desc);
        node.setStatus("pending");
        return node;
    }

    public List<FlowNode> calculate(String status, Boolean needOnline, Integer currentNode) {
        List<FlowNode> nodes = new ArrayList<>();
        for (FlowNode defaultNode : DEFAULT_NODES) {
            FlowNode node = new FlowNode();
            node.setTitle(defaultNode.getTitle());
            node.setDesc(defaultNode.getDesc());
            node.setStatus("pending");
            nodes.add(node);
        }

        // Map status to target node
        int targetNode = switch (status) {
            case "PENDING" -> 1;
            case "PROCESSING" -> 2;
            case "QUOTED" -> 3;
            case "CONFIRMED" -> 4;
            case "DEVELOPING" -> 5;
            case "COMPLETED" -> 10;
            default -> 1;
        };

        // Update node statuses
        for (int i = 0; i < nodes.size(); i++) {
            int nodeIndex = i + 1;
            if (nodeIndex < targetNode) {
                nodes.get(i).setStatus("completed");
            } else if (nodeIndex == targetNode) {
                nodes.get(i).setStatus("active");
            }
        }

        // Update node 8 description based on needOnline
        if (Boolean.TRUE.equals(needOnline)) {
            nodes.get(7).setDesc("上线部署");
        }

        return nodes;
    }
}
