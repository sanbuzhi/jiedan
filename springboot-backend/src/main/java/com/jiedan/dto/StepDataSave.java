package com.jiedan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StepDataSave {
    @NotBlank(message = "步骤不能为空")
    private String step;

    @NotNull(message = "数据不能为空")
    private Map<String, Object> data;
}
