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
public class BillUpdateDTO {
    @NotNull
    private Long id;
    private LocalDateTime billStartTime;
    private LocalDateTime billEndTime;
    private Integer billStatus;
    private BigDecimal billTotal;
    private BigDecimal billPaid;
    private String billRemark;
}
