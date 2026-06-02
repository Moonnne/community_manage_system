package edu.tut.cms.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillAddDTO {
    private String billCode;
    private Long ownerId;
    private Long assetId;
    @NotNull
    private LocalDateTime billStartTime;
    @NotNull
    private LocalDateTime billEndTime;
    private BigDecimal billTotal;
    private BigDecimal billPaid;
    private String billRemark;
    private List<Long> assetIds;
    private List<BillDetailAddDTO> details;
}
