package edu.tut.cms.server.service;

import edu.tut.cms.pojo.dto.AgentChatDTO;
import edu.tut.cms.pojo.dto.AgentExecuteDTO;
import edu.tut.cms.pojo.vo.AgentActionVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface AgentService {

    SseEmitter chat(AgentChatDTO dto);

    AgentActionVO execute(AgentExecuteDTO dto);
}
