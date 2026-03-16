package com.jiedan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InfrastructureDetail {

    private InfrastructureItem server;
    private InfrastructureItem domain;
    private InfrastructureItem ssl;
    private InfrastructureItem cdn;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InfrastructureItem {
        private String name;
        private BigDecimal cost;
        private String config;
        private Boolean optional;
    }
}
