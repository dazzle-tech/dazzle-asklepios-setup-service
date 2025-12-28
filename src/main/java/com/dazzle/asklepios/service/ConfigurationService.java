// src/main/java/com/dazzle/asklepios/service/ConfigurationService.java
package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Configuration;
import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.enumeration.ConfigurationKeys;
import com.dazzle.asklepios.domain.enumeration.ConfigurationReferenceType;
import com.dazzle.asklepios.domain.enumeration.ConfigurationValueType;
import com.dazzle.asklepios.repository.ConfigurationRepository;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.configuration.ConfigurationCreateVM;
import com.dazzle.asklepios.web.rest.vm.configuration.ConfigurationUpdateVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ConfigurationService {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationService.class);

    private final ConfigurationRepository configurationRepository;
    private final FacilityRepository facilityRepository;

    public ConfigurationService(
            ConfigurationRepository configurationRepository,
            FacilityRepository facilityRepository
    ) {
        this.configurationRepository = configurationRepository;
        this.facilityRepository = facilityRepository;
    }

    public Configuration create(ConfigurationCreateVM vm) {
        LOG.debug("Request to create Configuration : {}", vm);

        Facility facility = null;
        if (vm.facilityId() != null) {
            facility = facilityRepository.findById(vm.facilityId())
                    .orElseThrow(() -> new BadRequestAlertException(
                            "Facility not found with id " + vm.facilityId(),
                            "facility",
                            "notfound"
                    ));
        }

        Configuration configuration = Configuration.builder()
                .facility(facility)
                .key(vm.key())
                .value(vm.value())
                .valueType(vm.valueType())
                .referenceType(vm.referenceType())
                .description(vm.description())
                .isActive(Boolean.TRUE.equals(vm.isActive()))
                .build();

        return configurationRepository.save(configuration);
    }

    public Optional<Configuration> update(Long id, ConfigurationUpdateVM vm) {
        LOG.debug("Request to update Configuration id={} with {}", id, vm);

        Configuration configuration = configurationRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Configuration not found with id " + id,
                        "configuration",
                        "notfound"
                ));

        Facility facility = null;
        if (vm.facilityId() != null) {
            facility = facilityRepository.findById(vm.facilityId())
                    .orElseThrow(() -> new BadRequestAlertException(
                            "Facility not found with id " + vm.facilityId(),
                            "facility",
                            "notfound"
                    ));
        }

        if (vm.facilityId() != null) configuration.setFacility(facility);
        if (vm.key() != null) configuration.setKey(vm.key());
        if (vm.value() != null) configuration.setValue(vm.value());
        if (vm.description() != null) configuration.setDescription(vm.description());
        if (vm.referenceType() != null) configuration.setReferenceType(vm.referenceType());
        if (vm.valueType() != null) configuration.setValueType(vm.valueType());
        if (vm.isActive() != null) configuration.setIsActive(vm.isActive());

        Configuration updated = configurationRepository.save(configuration);
        LOG.debug("Updated configuration: {}", updated);

        return Optional.of(updated);
    }

    @Transactional(readOnly = true)
    public Optional<Configuration> findByKeyAndFacility(ConfigurationKeys key, Long facilityId) {
        LOG.debug("Request to get Configuration by key={} and facilityId={}", key, facilityId);

        if (facilityId == null) {
            return configurationRepository.findByKeyAndFacilityIsNullAndIsActiveTrue(key);
        }

        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Facility not found with id " + facilityId,
                        "facility",
                        "notfound"
                ));

        return configurationRepository.findByKeyAndFacilityAndIsActiveTrue(key, facility);
    }


    @Transactional(readOnly = true)
    public Optional<Configuration> findOne(Long id) {
        LOG.debug("Request to get Configuration : {}", id);
        return configurationRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Page<Configuration> findAll(Pageable pageable) {
        LOG.debug("Request to get all Configuration ");
        return configurationRepository.findAll(pageable);
    }

    public Page<Configuration> quickSearch(String searchText, Pageable pageable) {
        LOG.debug("Request to quick search for Configuration contain text : {} ", searchText);

        if (searchText == null || searchText.isBlank()) {
            return Page.empty(pageable);
        }
        return configurationRepository
                .findByValueContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrKeyContainingIgnoreCase(
                        searchText, searchText, searchText, pageable
                );
    }

    public Page<Configuration> filterByValueType(ConfigurationValueType valueType, Pageable pageable) {
        LOG.debug("Request to filterByValueType for Configuration ");
        return configurationRepository.findAllByValueType(valueType, pageable);
    }

    public Page<Configuration> filterByReferenceType(ConfigurationReferenceType referenceType, Pageable pageable) {
        LOG.debug("Request to filterByReferenceType for Configuration ");
        return configurationRepository.findAllByReferenceType(referenceType, pageable);
    }

}
