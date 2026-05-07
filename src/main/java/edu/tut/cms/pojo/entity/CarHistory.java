package edu.tut.cms.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CarHistory {
    private Long id;
    private String carHistoryCode;
    private Long carHistoryOperatorId;
    private Long carId;
    private Integer gateId;
    private Integer carHistoryType;
    private LocalDateTime carHistoryTime;
    private String carHistoryRemark;
}
