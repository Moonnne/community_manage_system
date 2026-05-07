package edu.tut.cms.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarTypeAddDTO {
    @NotNull
    private Integer carTypeId;
    @NotBlank
    private String carTypeName;
    private String carTypeRemark;
}
