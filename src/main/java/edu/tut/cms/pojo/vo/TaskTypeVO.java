package edu.tut.cms.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskTypeVO {
    private Integer taskTypeId;
    private String taskTypeName;
    private String taskTypeRemark;
}
