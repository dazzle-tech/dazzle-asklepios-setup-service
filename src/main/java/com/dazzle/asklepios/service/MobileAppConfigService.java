package com.dazzle.asklepios.service;

import com.dazzle.asklepios.attachments.AttachmentProperties;
import com.dazzle.asklepios.domain.MobileAppConfig;
import com.dazzle.asklepios.domain.enumeration.MobileAppConfigKey;
import com.dazzle.asklepios.domain.enumeration.MobileAppConfigType;
import com.dazzle.asklepios.repository.MobileAppConfigRepository;
import com.dazzle.asklepios.service.dto.mobileAppConfig.MobileAppConfigCreateDTO;
import com.dazzle.asklepios.service.dto.mobileAppConfig.MobileAppConfigUpdateDTO;
import com.dazzle.asklepios.web.rest.vm.mobileAppConfig.MobileAppConfigValueVM;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
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

    private final MobileAppConfigRepository repository;
    private final AttachmentStorageService storage;
    private final AttachmentProperties attachmentProperties;

    public MobileAppConfigService(
            MobileAppConfigRepository repository,
            AttachmentStorageService storage,
            AttachmentProperties attachmentProperties
    ) {
        this.repository = repository;
        this.storage = storage;
        this.attachmentProperties = attachmentProperties;
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
        return repository.save(entity);
    }

    public MobileAppConfig createImage(MobileAppConfigKey configKey, MultipartFile file) {
        LOG.debug("Request to create MobileAppConfig image key={}", configKey);
        if (configKey == null) {
            throw new BadRequestAlertException("Key is required", ENTITY_NAME, "keyrequired");
        }
        if (repository.existsByConfigKey(configKey)) {
            throw new BadRequestAlertException(
                    "A value already exists for this key", ENTITY_NAME, "keyexists");
        }
        String storageKey = storeImage(configKey, file);
        MobileAppConfig entity = MobileAppConfig.builder()
                .configKey(configKey)
                .configType(MobileAppConfigType.IMAGE)
                .configValue(storageKey)
                .build();
        return repository.save(entity);
    }

    public MobileAppConfig updateImage(Long id, MultipartFile file) {
        LOG.debug("Request to update MobileAppConfig image id={}", id);
        MobileAppConfig entity = getById(id);
        entity.setConfigType(MobileAppConfigType.IMAGE);
        entity.setConfigValue(storeImage(entity.getConfigKey(), file));
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
                        return new MobileAppConfigValueVM(key, null, null);
                    }
                    return MobileAppConfigValueVM.ofEntity(config, resolveValue(config));
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

    public String resolveValue(MobileAppConfig config) {
        if (config == null || config.getConfigValue() == null) {
            return "";
        }
        String value = config.getConfigValue().trim();
        if (value.isEmpty()) {
            return "";
        }
        if (value.startsWith("http://") || value.startsWith("https://")) {
            return value;
        }
        if (config.getConfigType() == MobileAppConfigType.IMAGE) {
            String base = publicBaseUrl();
            if (base.isEmpty()) {
                return value;
            }
            return base + "/" + value;
        }
        return value;
    }

    private String storeImage(MobileAppConfigKey key, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestAlertException("Image file is required", ENTITY_NAME, "fileempty");
        }
        String mime = file.getContentType();
        if (mime == null || !mime.startsWith("image/")) {
            throw new BadRequestAlertException("Only image files are allowed", ENTITY_NAME, "invalidmime");
        }
        long maxBytes = attachmentProperties.getMaxBytes();
        if (maxBytes > 0 && file.getSize() > maxBytes) {
            throw new BadRequestAlertException("Image file is too large", ENTITY_NAME, "filetoolarge");
        }

        String extension = extensionOf(file.getOriginalFilename());
        String storageKey = "mobile-app-config/" + key.name() + "/" + key.name().toLowerCase()
                + "-" + System.currentTimeMillis() + extension;
        try {
            storage.putPublic(storageKey, mime, file.getSize(), file.getInputStream());
        } catch (Exception ex) {
            LOG.error("Failed to upload mobile app config image for key {}", key, ex);
            throw new BadRequestAlertException(
                    "Failed to upload image", ENTITY_NAME, "uploadfailed");
        }
        return storageKey;
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
}
