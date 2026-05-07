package edu.tut.cms.pojo.vo;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVO {
    private Long id;
    private String uid;
    private String userName;
    @JSONField(format = "yyyy-MM-dd")
    private LocalDate userBirthday;
    private Integer userGender;
    private String userPhone;
    private String userEmail;
    private Integer userRole;
    private String roleName;
    private Integer userStatus;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime userCreateTime;
    private String userAvata;
    private String token;
}
