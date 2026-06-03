package edu.tut.cms.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuildingAddDTO {
    @NotBlank
    private String buildingName;
    @NotNull
    private Integer buildingMinFloor;
    @NotNull
    private Integer buildingMaxFloor;
    private String buildingLocation;
    private String buildingRemark;
}
