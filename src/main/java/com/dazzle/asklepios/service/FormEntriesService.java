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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class FormEntriesService {

    private static final Logger LOG = LoggerFactory.getLogger(FormEntriesService.class);

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
        LOG.debug("Create FormEntry payload: dataJsonLen={}",
                dto.dataJson() != null ? dto.dataJson().length() : 0);

        FormTemplate template = formTemplateRepository.findById(dto.templateId()).orElseThrow();
        Facility facility = facilityRepository.findById(dto.facilityId()).orElseThrow();
        Department department = departmentsRepository.findById(dto.departmentId()).orElseThrow();

        LOG.debug("Resolved templateId={} -> '{}', facilityId={} -> '{}', departmentId={} -> '{}'",
                template.getId(), template.getName(),
                facility.getId(), facility.getName(),
                department.getId(), department.getName());

        FormEntries formEntries = new FormEntries();
        formEntries.setTitle(dto.title());
        formEntries.setTemplate(template);
        formEntries.setFacility(facility);
        formEntries.setDepartment(department);
        formEntries.setDataJson(dto.dataJson());
        formEntries.setPatientId(dto.patientId());
        formEntries.setEncounterId(dto.encounterId());

        return toVM(formEntriesRepository.save(formEntries));
    }

    public FormEntryResponseVM update(Long id, FormEntryUpdateDTO dto) {
        FormEntries formEntries = formEntriesRepository.findById(id).orElseThrow();

        formEntries.setTitle(dto.title());
        formEntries.setDataJson(dto.dataJson());

        return toVM(formEntriesRepository.save(formEntries));
    }

    @Transactional(readOnly = true)
    public FormEntryResponseVM get(Long id) {
        LOG.debug("Request to get FormEntry id={}", id);
        return toVM(formEntriesRepository.findById(id).orElseThrow());
    }

    @Transactional(readOnly = true)
    public Page<FormEntryResponseVM> list(Long facilityId, Long departmentId, Long templateId, String query, Pageable pageable) {
        boolean hasFacility = facilityId != null;
        boolean hasDepartment = departmentId != null;
        boolean hasTemplate = templateId != null;
        boolean hasQuery = query != null && !query.trim().isEmpty();
        String qq = hasQuery ? query.trim() : null;

        LOG.debug("Request to list FormEntries: facilityId={}, departmentId={}, templateId={}, query='{}', page={}, size={}, sort={}",
                facilityId, departmentId, templateId, qq,
                pageable != null ? pageable.getPageNumber() : null,
                pageable != null ? pageable.getPageSize() : null,
                pageable != null ? pageable.getSort() : null);

        Page<FormEntries> page;

        if (hasTemplate) {
                if (hasQuery) {
                    LOG.debug("List branch: template+query");
                    page = formEntriesRepository.findByTemplate_IdAndTitleContainingIgnoreCase(templateId, qq, pageable);
                } else {
                    LOG.debug("List branch: template");
                    page = formEntriesRepository.findByTemplate_Id(templateId, pageable);
                }
        } else if (hasFacility && hasDepartment) {
            if (hasQuery) {
                LOG.debug("List branch: facility+department+query");
                page = formEntriesRepository.findByFacility_IdAndDepartment_IdAndTitleContainingIgnoreCase(
                        facilityId, departmentId, qq, pageable
                );
            } else {
                LOG.debug("List branch: facility+department");
                page = formEntriesRepository.findByFacility_IdAndDepartment_Id(facilityId, departmentId, pageable);
            }
           } else if (hasFacility) {
            if (hasQuery) {
                LOG.debug("List branch: facility+query");
                page = formEntriesRepository.findByFacility_IdAndTitleContainingIgnoreCase(facilityId, qq, pageable);
            } else {
                LOG.debug("List branch: facility");
                page = formEntriesRepository.findByFacility_Id(facilityId, pageable);
            }
            } else if (hasDepartment) {
            if (hasQuery) {
                LOG.debug("List branch: department+query");
                page = formEntriesRepository.findByDepartment_IdAndTitleContainingIgnoreCase(departmentId, qq, pageable);
            } else {
                LOG.debug("List branch: department");
                page = formEntriesRepository.findByDepartment_Id(departmentId, pageable);
            }
             } else {
            if (hasQuery) {
                LOG.debug("List branch: query-only");
                page = formEntriesRepository.findByTitleContainingIgnoreCase(qq, pageable);
            } else {
                LOG.debug("List branch: all");
                page = formEntriesRepository.findAll(pageable);
            }
           }

        LOG.debug("List result: totalElements={}, totalPages={}, returned={}",
                page.getTotalElements(), page.getTotalPages(), page.getNumberOfElements());

        return page.map(this::toVM);
    }

    @Transactional(readOnly = true)
    public Page<FormEntryResponseVM> listByPatient(Long patientId, Pageable pageable) {
        LOG.debug("Request to list FormEntries by patientId={}", patientId);
        return formEntriesRepository.findByPatientId(patientId, pageable).map(this::toVM);
    }

    @Transactional(readOnly = true)
    public Page<FormEntryResponseVM> listByEncounter(Long encounterId, Pageable pageable) {
        LOG.debug("Request to list FormEntries by encounterId={}", encounterId);
        return formEntriesRepository.findByEncounterId(encounterId, pageable).map(this::toVM);
    }

    public void delete(Long id) {
        LOG.info("Request to delete FormEntry id={}", id);
        formEntriesRepository.deleteById(id);
    }

    private FormEntryResponseVM toVM(FormEntries formEntries) {
        return new FormEntryResponseVM(
                formEntries.getId(),
                formEntries.getTitle(),
                formEntries.getTemplate() != null ? formEntries.getTemplate().getId() : null,
                formEntries.getTemplate() != null ? formEntries.getTemplate().getName() : null,
                formEntries.getFacility() != null ? formEntries.getFacility().getId() : null,
                formEntries.getDepartment() != null ? formEntries.getDepartment().getId() : null,
                formEntries.getDataJson(),
                formEntries.getCreatedDate(),
                formEntries.getCreatedBy(),
                formEntries.getLastModifiedDate(),
                formEntries.getLastModifiedBy(),
                formEntries.getPatientId(),
                formEntries.getEncounterId()
        );
    }
}
