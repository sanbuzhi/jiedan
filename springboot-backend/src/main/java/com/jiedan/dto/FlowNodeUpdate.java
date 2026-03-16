package com.jiedan.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlowNodeUpdate {
    @NotNull(message = "节点索引不能为空")
    @Min(value = 0, message = "节点索引不能小于0")
    private Integer nodeIndex;

    @NotBlank(message = "状态不能为空")
    private String status;
}
