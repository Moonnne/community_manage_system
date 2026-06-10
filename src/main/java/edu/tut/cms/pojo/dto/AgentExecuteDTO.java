package edu.tut.cms.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentExecuteDTO {

    private String actionName;

    private Map<String, Object> payload;
}
