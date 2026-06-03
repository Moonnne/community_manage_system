package edu.tut.cms.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskUpdateDTO {
    @NotNull
    private Long id;
    private String taskTitle;
    private String taskInfo;
    private Integer taskType;
    private Integer taskStatus;
    private Long taskOperatorId;
    private String taskRemark;
}
