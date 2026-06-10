package edu.tut.cms.server.controller;

import edu.tut.cms.common.result.Result;
import edu.tut.cms.pojo.dto.AgentChatDTO;
import edu.tut.cms.pojo.dto.AgentExecuteDTO;
import edu.tut.cms.pojo.vo.AgentActionVO;
import edu.tut.cms.server.service.AgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody AgentChatDTO dto) {
        return agentService.chat(dto);
    }

    @PostMapping("/execute")
    public Result<AgentActionVO> execute(@RequestBody AgentExecuteDTO dto) {
        return Result.success(agentService.execute(dto));
    }
}
