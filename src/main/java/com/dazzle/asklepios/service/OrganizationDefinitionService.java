package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Language;
import com.dazzle.asklepios.domain.OrganizationDefinition;
import com.dazzle.asklepios.domain.enumeration.DayOfWeek;
import com.dazzle.asklepios.repository.LanguageRepository;
import com.dazzle.asklepios.repository.OrganizationDefinitionRepository;
import com.dazzle.asklepios.service.dto.workingDay.WorkingDayJson;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import com.dazzle.asklepios.web.rest.vm.organizationDefinition.OrganizationDefinitionCreateVM;
import com.dazzle.asklepios.web.rest.vm.organizationDefinition.OrganizationDefinitionUpdateVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrganizationDefinitionService {

    private static final Logger LOG = LoggerFactory.getLogger(OrganizationDefinitionService.class);

    private final OrganizationDefinitionRepository organizationDefinitionRepository;
    private final LanguageRepository languageRepository;

    public OrganizationDefinitionService(
            OrganizationDefinitionRepository organizationDefinitionRepository,
            LanguageRepository languageRepository
    ) {
        this.organizationDefinitionRepository = organizationDefinitionRepository;
        this.languageRepository = languageRepository;
    }

    public OrganizationDefinition create(OrganizationDefinitionCreateVM vm) {
        LOG.debug("Request to create OrganizationDefinition : {}", vm);

        Language language = getLanguage(vm.defaultLanguageId());

        OrganizationDefinition org = new OrganizationDefinition();
        org.setName(vm.name());
        org.setDescription(vm.description());
        org.setAddress(vm.address());

        org.setContactName(vm.contactName());
        org.setContactAddress(vm.contactAddress());
        org.setContactEmail(vm.contactEmail());
        org.setContactMobile(vm.contactMobile());
        org.setContactLandNumber(vm.contactLandNumber());

        org.setTaxValue(vm.taxValue());
        org.setDefaultTimeZone(vm.defaultTimeZone());
        org.setDefaultLanguage(language);

        validateWorkingDays(vm.workingDays());
        org.setWorkingDays(normalizeWorkingDays(vm.workingDays()));

        return organizationDefinitionRepository.save(org);
    }

    public Optional<OrganizationDefinition> update(Long id, OrganizationDefinitionUpdateVM vm) {
        LOG.debug("Request to update OrganizationDefinition id={} with {}", id, vm);

        return organizationDefinitionRepository.findById(id).map(existing -> {
            if (vm.name() != null) existing.setName(vm.name());
            if (vm.description() != null) existing.setDescription(vm.description());
            if (vm.address() != null) existing.setAddress(vm.address());

            if (vm.contactName() != null) existing.setContactName(vm.contactName());
            if (vm.contactAddress() != null) existing.setContactAddress(vm.contactAddress());
            if (vm.contactEmail() != null) existing.setContactEmail(vm.contactEmail());
            if (vm.contactMobile() != null) existing.setContactMobile(vm.contactMobile());
            if (vm.contactLandNumber() != null) existing.setContactLandNumber(vm.contactLandNumber());

            if (vm.taxValue() != null) existing.setTaxValue(vm.taxValue());
            if (vm.defaultTimeZone() != null) existing.setDefaultTimeZone(vm.defaultTimeZone());
            if (vm.defaultLanguageId() != null) {
                existing.setDefaultLanguage(getLanguage(vm.defaultLanguageId()));
            }

            if (vm.workingDays() != null) {
                validateWorkingDays(vm.workingDays());
                existing.setWorkingDays(normalizeWorkingDays(vm.workingDays()));
            }

            return organizationDefinitionRepository.save(existing);
        });
    }

    @Transactional(readOnly = true)
    public List<OrganizationDefinition> findAll() {
        LOG.debug("Request to get all OrganizationDefinitions");
        return organizationDefinitionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<OrganizationDefinition> findOne(Long id) {
        LOG.debug("Request to get OrganizationDefinition : {}", id);
        return organizationDefinitionRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public boolean exists() {
        LOG.debug("organization definition exist");
        return organizationDefinitionRepository.count() > 0;
    }

    private List<WorkingDayJson> normalizeWorkingDays(List<WorkingDayJson> workingDays) {
        if (workingDays == null || workingDays.isEmpty()) {
            return Collections.emptyList();
        }

        return workingDays.stream()
                .map(item -> WorkingDayJson.builder()
                        .dayOfWeek(item.getDayOfWeek())
                        .isWorking(item.getIsWorking())
                        .build())
                .toList();
    }

    private void validateWorkingDays(List<WorkingDayJson> workingDays) {
        if (workingDays == null || workingDays.isEmpty()) {
            return;
        }

        Set<DayOfWeek> uniqueDays = workingDays.stream()
                .map(WorkingDayJson::getDayOfWeek)
                .collect(Collectors.toSet());

        if (uniqueDays.size() != workingDays.size()) {
            throw new BadRequestAlertException(
                    "Duplicate working day entries",
                    "organizationWorkingDay",
                    "duplicate_day"
            );
        }
    }

    private Language getLanguage(Long id) {
        LOG.debug("getLanguage for language: id={}", id);

        return languageRepository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException(
                        "language not found: " + id,
                        "Language",
                        "notfound"
                ));
    }
}