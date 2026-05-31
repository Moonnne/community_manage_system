package edu.tut.cms.common.utils;

import edu.tut.cms.common.config.OssProperties;
import edu.tut.cms.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class OssUtils {

    private static final long MAX_AVATAR_SIZE = 5L * 1024L * 1024L;
    private static final String REGION = "us-east-1";
    private static final String SERVICE = "s3";
    private static final DateTimeFormatter AMZ_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
    private static final DateTimeFormatter DATE_STAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    private final OssProperties ossProperties;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public String uploadAvatar(MultipartFile file) {
        validateAvatar(file);

        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType)) {
            contentType = "image/png";
        }

        String objectName = normalizeDir(ossProperties.getAvatarDir())
                + "/" + UUID.randomUUID() + extensionOf(contentType);

        try {
            byte[] bytes = file.getBytes();
            String url = buildObjectUrl(objectName);
            HttpRequest request = buildSignedPutRequest(url, objectName, contentType, bytes);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BusinessException("头像上传失败");
            }
            return url;
        } catch (BusinessException e) {
            throw e;
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

    private String buildObjectUrl(String objectName) {
        String baseUrl = StringUtils.hasText(ossProperties.getPublicUrl())
                ? ossProperties.getPublicUrl()
                : ossProperties.getEndpoint();
        return trimTrailingSlash(baseUrl) + "/" + ossProperties.getBucket() + "/" + objectName;
    }

    private HttpRequest buildSignedPutRequest(String url, String objectName, String contentType, byte[] bytes)
            throws Exception {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        String amzDate = AMZ_DATE_FORMATTER.format(now);
        String dateStamp = DATE_STAMP_FORMATTER.format(now);
        String payloadHash = sha256Hex(bytes);
        URI uri = URI.create(url);
        String host = uri.getHost();
        String canonicalUri = "/" + encodePathSegment(ossProperties.getBucket())
                + "/" + encodeObjectName(objectName);
        String signedHeaders = "content-type;host;x-amz-content-sha256;x-amz-date";
        String canonicalHeaders = "content-type:" + contentType + "\n"
                + "host:" + host + "\n"
                + "x-amz-content-sha256:" + payloadHash + "\n"
                + "x-amz-date:" + amzDate + "\n";
        String canonicalRequest = "PUT\n"
                + canonicalUri + "\n"
                + "\n"
                + canonicalHeaders + "\n"
                + signedHeaders + "\n"
                + payloadHash;
        String credentialScope = dateStamp + "/" + REGION + "/" + SERVICE + "/aws4_request";
        String stringToSign = "AWS4-HMAC-SHA256\n"
                + amzDate + "\n"
                + credentialScope + "\n"
                + sha256Hex(canonicalRequest);
        String signature = hmacHex(getSignatureKey(ossProperties.getSecretKey(), dateStamp), stringToSign);
        String authorization = "AWS4-HMAC-SHA256 Credential=" + ossProperties.getAccessKey()
                + "/" + credentialScope
                + ", SignedHeaders=" + signedHeaders
                + ", Signature=" + signature;

        return HttpRequest.newBuilder(uri)
                .PUT(HttpRequest.BodyPublishers.ofByteArray(bytes))
                .header("Authorization", authorization)
                .header("Content-Type", contentType)
                .header("x-amz-content-sha256", payloadHash)
                .header("x-amz-date", amzDate)
                .build();
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

    private String encodeObjectName(String objectName) {
        return Stream.of(objectName.split("/"))
                .map(this::encodePathSegment)
                .collect(Collectors.joining("/"));
    }

    private String encodePathSegment(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private String sha256Hex(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }

    private String sha256Hex(String value) throws Exception {
        return sha256Hex(value.getBytes(StandardCharsets.UTF_8));
    }

    private byte[] hmac(byte[] key, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    private String hmacHex(byte[] key, String data) throws Exception {
        return HexFormat.of().formatHex(hmac(key, data));
    }

    private byte[] getSignatureKey(String secretKey, String dateStamp) throws Exception {
        byte[] kDate = hmac(("AWS4" + secretKey).getBytes(StandardCharsets.UTF_8), dateStamp);
        byte[] kRegion = hmac(kDate, REGION);
        byte[] kService = hmac(kRegion, SERVICE);
        return hmac(kService, "aws4_request");
    }
}
