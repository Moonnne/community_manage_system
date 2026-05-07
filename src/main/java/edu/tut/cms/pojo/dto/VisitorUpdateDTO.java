package edu.tut.cms.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VisitorUpdateDTO {
    @NotNull
    private Long id;
    private String visitorStatus;
    private String visitorRemark;
}
