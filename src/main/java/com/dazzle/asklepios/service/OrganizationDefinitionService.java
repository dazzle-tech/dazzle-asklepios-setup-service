package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Language;
import com.dazzle.asklepios.domain.OrganizationDefinition;
import com.dazzle.asklepios.domain.OrganizationWorkingDay;
import com.dazzle.asklepios.domain.enumeration.DayOfWeek;
import com.dazzle.asklepios.repository.LanguageRepository;
import com.dazzle.asklepios.repository.OrganizationDefinitionRepository;
import com.dazzle.asklepios.repository.OrganizationWorkingDayRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import com.dazzle.asklepios.web.rest.vm.organizationDefinition.OrganizationDefinitionCreateVM;
import com.dazzle.asklepios.web.rest.vm.organizationDefinition.OrganizationDefinitionUpdateVM;
import com.dazzle.asklepios.web.rest.vm.organizationDefinition.OrganizationWorkingDayCreateVM;
import com.dazzle.asklepios.web.rest.vm.organizationDefinition.OrganizationWorkingDayUpdateVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrganizationDefinitionService {

    private static final Logger LOG = LoggerFactory.getLogger(OrganizationDefinitionService.class);

    private final OrganizationDefinitionRepository organizationDefinitionRepository;
    private final OrganizationWorkingDayRepository organizationWorkingDayRepository;
    private final LanguageRepository languageRepository;

    public OrganizationDefinitionService(OrganizationDefinitionRepository organizationDefinitionRepository, OrganizationWorkingDayRepository organizationWorkingDayRepository, LanguageRepository languageRepository) {
        this.organizationDefinitionRepository = organizationDefinitionRepository;
        this.organizationWorkingDayRepository = organizationWorkingDayRepository;
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

        OrganizationDefinition saved = organizationDefinitionRepository.save(org);

        saveWorkingDaysOnCreate(saved, vm.workingDays());
        loadWorkingDays(saved);

        return saved;
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
            if (vm.defaultLanguageId() != null) existing.setDefaultLanguage(getLanguage(vm.defaultLanguageId()));

            OrganizationDefinition updated = organizationDefinitionRepository.save(existing);

            if (vm.workingDays() != null) {
                replaceWorkingDays(updated, vm.workingDays());
            }

            loadWorkingDays(updated);
            return updated;
        });
    }

    @Transactional(readOnly = true)
    public List<OrganizationDefinition> findAll() {
        LOG.debug("Request to get all OrganizationDefinitions");

        List<OrganizationDefinition> organizations = organizationDefinitionRepository.findAll();
        organizations.forEach(this::loadWorkingDays);

        return organizations;
    }

    @Transactional(readOnly = true)
    public Optional<OrganizationDefinition> findOne(Long id) {
        LOG.debug("Request to get OrganizationDefinition : {}", id);

        return organizationDefinitionRepository.findById(id)
                .map(org -> {
                    loadWorkingDays(org);
                    return org;
                });
    }

    @Transactional(readOnly = true)
    public boolean exists() {
        return organizationDefinitionRepository.count() > 0;
    }

    private void saveWorkingDaysOnCreate(OrganizationDefinition organization, List<OrganizationWorkingDayCreateVM> workingDays) {
        if (workingDays == null || workingDays.isEmpty()) {
            organization.setWorkingDays(List.of());
            return;
        }

        validateCreateWorkingDays(workingDays);

        List<OrganizationWorkingDay> entities = workingDays.stream()
                .map(item -> {
                    OrganizationWorkingDay row = new OrganizationWorkingDay();
                    row.setOrganizationDefinition(organization);
                    row.setDayOfWeek(item.dayOfWeek());
                    row.setIsWorking(item.isWorking());
                    return row;
                })
                .toList();

        organizationWorkingDayRepository.saveAll(entities);

    }

    private void replaceWorkingDays(OrganizationDefinition organization, List<OrganizationWorkingDayUpdateVM> workingDays) {
        validateUpdateWorkingDays(workingDays);

        organizationWorkingDayRepository.deleteAllByOrganizationDefinitionId(organization.getId());
        organizationWorkingDayRepository.flush();

        if (workingDays.isEmpty()) {
            organization.setWorkingDays(List.of());
            return;
        }

        List<OrganizationWorkingDay> entities = workingDays.stream()
                .map(item -> {
                    OrganizationWorkingDay row = new OrganizationWorkingDay();
                    row.setOrganizationDefinition(organization);
                    row.setDayOfWeek(item.dayOfWeek());
                    row.setIsWorking(item.isWorking());
                    return row;
                })
                .toList();

        organizationWorkingDayRepository.saveAll(entities);
        organizationWorkingDayRepository.flush();
    }

    private void validateCreateWorkingDays(List<OrganizationWorkingDayCreateVM> workingDays) {
        Set<DayOfWeek> uniqueDays = workingDays.stream()
                .map(OrganizationWorkingDayCreateVM::dayOfWeek)
                .collect(Collectors.toSet());

        if (uniqueDays.size() != workingDays.size()) {
            throw new BadRequestAlertException(
                    "Duplicate working day entries",
                    "organizationWorkingDay",
                    "duplicate_day"
            );
        }
    }

    private void validateUpdateWorkingDays(List<OrganizationWorkingDayUpdateVM> workingDays) {
        Set<DayOfWeek> uniqueDays = workingDays.stream()
                .map(OrganizationWorkingDayUpdateVM::dayOfWeek)
                .collect(Collectors.toSet());

        if (uniqueDays.size() != workingDays.size()) {
            throw new BadRequestAlertException(
                    "Duplicate working day entries",
                    "organizationWorkingDay",
                    "duplicate_day"
            );
        }
    }

    private void loadWorkingDays(OrganizationDefinition organization) {
        List<OrganizationWorkingDay> workingDays =
                organizationWorkingDayRepository.findAllByOrganizationDefinitionIdOrderByDayOfWeekAsc(
                        organization.getId()
                );
        organization.setWorkingDays(workingDays);
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