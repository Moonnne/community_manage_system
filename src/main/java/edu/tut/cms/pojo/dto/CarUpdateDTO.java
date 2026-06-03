package edu.tut.cms.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarUpdateDTO {
    @NotNull
    private Long id;
    private Long ownerId;
    private Long assetId;
    private Long visitorId;
    private String carNumber;
    private Integer carType;
    private Integer carStatus;
}
