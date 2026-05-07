package edu.tut.cms.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VisitorAddDTO {
    @NotBlank
    private String visitorName;
    private String visitorPhone;
    @NotNull
    private Integer visitorType;
    private Long assetId;
    private Long ownerId;
    private String visitorRemark;
}
