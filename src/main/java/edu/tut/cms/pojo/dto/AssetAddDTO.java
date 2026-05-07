package edu.tut.cms.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetAddDTO {
    @NotBlank
    private String assetCode;
    private Long assetOwnerId;
    @NotBlank
    private String assetName;
    @NotNull
    private Integer assetType;
    private LocalDateTime assetDueTime;
    private Integer assetStatus;
    private BigDecimal assetArea;
    private String assetBuilding;
    private String assetFloor;
    private String assetPosition;
    private String assetRemark;
}
