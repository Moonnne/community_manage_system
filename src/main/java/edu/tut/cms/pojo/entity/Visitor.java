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
public class Visitor {
    private Long id;
    private String visitorCode;
    private Long assetId;
    private Long ownerId;
    private Long visitorCreater;
    private String visitorName;
    private String visitorPhone;
    private Integer visitorType;
    private String visitorStatus;
    private LocalDateTime visitorCreateTime;
    private LocalDateTime visitorFinishTime;
    private String visitorRemark;
}
