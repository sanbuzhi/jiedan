package com.jiedan.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrafficData {
    @Min(value = 0, message = "总用户数不能小于0")
    private Integer totalUsers;

    @Min(value = 0, message = "日活跃用户不能小于0")
    private Integer dau;

    @Min(value = 0, message = "并发数不能小于0")
    private Integer concurrent;
}
