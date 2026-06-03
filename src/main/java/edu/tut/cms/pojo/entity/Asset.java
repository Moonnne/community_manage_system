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
public class Asset {
    private Long id;
    private String assetCode;
    private Long assetOwnerId;
    private String assetName;
    private Integer assetType;
    private LocalDateTime assetRegTime;
    private LocalDateTime assetDueTime;
    private Integer assetStatus;
    private BigDecimal assetArea;
    private Long assetBuilding;
    private String assetFloor;
    private String assetPosition;
    private String assetRemark;
}
