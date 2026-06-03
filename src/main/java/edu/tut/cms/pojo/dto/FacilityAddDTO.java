package edu.tut.cms.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacilityAddDTO {
    private String facilityCode;
    @NotBlank
    private String facilityName;
    private Long facilityChiefId;
    @NotNull
    private Integer facilityType;
    @NotNull
    private Integer facilityStatus;
    private LocalDateTime facilityCheckTime;
    private Integer facilityExpirationDate;
    @NotNull
    private Long facilityBuilding;
    private String facilityFloor;
    private String facilityPosition;
    private String facilityOwner;
    private String facilityRemark;
}
