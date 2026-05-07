package edu.tut.cms.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Gate {
    private Integer id;
    private String gateCode;
    private String gateName;
    private Long gateChiefId;
    private String gateRemark;
}
