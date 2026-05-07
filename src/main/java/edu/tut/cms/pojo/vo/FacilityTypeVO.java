package edu.tut.cms.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacilityTypeVO {
    private Integer facilityTypeId;
    private String facilityTypeName;
    private String facilityTypeRemark;
}
