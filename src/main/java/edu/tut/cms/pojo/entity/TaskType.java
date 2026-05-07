package edu.tut.cms.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskType {
    private Integer taskTypeId;
    private String taskTypeName;
    private String taskTypeRemark;
}
