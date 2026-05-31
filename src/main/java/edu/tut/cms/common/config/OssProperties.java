package edu.tut.cms.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "minio")
public class OssProperties {

    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket = "cms";
    private String avatarDir = "avatas";
    private String publicUrl;
}
