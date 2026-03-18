package com.jiedan.task.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class TaskDTO {
    private Long id;
    @NotBlank(message = "标题不能为空")
    private String title;
    private String content;
    @NotNull(message = "优先级不能为空")
    private Integer priority;
    private LocalDateTime deadline;
}