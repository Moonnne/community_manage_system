package edu.tut.cms.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuildingVO {
    private Long id;
    private String buildingCode;
    private String buildingName;
    private Integer buildingMinFloor;
    private Integer buildingMaxFloor;
    private String buildingLocation;
    private String buildingRemark;
}
