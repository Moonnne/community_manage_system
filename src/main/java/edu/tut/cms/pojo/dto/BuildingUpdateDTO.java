package edu.tut.cms.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuildingUpdateDTO {
    @NotNull
    private Long id;
    private String buildingName;
    private Integer buildingMinFloor;
    private Integer buildingMaxFloor;
    private String buildingLocation;
    private String buildingRemark;
}
