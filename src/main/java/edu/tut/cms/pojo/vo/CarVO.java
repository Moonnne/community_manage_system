package edu.tut.cms.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarVO {
    private Long id;
    private String carCode;
    private Long ownerId;
    private String ownerName;
    private Long assetId;
    private Long visitorId;
    private String carNumber;
    private Integer carType;
    private String carTypeName;
    private Integer carStatus;
}
