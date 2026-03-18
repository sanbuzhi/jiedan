package com.jiedan.task.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class LoginDTO {
    @NotBlank(message = "code不能为空")
    private String code;
    private String nickname;
    private String avatarUrl;
}