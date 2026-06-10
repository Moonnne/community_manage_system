package edu.tut.cms.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "cms.agent")
public class DeepSeekAgentProperties {

    private int pageSize = 5;

    private int maxHistoryMessages = 8;
}
