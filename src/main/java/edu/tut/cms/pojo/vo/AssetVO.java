package edu.tut.cms.pojo.vo;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetVO {
    private Long id;
    private String assetCode;
    private Long assetOwnerId;
    private String ownerName;
    private String assetName;
    private Integer assetType;
    private String assetTypeName;
    private String assetTypeShort;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime assetRegTime;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime assetDueTime;
    private Integer assetStatus;
    private BigDecimal assetArea;
    private Long assetBuilding;
    private String buildingCode;
    private String buildingName;
    private Integer buildingMinFloor;
    private Integer buildingMaxFloor;
    private String buildingLocation;
    private String assetFloor;
    private String assetPosition;
    private String assetRemark;
}
