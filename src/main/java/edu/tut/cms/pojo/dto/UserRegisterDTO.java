package edu.tut.cms.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterDTO {
    @NotBlank
    private String uid;
    @NotBlank
    private String userName;
    @NotBlank
    @Size(min = 6, message = "密码长度不能少于6位")
    private String userPassword;
    @NotBlank
    private String captchaCode;
}
