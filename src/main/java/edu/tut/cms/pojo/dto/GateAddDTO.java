package edu.tut.cms.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GateAddDTO {
    @NotBlank
    private String gateCode;
    @NotBlank
    private String gateName;
    private Long gateChiefId;
    private String gateRemark;
}
