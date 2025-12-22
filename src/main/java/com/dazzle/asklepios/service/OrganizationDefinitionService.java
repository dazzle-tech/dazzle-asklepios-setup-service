package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.OrganizationDefinition;
import com.dazzle.asklepios.repository.OrganizationDefinitionRepository;
import com.dazzle.asklepios.web.rest.vm.organizationDefinition.OrganizationDefinitionCreateVM;
import com.dazzle.asklepios.web.rest.vm.organizationDefinition.OrganizationDefinitionUpdateVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrganizationDefinitionService {

    private static final Logger LOG = LoggerFactory.getLogger(OrganizationDefinitionService.class);

    private final OrganizationDefinitionRepository organizationDefinitionRepository;

    public OrganizationDefinitionService(OrganizationDefinitionRepository organizationDefinitionRepository) {
        this.organizationDefinitionRepository = organizationDefinitionRepository;
    }

    public OrganizationDefinition create(OrganizationDefinitionCreateVM vm) {
        LOG.debug("Request to create OrganizationDefinition : {}", vm);

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

            OrganizationDefinition updated =
                    organizationDefinitionRepository.save(existing);

            return updated;
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
        return organizationDefinitionRepository.count() > 0;
    }
}
