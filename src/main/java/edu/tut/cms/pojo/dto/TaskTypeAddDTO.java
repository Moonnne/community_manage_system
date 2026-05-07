package edu.tut.cms.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskTypeAddDTO {
    @NotBlank
    private String taskTypeName;
    private String taskTypeRemark;
}
