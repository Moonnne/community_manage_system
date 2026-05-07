package edu.tut.cms.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillDetailUpdateDTO {
    @NotNull
    private Long id;
    private String billDetailName;
    private Integer billDetailType;
    private Integer billDetailQuantity;
    private BigDecimal billDetailUnitPrice;
    private BigDecimal billDetailTotal;
    private String billDetailRemark;
}
