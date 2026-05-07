package edu.tut.cms.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillDetailAddDTO {
    @NotNull
    private Long billId;
    private String billDetailName;
    private Integer billDetailType;
    @NotNull
    private Integer billDetailQuantity;
    @NotNull
    private BigDecimal billDetailUnitPrice;
    @NotNull
    private BigDecimal billDetailTotal;
    private String billDetailRemark;
}
