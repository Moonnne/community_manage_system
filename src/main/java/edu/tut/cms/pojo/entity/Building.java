package edu.tut.cms.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Building {
    private Long id;
    private String buildingCode;
    private String buildingName;
    private Integer buildingMinFloor;
    private Integer buildingMaxFloor;
    private String buildingLocation;
    private String buildingRemark;
}
