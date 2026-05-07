package edu.tut.cms.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarHistoryAddDTO {
    @NotNull
    private Long carId;
    private Integer gateId;
    @NotNull
    private Integer carHistoryType;
    private String carHistoryRemark;
}
