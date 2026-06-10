package edu.tut.cms.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentActionVO {

    private String type;

    private String actionName;

    private String title;

    private String summary;

    private String routePath;

    private Boolean readOnly;

    private Boolean requiresConfirmation;

    private Map<String, Object> payload;

    private Object data;
}
