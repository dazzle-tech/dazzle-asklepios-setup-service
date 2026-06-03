package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.SystemConfiguration;
import com.dazzle.asklepios.domain.enumeration.SystemConfigKey;
import com.dazzle.asklepios.repository.SystemConfigurationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SystemConfigurationService {
    private static final Logger LOG =
            LoggerFactory.getLogger(SystemConfigurationService.class);
    private static final String CDN_BASE_URL = "https://asklepios.sfo3.cdn.digitaloceanspaces.com/";

    private final AttachmentStorageService storage;
    private final SystemConfigurationRepository repository;

    public Map<SystemConfigKey, String> getAll() {
        LOG.info("Loading all system configurations");

        Map<SystemConfigKey, String> configs = repository.findAll()
                .stream()
                .filter(config -> config.getConfigKey() != null)
                .collect(Collectors.toMap(
                        SystemConfiguration::getConfigKey,
                        this::resolveValue
                ));

        LOG.info("Loaded {} system configurations", configs.size());

        return configs;
    }

    private String resolveValue(SystemConfiguration config) {
        String value = config.getConfigValue();

        if (value == null || value.isBlank()) {
            return "";
        }

        if (value.startsWith("http://") || value.startsWith("https://")) {
            return value;
        }

        if (config.getConfigKey() == SystemConfigKey.FAVICON ||
                config.getConfigKey() == SystemConfigKey.SYSTEM_LOGO ||
                config.getConfigKey() == SystemConfigKey.LOGIN_BACKGROUND) {

            String resolvedUrl = CDN_BASE_URL + value;

            LOG.debug(
                    "Resolved image url for [{}]: {}",
                    config.getConfigKey(),
                    resolvedUrl
            );

            return resolvedUrl;
        }

        return value;
    }

    public String getValue(SystemConfigKey key) {
        LOG.info("Loading system configuration: {}", key);

        String value = repository.findByConfigKey(key)
                .map(SystemConfiguration::getConfigValue)
                .orElse(null);

        LOG.info("Loaded system configuration [{}]", key);

        return value;
    }

    public List<SystemConfiguration> getAllDetails() {
        LOG.info("Loading system configuration details");

        List<SystemConfiguration> details = repository.findAllByOrderByConfigKeyAsc();

        LOG.info("Loaded {} system configuration details", details.size());

        return details;
    }

    public SystemConfiguration updateValue(SystemConfigKey key, String value) {
        LOG.info("Updating system configuration [{}] with value [{}]", key, value);

        SystemConfiguration config = repository.findByConfigKey(key)
                .orElseThrow(() -> {
                    LOG.warn("System configuration not found: {}", key);
                    return new RuntimeException("System config not found: " + key);
                });

        config.setConfigValue(value);

        SystemConfiguration saved = repository.save(config);

        LOG.info("System configuration [{}] updated successfully", key);

        return saved;
    }

    public List<SystemConfiguration> updateMany(Map<SystemConfigKey, String> values) {
        LOG.info("Updating {} system configurations", values.size());

        List<SystemConfiguration> result = values.entrySet()
                .stream()
                .map(entry -> updateValue(entry.getKey(), entry.getValue()))
                .toList();

        LOG.info("Bulk system configuration update completed successfully");

        return result;
    }

    public SystemConfiguration uploadImage(SystemConfigKey key, MultipartFile file) {
        LOG.info("Uploading image for system configuration key [{}]", key);

        if (file == null || file.isEmpty()) {
            LOG.warn("Upload failed for [{}]: file is empty", key);
            throw new RuntimeException("File is empty");
        }

        String mime = file.getContentType();

        LOG.info(
                "Received system config image: key={}, fileName={}, size={} bytes, mime={}",
                key,
                file.getOriginalFilename(),
                file.getSize(),
                mime
        );

        if (mime == null || !mime.startsWith("image/")) {
            LOG.warn("Upload failed for [{}]: invalid mime type [{}]", key, mime);
            throw new RuntimeException("Only image files are allowed");
        }

        SystemConfiguration config = repository.findByConfigKey(key)
                .orElseThrow(() -> {
                    LOG.warn("System configuration not found while uploading image: {}", key);
                    return new RuntimeException("System config not found: " + key);
                });

        String extension = getExtension(file.getOriginalFilename());
        String storageKey = buildSystemConfigImageKey(key, extension);

        LOG.info("Generated storage key for [{}]: {}", key, storageKey);

        try {
            storage.putPublic(
                    storageKey,
                    mime,
                    file.getSize(),
                    file.getInputStream()
            );

            LOG.info("Image uploaded successfully to storage: {}", storageKey);

        } catch (Exception e) {
            LOG.error("Failed to upload image for system configuration key [{}]", key, e);
            throw new RuntimeException("Failed to upload system config image", e);
        }

        config.setConfigValue(storageKey);

        SystemConfiguration saved = repository.save(config);

        LOG.info(
                "System configuration [{}] updated with image key [{}]",
                key,
                storageKey
        );

        return saved;
    }

    private String buildSystemConfigImageKey(SystemConfigKey key, String extension) {
        long timestamp = System.currentTimeMillis();

        if (key == SystemConfigKey.FAVICON) {
            return "config/FAVICON/favicon-" + timestamp + extension;
        }

        if (key == SystemConfigKey.SYSTEM_LOGO) {
            return "config/SYSTEM_LOGO/logo-" + timestamp + extension;
        }

        if (key == SystemConfigKey.LOGIN_BACKGROUND) {
            return "config/LOGIN_BACKGROUND/background-" + timestamp + extension;
        }

        return "config/" + key.name() + "/" + key.name().toLowerCase() + "-" + timestamp + extension;
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }

        return filename.substring(filename.lastIndexOf("."));
    }
}