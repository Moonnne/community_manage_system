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
public class BillUpdateDTO {
    @NotNull
    private Long id;
    private Long ownerId;
    private Long assetId;
    private LocalDateTime billStartTime;
    private LocalDateTime billEndTime;
    private Integer billStatus;
    private BigDecimal billTotal;
    private BigDecimal billPaid;
    private String billRemark;
    private List<BillDetailAddDTO> details;
}
