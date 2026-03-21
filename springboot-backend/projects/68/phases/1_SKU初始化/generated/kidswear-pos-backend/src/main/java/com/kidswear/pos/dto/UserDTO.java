package com.kidswear.pos.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String role;
}