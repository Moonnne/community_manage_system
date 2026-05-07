package edu.tut.cms.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GateUpdateDTO {
    @NotNull
    private Integer id;
    private String gateName;
    private Long gateChiefId;
    private String gateRemark;
}
