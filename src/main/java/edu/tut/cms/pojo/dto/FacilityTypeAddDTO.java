package edu.tut.cms.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacilityTypeAddDTO {
    @NotNull
    private Integer facilityTypeId;
    @NotBlank
    private String facilityTypeName;
    private String facilityTypeRemark;
}
