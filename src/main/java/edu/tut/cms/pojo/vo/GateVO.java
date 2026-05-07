package edu.tut.cms.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GateVO {
    private Integer id;
    private String gateCode;
    private String gateName;
    private Long gateChiefId;
    private String chiefName;
    private String gateRemark;
}
