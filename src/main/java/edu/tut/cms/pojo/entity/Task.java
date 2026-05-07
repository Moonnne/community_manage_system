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
public class Task {
    private Long id;
    private String taskCode;
    private String taskTitle;
    private String taskInfo;
    private Long ownerId;
    private Long taskOperatorId;
    private Integer taskStatus;
    private Integer taskType;
    private LocalDateTime taskCreateTime;
    private LocalDateTime taskUpdateTime;
    private LocalDateTime taskFinishTime;
    private String taskRemark;
}
