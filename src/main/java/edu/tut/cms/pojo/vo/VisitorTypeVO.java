package edu.tut.cms.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisitorTypeVO {
    private Integer visitorTypeId;
    private String visitorTypeName;
    private String visitorTypeRemark;
}
