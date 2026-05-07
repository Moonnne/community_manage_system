package edu.tut.cms.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    private Long id;
    private String uid;
    private String userName;
    private LocalDate userBirthday;
    private Integer userGender;
    private String userPhone;
    private String userEmail;
    private String userPassword;
    private Integer userRole;
    private Integer userStatus;
    private LocalDateTime userCreateTime;
    private String userAvata;
}
