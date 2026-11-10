package com.dazzle.asklepios.service;

import com.dazzle.asklepios.attachments.AttachmentProperties;
import com.dazzle.asklepios.domain.MobileAppConfig;
import com.dazzle.asklepios.domain.enumeration.MobileAppConfigKey;
import com.dazzle.asklepios.domain.enumeration.MobileAppConfigType;
import com.dazzle.asklepios.repository.MobileAppConfigRepository;
import com.dazzle.asklepios.service.dto.mobileAppConfig.MobileAppConfigCreateDTO;
import com.dazzle.asklepios.service.dto.mobileAppConfig.MobileAppConfigUpdateDTO;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import com.dazzle.asklepios.web.rest.vm.mobileAppConfig.MobileAppConfigValueVM;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class MobileAppConfigService {

    private static final Logger LOG = LoggerFactory.getLogger(MobileAppConfigService.class);
    private static final String ENTITY_NAME = "mobileAppConfig";
    private static final int MAX_VALUE_LENGTH = 4000;
    private static final int MAX_DESCRIPTION_LENGTH = 1000;

    private final MobileAppConfigRepository repository;
    private final AttachmentStorageService storage;
    private final AttachmentProperties attachmentProperties;
    private final ObjectMapper objectMapper;

    public MobileAppConfigService(
            MobileAppConfigRepository repository,
            AttachmentStorageService storage,
            AttachmentProperties attachmentProperties,
            ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.storage = storage;
        this.attachmentProperties = attachmentProperties;
        this.objectMapper = objectMapper;
    }

    public MobileAppConfig create(MobileAppConfigCreateDTO dto) {
        LOG.debug("Request to create MobileAppConfig : {}", dto);
        if (dto.configType() == MobileAppConfigType.IMAGE) {
            throw new BadRequestAlertException(
                    "Upload an image for this type", ENTITY_NAME, "imagerequired");
        }
        String value = normalizeValue(dto.configValue());

        if (repository.existsByConfigKey(dto.configKey())) {
            throw new BadRequestAlertException(
                    "A value already exists for this key", ENTITY_NAME, "keyexists");
        }

        MobileAppConfig entity = MobileAppConfig.builder()
                .configKey(dto.configKey())
                .configType(dto.configType())
                .configValue(value)
                .description(normalizeDescription(dto.description()))
                .build();

        return repository.save(entity);
    }

    public MobileAppConfig update(Long id, MobileAppConfigUpdateDTO dto) {
        LOG.debug("Request to update MobileAppConfig id={} with {}", id, dto);
        if (dto.configType() == MobileAppConfigType.IMAGE) {
            throw new BadRequestAlertException(
                    "Upload an image for this type", ENTITY_NAME, "imagerequired");
        }
        MobileAppConfig entity = getById(id);
        entity.setConfigType(dto.configType());
        entity.setConfigValue(normalizeValue(dto.configValue()));
        entity.setDescription(normalizeDescription(dto.description()));
        return repository.save(entity);
    }

    public MobileAppConfig createImage(MobileAppConfigKey configKey, MultipartFile[] files, String description) {
        LOG.debug("Request to create MobileAppConfig image key={}", configKey);
        if (configKey == null) {
            throw new BadRequestAlertException("Key is required", ENTITY_NAME, "keyrequired");
        }
        if (repository.existsByConfigKey(configKey)) {
            throw new BadRequestAlertException(
                    "A value already exists for this key", ENTITY_NAME, "keyexists");
        }
        List<String> storageKeys = storeImages(configKey, files);
        if (storageKeys.isEmpty()) {
            throw new BadRequestAlertException("At least one image is required", ENTITY_NAME, "fileempty");
        }
        MobileAppConfig entity = MobileAppConfig.builder()
                .configKey(configKey)
                .configType(MobileAppConfigType.IMAGE)
                .configValue(writeStorageKeys(storageKeys))
                .description(normalizeDescription(description))
                .build();
        return repository.save(entity);
    }

    public MobileAppConfig updateImage(
            Long id,
            MultipartFile[] files,
            List<String> keptValues,
            String description
    ) {
        LOG.debug("Request to update MobileAppConfig image id={}", id);
        MobileAppConfig entity = getById(id);
        entity.setConfigType(MobileAppConfigType.IMAGE);

        List<String> nextKeys = new ArrayList<>(normalizeKeptValues(keptValues));
        nextKeys.addAll(storeImages(entity.getConfigKey(), files));
        if (nextKeys.isEmpty()) {
            throw new BadRequestAlertException("At least one image is required", ENTITY_NAME, "fileempty");
        }
        entity.setConfigValue(writeStorageKeys(nextKeys));
        entity.setDescription(normalizeDescription(description));
        return repository.save(entity);
    }

    public void delete(Long id) {
        LOG.debug("Request to delete MobileAppConfig id={}", id);
        if (!repository.existsById(id)) {
            throw new NotFoundAlertException(
                    "Mobile app configuration not found: " + id, ENTITY_NAME, "notfound");
        }
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<MobileAppConfig> findAll() {
        return repository.findAllByOrderByConfigKeyAsc();
    }

    @Transactional(readOnly = true)
    public List<MobileAppConfigValueVM> findValues(List<MobileAppConfigKey> keys) {
        List<MobileAppConfigKey> requested = normalizeKeys(keys);
        Map<MobileAppConfigKey, MobileAppConfig> found = repository.findByConfigKeyIn(requested).stream()
                .collect(Collectors.toMap(
                        MobileAppConfig::getConfigKey,
                        Function.identity(),
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
        return requested.stream()
                .map(key -> {
                    MobileAppConfig config = found.get(key);
                    if (config == null) {
                        return new MobileAppConfigValueVM(key, null, null, null);
                    }
                    return MobileAppConfigValueVM.ofEntity(config, resolveValues(config));
                })
                .toList();
    }

    private static List<MobileAppConfigKey> normalizeKeys(List<MobileAppConfigKey> keys) {
        if (keys == null || keys.isEmpty()) {
            throw new BadRequestAlertException("At least one key is required", ENTITY_NAME, "keysrequired");
        }
        List<MobileAppConfigKey> requested = new ArrayList<>();
        for (MobileAppConfigKey key : keys) {
            if (key != null && !requested.contains(key)) {
                requested.add(key);
            }
        }
        if (requested.isEmpty()) {
            throw new BadRequestAlertException("At least one key is required", ENTITY_NAME, "keysrequired");
        }
        return requested;
    }

    private MobileAppConfig getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException(
                        "Mobile app configuration not found: " + id, ENTITY_NAME, "notfound"));
    }

    /**
     * TEXT: first/only value as string (admin UI). IMAGE: first public URL, or null when empty.
     */
    public String resolveValue(MobileAppConfig config) {
        List<String> values = resolveValues(config);
        if (values == null || values.isEmpty()) {
            return config != null && config.getConfigType() == MobileAppConfigType.IMAGE ? null : "";
        }
        return values.get(0);
    }

    /**
     * Always a list: TEXT → one-element list; IMAGE → public URLs for all stored images.
     */
    public List<String> resolveValues(MobileAppConfig config) {
        if (config == null || config.getConfigValue() == null) {
            return List.of();
        }
        if (config.getConfigType() == MobileAppConfigType.IMAGE) {
            return readStorageKeys(config.getConfigValue()).stream()
                    .map(this::toPublicUrl)
                    .toList();
        }
        String value = config.getConfigValue().trim();
        return value.isEmpty() ? List.of() : List.of(value);
    }

    private List<String> storeImages(MobileAppConfigKey key, MultipartFile[] files) {
        List<String> storageKeys = new ArrayList<>();
        if (files == null) {
            return storageKeys;
        }
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                storageKeys.add(storeImage(key, file));
            }
        }
        return storageKeys;
    }

    private String storeImage(MobileAppConfigKey key, MultipartFile file) {
        String mime = file.getContentType();
        if (mime == null || !mime.startsWith("image/")) {
            throw new BadRequestAlertException("Only image files are allowed", ENTITY_NAME, "invalidmime");
        }
        long maxBytes = attachmentProperties.getMaxBytes();
        if (maxBytes > 0 && file.getSize() > maxBytes) {
            throw new BadRequestAlertException("Image file is too large", ENTITY_NAME, "filetoolarge");
        }

        String originalName = file.getOriginalFilename();
        String extension = extensionOf(originalName);
        int nameHash = originalName == null ? 0 : Math.abs(originalName.hashCode());
        String storageKey = "mobile-app-config/" + key.name() + "/" + key.name().toLowerCase()
                + "-" + System.currentTimeMillis() + "-" + nameHash
                + extension;
        try {
            storage.putPublic(storageKey, mime, file.getSize(), file.getInputStream());
        } catch (Exception ex) {
            LOG.error("Failed to upload mobile app config image for key {}", key, ex);
            throw new BadRequestAlertException(
                    "Failed to upload image", ENTITY_NAME, "uploadfailed");
        }
        return storageKey;
    }

    private List<String> readStorageKeys(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        String value = raw.trim();
        if (value.startsWith("[")) {
            try {
                List<String> keys = objectMapper.readValue(value, new TypeReference<>() {});
                return keys == null ? List.of() : keys.stream()
                        .filter(item -> item != null && !item.isBlank())
                        .map(String::trim)
                        .toList();
            } catch (JsonProcessingException ex) {
                LOG.warn("Invalid IMAGE config_value JSON, treating as single key: {}", value);
            }
        }
        return List.of(value);
    }

    private String writeStorageKeys(List<String> keys) {
        try {
            return objectMapper.writeValueAsString(keys);
        } catch (JsonProcessingException ex) {
            throw new BadRequestAlertException("Failed to store image list", ENTITY_NAME, "serializefailed");
        }
    }

    private List<String> normalizeKeptValues(List<String> keptValues) {
        if (keptValues == null || keptValues.isEmpty()) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (String kept : keptValues) {
            if (kept == null || kept.isBlank()) {
                continue;
            }
            String storageKey = toStorageKey(kept.trim());
            if (!storageKey.isBlank() && !result.contains(storageKey)) {
                result.add(storageKey);
            }
        }
        return result;
    }

    private String toStorageKey(String valueOrUrl) {
        if (valueOrUrl.startsWith("http://") || valueOrUrl.startsWith("https://")) {
            String base = publicBaseUrl();
            if (!base.isEmpty() && valueOrUrl.startsWith(base + "/")) {
                return valueOrUrl.substring(base.length() + 1);
            }
            int idx = valueOrUrl.indexOf("mobile-app-config/");
            if (idx >= 0) {
                return valueOrUrl.substring(idx);
            }
        }
        return valueOrUrl;
    }

    private String toPublicUrl(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            return "";
        }
        String value = storageKey.trim();
        if (value.startsWith("http://") || value.startsWith("https://")) {
            return value;
        }
        String base = publicBaseUrl();
        if (base.isEmpty()) {
            return value;
        }
        return base + "/" + value;
    }

    private String publicBaseUrl() {
        String cdn = attachmentProperties.getCdnEndpoint();
        if (cdn != null && !cdn.isBlank()) {
            return cdn.replaceAll("/$", "");
        }
        String endpoint = attachmentProperties.getEndpoint();
        String bucket = attachmentProperties.getBucket();
        if (endpoint == null || endpoint.isBlank() || bucket == null || bucket.isBlank()) {
            return "";
        }
        String host = endpoint.replaceFirst("^https://", "").replaceFirst("^http://", "").replaceAll("/$", "");
        return "https://" + bucket + "." + host;
    }

    private static String extensionOf(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }

    private static String normalizeValue(String configValue) {
        String value = configValue == null ? "" : configValue.trim();
        if (value.isEmpty()) {
            throw new BadRequestAlertException("Value is required", ENTITY_NAME, "valuerequired");
        }
        if (value.length() > MAX_VALUE_LENGTH) {
            throw new BadRequestAlertException(
                    "Value must be at most " + MAX_VALUE_LENGTH + " characters",
                    ENTITY_NAME,
                    "valuetoolong");
        }
        return value;
    }

    private static String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }
        String value = description.trim();
        if (value.isEmpty()) {
            return null;
        }
        if (value.length() > MAX_DESCRIPTION_LENGTH) {
            throw new BadRequestAlertException(
                    "Description must be at most " + MAX_DESCRIPTION_LENGTH + " characters",
                    ENTITY_NAME,
                    "descriptiontoolong");
        }
        return value;
    }
}
