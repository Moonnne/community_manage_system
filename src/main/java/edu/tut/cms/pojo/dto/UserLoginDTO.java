package edu.tut.cms.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginDTO {
    /** 用户识别符（登录账号） */
    @NotBlank
    private String uid;
    @NotBlank
    private String userPassword;
    @NotBlank
    private String captchaCode;
}
