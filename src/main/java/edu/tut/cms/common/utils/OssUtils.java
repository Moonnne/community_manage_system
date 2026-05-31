package edu.tut.cms.common.utils;

import edu.tut.cms.common.config.OssProperties;
import edu.tut.cms.common.exception.BusinessException;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OssUtils {

    private static final long MAX_AVATAR_SIZE = 5L * 1024L * 1024L;

    private final MinioClient minioClient;
    private final OssProperties ossProperties;

    public String uploadAvatar(MultipartFile file) {
        validateAvatar(file);

        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType)) {
            contentType = "image/png";
        }

        String objectName = normalizeDir(ossProperties.getAvatarDir())
                + "/" + UUID.randomUUID() + extensionOf(contentType);

        try {
            ensureBucket();
            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(ossProperties.getBucket())
                        .object(objectName)
                        .stream(inputStream, file.getSize(), -1L)
                        .contentType(contentType)
                        .build());
            }
            return buildObjectUrl(objectName);
        } catch (Exception e) {
            throw new BusinessException("头像上传失败");
        }
    }

    private void validateAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("头像文件不能为空");
        }
        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new BusinessException("头像文件不能超过 5MB");
        }
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !contentType.startsWith("image/")) {
            throw new BusinessException("只能上传图片格式的头像");
        }
    }

    private void ensureBucket() throws Exception {
        String bucket = ossProperties.getBucket();
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(bucket)
                .build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucket)
                    .build());
        }
    }

    private String buildObjectUrl(String objectName) {
        String baseUrl = StringUtils.hasText(ossProperties.getPublicUrl())
                ? ossProperties.getPublicUrl()
                : ossProperties.getEndpoint();
        return trimTrailingSlash(baseUrl) + "/" + ossProperties.getBucket() + "/" + objectName;
    }

    private String normalizeDir(String dir) {
        if (!StringUtils.hasText(dir)) {
            return "avatas";
        }
        String normalized = dir.replace("\\", "/");
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return StringUtils.hasText(normalized) ? normalized : "avatas";
    }

    private String trimTrailingSlash(String value) {
        String result = value == null ? "" : value;
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    private String extensionOf(String contentType) {
        return switch (contentType) {
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            default -> ".png";
        };
    }
}
