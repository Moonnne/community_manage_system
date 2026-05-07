package edu.tut.cms.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillDetailVO {
    private Long id;
    private Long billId;
    private String billDetailName;
    private Integer billDetailType;
    private String billDetailTypeName;
    private Integer billDetailQuantity;
    private BigDecimal billDetailUnitPrice;
    private BigDecimal billDetailTotal;
    private String billDetailRemark;
}
