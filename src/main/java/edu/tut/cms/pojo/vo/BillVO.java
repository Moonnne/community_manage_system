package edu.tut.cms.pojo.vo;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillVO {
    private Long id;
    private String billCode;
    private Long ownerId;
    private String ownerName;
    private Long createrId;
    private String createrName;
    private Long assetId;
    private String assetName;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime billCreateTime;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime billStartTime;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime billEndTime;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime billFinishTime;
    private Integer billStatus;
    private BigDecimal billTotal;
    private BigDecimal billPaid;
    private String billRemark;
    private List<BillDetailVO> details;
}
