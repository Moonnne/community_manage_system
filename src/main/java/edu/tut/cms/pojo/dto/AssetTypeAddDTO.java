package edu.tut.cms.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetTypeAddDTO {
    @NotNull
    private Integer assetTypeId;
    @NotBlank
    private String assetTypeName;
    @NotBlank
    private String assetTypeShort;
}
