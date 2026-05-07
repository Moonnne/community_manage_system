package edu.tut.cms.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BillDetail {
    private Long id;
    private Long billId;
    private String billDetailName;
    private Integer billDetailType;
    private Integer billDetailQuantity;
    private BigDecimal billDetailUnitPrice;
    private BigDecimal billDetailTotal;
    private String billDetailRemark;
}
