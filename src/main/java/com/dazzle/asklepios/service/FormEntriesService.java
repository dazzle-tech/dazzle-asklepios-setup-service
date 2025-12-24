package com.dazzle.asklepios.service;


import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.FormEntries;
import com.dazzle.asklepios.domain.FormTemplate;
import com.dazzle.asklepios.repository.DepartmentsRepository;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.repository.FormEntriesRepository;
import com.dazzle.asklepios.repository.FormTemplateRepository;
import com.dazzle.asklepios.service.dto.FormEntryCreateDTO;
import com.dazzle.asklepios.service.dto.FormEntryUpdateDTO;
import com.dazzle.asklepios.web.rest.vm.FormEntryResponseVM;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
public class FormEntriesService {

    private final FormEntriesRepository formEntriesRepository;
    private final FormTemplateRepository formTemplateRepository;
    private final FacilityRepository facilityRepository;
    private final DepartmentsRepository departmentsRepository;

    public FormEntriesService(
            FormEntriesRepository formEntriesRepository,
            FormTemplateRepository formTemplateRepository,
            FacilityRepository facilityRepository,
            DepartmentsRepository departmentsRepository
    ) {
        this.formEntriesRepository = formEntriesRepository;
        this.formTemplateRepository = formTemplateRepository;
        this.facilityRepository = facilityRepository;
        this.departmentsRepository = departmentsRepository;
    }

    public FormEntryResponseVM create(FormEntryCreateDTO dto) {
        FormTemplate template = formTemplateRepository.findById(dto.templateId()).orElseThrow();
        Facility facility = facilityRepository.findById(dto.facilityId()).orElseThrow();
        Department department = departmentsRepository.findById(dto.departmentId()).orElseThrow();

        FormEntries e = new FormEntries();
        e.setTitle(dto.title());
        e.setTemplate(template);
        e.setFacility(facility);
        e.setDepartment(department);
        e.setDataJson(dto.dataJson());

        return toVM(formEntriesRepository.save(e));
    }

    public FormEntryResponseVM update(Long id, FormEntryUpdateDTO dto) {
        FormEntries e = formEntriesRepository.findById(id).orElseThrow();

        e.setTitle(dto.title());
        e.setDataJson(dto.dataJson());

        return toVM(formEntriesRepository.save(e));
    }

    @Transactional(readOnly = true)
    public FormEntryResponseVM get(Long id) {
        return toVM(formEntriesRepository.findById(id).orElseThrow());
    }

    @Transactional(readOnly = true)
    public Page<FormEntryResponseVM> list(Long facilityId, Long departmentId, Long templateId, String q, Pageable pageable) {
        boolean hasFacility = facilityId != null;
        boolean hasDepartment = departmentId != null;
        boolean hasTemplate = templateId != null;
        boolean hasQ = q != null && !q.trim().isEmpty();
        String qq = hasQ ? q.trim() : null;

        Page<FormEntries> page;

        if (hasTemplate) {
            if (hasQ)
                page = formEntriesRepository.findByTemplate_IdAndTitleContainingIgnoreCase(templateId, qq, pageable);
            else page = formEntriesRepository.findByTemplate_Id(templateId, pageable);
        } else if (hasFacility && hasDepartment) {
            if (hasQ)
                page = formEntriesRepository.findByFacility_IdAndDepartment_IdAndTitleContainingIgnoreCase(facilityId, departmentId, qq, pageable);
            else page = formEntriesRepository.findByFacility_IdAndDepartment_Id(facilityId, departmentId, pageable);
        } else if (hasFacility) {
            if (hasQ)
                page = formEntriesRepository.findByFacility_IdAndTitleContainingIgnoreCase(facilityId, qq, pageable);
            else page = formEntriesRepository.findByFacility_Id(facilityId, pageable);
        } else if (hasDepartment) {
            if (hasQ)
                page = formEntriesRepository.findByDepartment_IdAndTitleContainingIgnoreCase(departmentId, qq, pageable);
            else page = formEntriesRepository.findByDepartment_Id(departmentId, pageable);
        } else {
            if (hasQ) page = formEntriesRepository.findByTitleContainingIgnoreCase(qq, pageable);
            else page = formEntriesRepository.findAll(pageable);
        }

        return page.map(this::toVM);
    }

    public void delete(Long id) {
        formEntriesRepository.deleteById(id);
    }

    private FormEntryResponseVM toVM(FormEntries e) {
        return new FormEntryResponseVM(
                e.getId(),
                e.getTitle(),
                e.getTemplate() != null ? e.getTemplate().getId() : null,
                e.getTemplate() != null ? e.getTemplate().getName() : null,
                e.getFacility() != null ? e.getFacility().getId() : null,
                e.getDepartment() != null ? e.getDepartment().getId() : null,
                e.getDataJson(),
                e.getCreatedDate(),
                e.getCreatedBy(),
                e.getLastModifiedDate(),
                e.getLastModifiedBy()
        );
    }
}
