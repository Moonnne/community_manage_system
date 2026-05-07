package edu.tut.cms.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Bill {
    private Long id;
    private String billCode;
    private Long ownerId;
    private Long createrId;
    private Long assetId;
    private LocalDateTime billCreateTime;
    private LocalDateTime billStartTime;
    private LocalDateTime billEndTime;
    private LocalDateTime billFinishTime;
    private Integer billStatus;
    private BigDecimal billTotal;
    private BigDecimal billPaid;
    private String billRemark;
}
