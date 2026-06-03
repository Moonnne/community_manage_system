package edu.tut.cms.pojo.vo;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacilityVO {
    private Long id;
    private String facilityCode;
    private String facilityName;
    private Long facilityChiefId;
    private String chiefName;
    private Integer facilityType;
    private String facilityTypeName;
    private Integer facilityStatus;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime facilityCheckTime;
    private Integer facilityExpirationDate;
    private Long facilityBuilding;
    private String buildingCode;
    private String buildingName;
    private Integer buildingMinFloor;
    private Integer buildingMaxFloor;
    private String buildingLocation;
    private String facilityFloor;
    private String facilityPosition;
    private String facilityOwner;
    private String facilityRemark;
}
