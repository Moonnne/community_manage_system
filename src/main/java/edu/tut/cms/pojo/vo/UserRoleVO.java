package edu.tut.cms.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRoleVO {
    private Integer roleId;
    private String roleName;
    private String rolePromission;
}
