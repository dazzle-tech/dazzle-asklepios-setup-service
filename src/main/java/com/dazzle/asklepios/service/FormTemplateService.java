package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.FormTemplate;
import com.dazzle.asklepios.repository.DepartmentsRepository;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.repository.FormTemplateRepository;
import com.dazzle.asklepios.service.dto.FormTemplateCreateDTO;
import com.dazzle.asklepios.service.dto.FormTemplateUpdateDTO;
import com.dazzle.asklepios.web.rest.vm.FormTemplateResponseVM;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
@Transactional
public class FormTemplateService {

    private final FormTemplateRepository formTemplateRepository;
    private final FacilityRepository facilityRepository;
    private final DepartmentsRepository departmentsRepository;
    private static final Logger LOG = LoggerFactory.getLogger(FormTemplateService.class);

    public FormTemplateService(
            FormTemplateRepository formTemplateRepository,
            FacilityRepository facilityRepository,
            DepartmentsRepository departmentsRepository
    ) {
        this.formTemplateRepository = formTemplateRepository;
        this.facilityRepository = facilityRepository;
        this.departmentsRepository = departmentsRepository;
    }

    public FormTemplateResponseVM create(FormTemplateCreateDTO dto) {
        LOG.info("Request to create FormTemplate: name='{}', facilityId={}, departmentId={}",
                dto.name(), dto.facilityId(), dto.departmentId());
        LOG.debug("Create FormTemplate payload: descriptionLen={}, formJsonLen={}",
                dto.description() != null ? dto.description().length() : 0,
                dto.formJson() != null ? dto.formJson().length() : 0);

        Facility facility = facilityRepository.findById(dto.facilityId()).orElseThrow();
        Department department = departmentsRepository.findById(dto.departmentId()).orElseThrow();

        FormTemplate formTemplate = new FormTemplate();
        formTemplate.setName(dto.name());
        formTemplate.setDescription(dto.description());
        formTemplate.setFacility(facility);
        formTemplate.setDepartment(department);
        formTemplate.setFormJson(dto.formJson());


        FormTemplate saved = formTemplateRepository.save(formTemplate);
        LOG.info("Created FormTemplate id={} name='{}'", saved.getId(), saved.getName());
        return toVM(saved);
    }

    public FormTemplateResponseVM update(Long id, FormTemplateUpdateDTO dto) {
        LOG.debug("Update FormTemplate payload: descriptionLen={}, formJsonLen={}",
                dto.description() != null ? dto.description().length() : 0,
                dto.formJson() != null ? dto.formJson().length() : 0);
        FormTemplate formTemplate = formTemplateRepository.findById(id).orElseThrow();

        Facility facility = facilityRepository.findById(dto.facilityId()).orElseThrow();
        Department department = departmentsRepository.findById(dto.departmentId()).orElseThrow();

        formTemplate.setName(dto.name());
        formTemplate.setDescription(dto.description());
        formTemplate.setFacility(facility);
        formTemplate.setDepartment(department);
        formTemplate.setFormJson(dto.formJson());
        FormTemplate saved = formTemplateRepository.save(formTemplate);
        LOG.info("Updated FormTemplate id={} name='{}'", saved.getId(), saved.getName());
        return toVM(saved);
    }

    @Transactional(readOnly = true)
    public FormTemplateResponseVM get(Long id) {
        LOG.debug("Request to get FormTemplate id={}", id);
        return toVM(formTemplateRepository.findById(id).orElseThrow());
    }

    @Transactional(readOnly = true)
    public Page<FormTemplateResponseVM> list(Long facilityId, Long departmentId, String query, Pageable pageable) {
        boolean hasFacility = facilityId != null;
        boolean hasDepartment = departmentId != null;
        boolean hasQuery = query != null && !query.trim().isEmpty();

        Page<FormTemplate> page;

        // 1) both facility + department
        if (hasFacility && hasDepartment) {
            if (hasQuery) {
                LOG.debug("List branch: facility+department+query");
                page = formTemplateRepository.findByFacility_IdAndDepartment_IdAndNameContainingIgnoreCase(
                        facilityId, departmentId, query.trim(), pageable
                );
            } else {
                LOG.debug("List branch: facility+department");
                page = formTemplateRepository.findByFacility_IdAndDepartment_Id(facilityId, departmentId, pageable);
            }
        }
        // 2) only facility
        else if (hasFacility) {
            if (hasQuery) {
                LOG.debug("List branch: facility+query");
                page = formTemplateRepository.findByFacility_IdAndNameContainingIgnoreCase(facilityId, query.trim(), pageable);
            } else {
                LOG.debug("List branch: facility");
                page = formTemplateRepository.findByFacility_Id(facilityId, pageable);
            }
        }
        // 3) only department
        else if (hasDepartment) {
            if (hasQuery) {
                LOG.debug("List branch: department+query");
                page = formTemplateRepository.findByDepartment_IdAndNameContainingIgnoreCase(departmentId, query.trim(), pageable);
            } else {
                LOG.debug("List branch: department");
                page = formTemplateRepository.findByDepartment_Id(departmentId, pageable);
            }
        }
        else {
            if (hasQuery) {
                LOG.debug("List branch: query-only");
                page = formTemplateRepository.findByNameContainingIgnoreCase(query.trim(), pageable);
            } else {
                LOG.debug("List branch: all");
                page = formTemplateRepository.findAll(pageable);
            }
        }
        LOG.debug("List result: totalElements={}, totalPages={}, returned={}",
                page.getTotalElements(), page.getTotalPages(), page.getNumberOfElements());


        return page.map(this::toVM);
    }

    public void delete(Long id) {
        formTemplateRepository.deleteById(id);
    }

    private FormTemplateResponseVM toVM(FormTemplate formTemplate) {
        return new FormTemplateResponseVM(
                formTemplate.getId(),
                formTemplate.getName(),
                formTemplate.getDescription(),
                formTemplate.getFacility() != null ? formTemplate.getFacility().getId() : null,
                formTemplate.getFacility() != null ? formTemplate.getFacility().getName() : null,
                formTemplate.getDepartment() != null ? formTemplate.getDepartment().getId() : null,
                formTemplate.getDepartment() != null ? formTemplate.getDepartment().getName() : null,
                formTemplate.getFormJson(),
                formTemplate.getCreatedBy(),
                formTemplate.getCreatedDate(),
                formTemplate.getLastModifiedBy(),
                formTemplate.getLastModifiedDate()
        );
    }
}
