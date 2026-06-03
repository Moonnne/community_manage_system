package edu.tut.cms.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarAddDTO {
    private String carCode;
    private Long ownerId;
    private Long assetId;
    private Long visitorId;
    @NotBlank
    private String carNumber;
    @NotNull
    private Integer carType;
    @NotNull
    private Integer carStatus;
}
