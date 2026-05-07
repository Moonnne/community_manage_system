package edu.tut.cms.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleAddDTO {
    @NotBlank
    private String roleName;

    @NotBlank
    @Pattern(regexp = "[0-3]", message = "权限等级必须为0到3")
    private String rolePromission;
}
