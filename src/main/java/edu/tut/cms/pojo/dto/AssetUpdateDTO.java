package edu.tut.cms.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetUpdateDTO {
    @NotNull
    private Long id;
    private Long assetOwnerId;
    private String assetName;
    private Integer assetType;
    private LocalDateTime assetDueTime;
    private Integer assetStatus;
    private BigDecimal assetArea;
    private Long assetBuilding;
    private String assetFloor;
    private String assetPosition;
    private String assetRemark;
}
