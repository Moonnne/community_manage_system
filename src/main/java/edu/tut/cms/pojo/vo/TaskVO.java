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
public class TaskVO {
    private Long id;
    private String taskCode;
    private String taskTitle;
    private String taskInfo;
    private Long ownerId;
    private String ownerName;
    private Long taskOperatorId;
    private String operatorName;
    private Integer taskStatus;
    private Integer taskType;
    private String taskTypeName;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime taskCreateTime;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime taskUpdateTime;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime taskFinishTime;
    private String taskRemark;
}
