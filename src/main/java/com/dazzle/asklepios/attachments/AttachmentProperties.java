package com.dazzle.asklepios.attachments;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;

import java.util.Collections;
import java.util.Set;

@Component
@ConfigurationProperties(prefix = "attachments")
public class AttachmentProperties {
    private String bucket="asklepios";
    private Region region= Region.US_EAST_1;
    private String endpoint="https://asklepios.sfo3.digitaloceanspaces.com";
    private int presignExpirySeconds = 300;
    private long maxBytes = 52_428_800L;
    private Set<String> allowed=Set.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "image/jpeg",
            "image/png",
            "text/plain"
    );
    private String accessKeyId="DO00TRNC7U4TLLL6ZAN2";
    private String secretAccessKey="mNFZKBA/BPXTHVUsde8y/2MslOn7zZEiPzwfIuhLrx4";

    // getters/setters

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public int getPresignExpirySeconds() {
        return presignExpirySeconds;
    }

    public void setPresignExpirySeconds(int presignExpirySeconds) {
        this.presignExpirySeconds = presignExpirySeconds;
    }

    public long getMaxBytes() {
        return maxBytes;
    }

    public void setMaxBytes(long maxBytes) {
        this.maxBytes = maxBytes;
    }

    public Set<String> getAllowed() {
        return allowed;
    }

    public void setAllowed(Set<String> allowed) {
        this.allowed = allowed;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getSecretAccessKey() {
        return secretAccessKey;
    }

    public void setSecretAccessKey(String secretAccessKey) {
        this.secretAccessKey = secretAccessKey;
    }
}
