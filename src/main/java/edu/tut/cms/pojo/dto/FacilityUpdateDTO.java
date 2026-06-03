package edu.tut.cms.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacilityUpdateDTO {
    @NotNull
    private Long id;
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
