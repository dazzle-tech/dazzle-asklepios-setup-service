package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.MobileAppConfig;
import com.dazzle.asklepios.domain.enumeration.MobileAppConfigKey;
import com.dazzle.asklepios.repository.MobileAppConfigRepository;
import com.dazzle.asklepios.service.dto.mobileAppConfig.MobileAppConfigCreateDTO;
import com.dazzle.asklepios.service.dto.mobileAppConfig.MobileAppConfigUpdateDTO;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MobileAppConfigService {

    private static final Logger LOG = LoggerFactory.getLogger(MobileAppConfigService.class);
    private static final String ENTITY_NAME = "mobileAppConfig";
    private static final int MAX_VALUE_LENGTH = 4000;

    private final MobileAppConfigRepository repository;

    public MobileAppConfigService(MobileAppConfigRepository repository) {
        this.repository = repository;
    }

    public MobileAppConfig create(MobileAppConfigCreateDTO dto) {
        LOG.debug("Request to create MobileAppConfig : {}", dto);
        String value = normalizeValue(dto.configValue());

        if (repository.existsByConfigKey(dto.configKey())) {
            throw new BadRequestAlertException(
                    "A value already exists for this key", ENTITY_NAME, "keyexists");
        }

        MobileAppConfig entity = MobileAppConfig.builder()
                .configKey(dto.configKey())
                .configValue(value)
                .build();

        return repository.save(entity);
    }

    public MobileAppConfig update(Long id, MobileAppConfigUpdateDTO dto) {
        LOG.debug("Request to update MobileAppConfig id={} with {}", id, dto);
        MobileAppConfig entity = getById(id);
        entity.setConfigValue(normalizeValue(dto.configValue()));
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
    public MobileAppConfig findByKey(MobileAppConfigKey configKey) {
        return repository.findByConfigKey(configKey)
                .orElseThrow(() -> new NotFoundAlertException(
                        "No value is configured for key " + configKey, ENTITY_NAME, "keynotfound"));
    }

    private MobileAppConfig getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException(
                        "Mobile app configuration not found: " + id, ENTITY_NAME, "notfound"));
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
