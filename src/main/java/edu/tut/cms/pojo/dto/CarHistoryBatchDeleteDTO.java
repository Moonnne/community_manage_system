package edu.tut.cms.pojo.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarHistoryBatchDeleteDTO {
    @NotEmpty
    private List<Long> ids;
}
