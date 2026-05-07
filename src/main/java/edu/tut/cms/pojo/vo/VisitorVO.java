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
public class VisitorVO {
    private Long id;
    private String visitorCode;
    private Long assetId;
    private Long ownerId;
    private String ownerName;
    private Long visitorCreater;
    private String createrName;
    private String visitorName;
    private String visitorPhone;
    private Integer visitorType;
    private String visitorTypeName;
    private String visitorStatus;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime visitorCreateTime;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime visitorFinishTime;
    private String visitorRemark;
}
