package edu.tut.cms.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Facility {
    private Long id;
    private String facilityCode;
    private String facilityName;
    private Long facilityChiefId;
    private Integer facilityType;
    private Integer facilityStatus;
    private LocalDateTime facilityCheckTime;
    private Integer facilityExpirationDate;
    private Long facilityBuilding;
    private String facilityFloor;
    private String facilityPosition;
    private String facilityOwner;
    private String facilityRemark;
}
