package com.dazzle.asklepios.attachments;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;

import java.util.Set;

@Component
@Data
@ConfigurationProperties(prefix = "setup.attachments")
public class AttachmentProperties {
    private String bucket = "asklepios";
    private Region region = Region.US_EAST_1;
    private String endpoint = "https://sfo3.digitaloceanspaces.com";
    private int presignExpirySeconds = 300;
    private long maxBytes = 52_428_800L;
    private java.util.Set<String> allowed = Set.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "image/jpeg", "image/png", "text/plain");
    private String accessKeyId;
    private String secretAccessKey;
}
