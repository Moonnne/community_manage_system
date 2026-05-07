package edu.tut.cms.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskAddDTO {
    @NotBlank
    private String taskTitle;
    private String taskInfo;
    @NotNull
    private Integer taskType;
    private String taskRemark;
}
