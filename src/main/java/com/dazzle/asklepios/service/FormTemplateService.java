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

@Service
@Transactional
public class FormTemplateService {

    private final FormTemplateRepository formTemplateRepository;
    private final FacilityRepository facilityRepository;
    private final DepartmentsRepository departmentsRepository;

    public FormTemplateService(
            FormTemplateRepository formTemplateRepository,
            FacilityRepository facilityRepository,
            DepartmentsRepository departmentsRepository
    ) {
        this.formTemplateRepository = formTemplateRepository;
        this.facilityRepository = facilityRepository;
        this.departmentsRepository = departmentsRepository;
    }

    public FormTemplateResponseVM create(FormTemplateCreateDTO vm) {
        Facility facility = facilityRepository.findById(vm.facilityId()).orElseThrow();
        Department department = departmentsRepository.findById(vm.departmentId()).orElseThrow();

        FormTemplate ft = new FormTemplate();
        ft.setName(vm.name());
        ft.setDescription(vm.description());
        ft.setFacility(facility);
        ft.setDepartment(department);
        ft.setFormJson(vm.formJson());


        FormTemplate saved = formTemplateRepository.save(ft);
        return toVM(saved);
    }

    public FormTemplateResponseVM update(Long id, FormTemplateUpdateDTO vm) {
        FormTemplate ft = formTemplateRepository.findById(id).orElseThrow();

        Facility facility = facilityRepository.findById(vm.facilityId()).orElseThrow();
        Department department = departmentsRepository.findById(vm.departmentId()).orElseThrow();

        ft.setName(vm.name());
        ft.setDescription(vm.description());
        ft.setFacility(facility);
        ft.setDepartment(department);
        ft.setFormJson(vm.formJson());

        return toVM(formTemplateRepository.save(ft));
    }

    @Transactional(readOnly = true)
    public FormTemplateResponseVM get(Long id) {
        return toVM(formTemplateRepository.findById(id).orElseThrow());
    }

    @Transactional(readOnly = true)
    public Page<FormTemplateResponseVM> list(Long facilityId, Long departmentId, String q, Pageable pageable) {
        boolean hasFacility = facilityId != null;
        boolean hasDepartment = departmentId != null;
        boolean hasQ = q != null && !q.trim().isEmpty();

        Page<FormTemplate> page;

        // 1) both facility + department
        if (hasFacility && hasDepartment) {
            if (hasQ) {
                page = formTemplateRepository.findByFacility_IdAndDepartment_IdAndNameContainingIgnoreCase(
                        facilityId, departmentId, q.trim(), pageable
                );
            } else {
                page = formTemplateRepository.findByFacility_IdAndDepartment_Id(facilityId, departmentId, pageable);
            }
        }
        // 2) only facility
        else if (hasFacility) {
            if (hasQ) {
                page = formTemplateRepository.findByFacility_IdAndNameContainingIgnoreCase(facilityId, q.trim(), pageable);
            } else {
                page = formTemplateRepository.findByFacility_Id(facilityId, pageable);
            }
        }
        // 3) only department
        else if (hasDepartment) {
            if (hasQ) {
                page = formTemplateRepository.findByDepartment_IdAndNameContainingIgnoreCase(departmentId, q.trim(), pageable);
            } else {
                page = formTemplateRepository.findByDepartment_Id(departmentId, pageable);
            }
        }
        // 4) none (only q or all)
        else {
            if (hasQ) {
                page = formTemplateRepository.findByNameContainingIgnoreCase(q.trim(), pageable);
            } else {
                page = formTemplateRepository.findAll(pageable);
            }
        }

        return page.map(this::toVM);
    }

    public void delete(Long id) {
        formTemplateRepository.deleteById(id);
    }

    private FormTemplateResponseVM toVM(FormTemplate ft) {
        return new FormTemplateResponseVM(
                ft.getId(),
                ft.getName(),
                ft.getDescription(),
                ft.getFacility() != null ? ft.getFacility().getId() : null,
                ft.getFacility() != null ? ft.getFacility().getName() : null,
                ft.getDepartment() != null ? ft.getDepartment().getId() : null,
                ft.getDepartment() != null ? ft.getDepartment().getName() : null,
                ft.getFormJson(),
                ft.getCreatedBy(),
                ft.getCreatedDate(),
                ft.getLastModifiedBy(),
                ft.getLastModifiedDate()
        );
    }
}
