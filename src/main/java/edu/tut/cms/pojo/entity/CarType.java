package edu.tut.cms.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CarType {
    private Integer carTypeId;
    private String carTypeName;
    private String carTypeRemark;
}
