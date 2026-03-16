package com.jiedan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReferralTreeItem {
    private UserResponse user;
    private Integer level;
    private List<ReferralTreeItem> children;
}
