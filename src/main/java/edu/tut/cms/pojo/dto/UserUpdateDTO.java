package edu.tut.cms.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDTO {
    @NotNull
    private Long id;
    private String userName;
    private LocalDate userBirthday;
    private Integer userGender;
    private String userPhone;
    private String userEmail;
    private String userAvata;
}
