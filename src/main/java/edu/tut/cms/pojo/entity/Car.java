package edu.tut.cms.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Car {
    private Long id;
    private String carCode;
    private Long ownerId;
    private Long assetId;
    private Long visitorId;
    private String carNumber;
    private Integer carType;
    private Integer carStatus;
}
