package com.kidswear.pos.dto;

import lombok.Data;

@Data
public class LoginResponseDTO {
    private String token;
    private UserDTO user;
}