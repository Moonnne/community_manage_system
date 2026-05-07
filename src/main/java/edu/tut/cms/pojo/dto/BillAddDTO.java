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
public class BillAddDTO {
    @NotBlank
    private String billCode;
    @NotNull
    private Long ownerId;
    @NotNull
    private Long assetId;
    private LocalDateTime billStartTime;
    private LocalDateTime billEndTime;
    @NotNull
    private BigDecimal billTotal;
    private BigDecimal billPaid;
    private String billRemark;
}
