package com.jiedan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlowNodeStatusResponse {
    private List<FlowNode> flowNodeStatus;
    private Integer currentFlowNode;
}
