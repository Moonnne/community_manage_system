package edu.tut.cms.pojo.vo;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarHistoryVO {
    private Long id;
    private String carHistoryCode;
    private Long carHistoryOperatorId;
    private String operatorName;
    private Long carId;
    private Integer carNumber;
    private Integer gateId;
    private String gateName;
    private Integer carHistoryType;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime carHistoryTime;
    private String carHistoryRemark;
}
