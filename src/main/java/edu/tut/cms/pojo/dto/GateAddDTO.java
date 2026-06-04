package edu.tut.cms.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GateAddDTO {
    @NotBlank
    @Pattern(regexp = "^[A-Z]{1,2}\\d{1,2}$", message = "门岗编号需由至多两位大写字母和至多两位数字组成")
    private String gateCode;
    @NotBlank
    private String gateName;
    private Long gateChiefId;
    private String gateRemark;
}
