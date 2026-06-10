package edu.tut.cms.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentChatDTO {

    private String message;

    private String currentPath;

    private List<AgentMessageDTO> history;
}
