package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.DepartmentMedicalSheetsVisibility;
import com.dazzle.asklepios.repository.DepartmentMedicalSheetsVisibilityRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DepartmentMedicalSheetsVisibilityService {

    private final DepartmentMedicalSheetsVisibilityRepository repository;
    private static final String ENTITY_NAME = "departmentMedicalSheetsVisibility";

    public DepartmentMedicalSheetsVisibilityService(DepartmentMedicalSheetsVisibilityRepository repository) {
        this.repository = repository;
    }

    public DepartmentMedicalSheetsVisibility create(DepartmentMedicalSheetsVisibility entity) {
        if (entity.getMedicalSheet() == null ) {
            throw new BadRequestAlertException("Invalid medical sheet code", ENTITY_NAME, "medicalSheetInvalid");
        }
        return repository.save(entity);
    }

    @Transactional(readOnly = true)
    public List<DepartmentMedicalSheetsVisibility> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<DepartmentMedicalSheetsVisibility> findByDepartmentId(Long departmentId) {
        return repository.findByDepartmentId(departmentId);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Transactional
    public List<DepartmentMedicalSheetsVisibility> bulkSave(List<DepartmentMedicalSheetsVisibility> list) {
        if (list.isEmpty()) {
            throw new BadRequestAlertException("Bulk save list cannot be empty", ENTITY_NAME, "listEmpty");
        }

        list.stream()
                .filter(e -> e.getMedicalSheet() == null)
                .findAny()
                .ifPresent(e -> {
                    throw new BadRequestAlertException("Invalid medical sheet code", ENTITY_NAME, "medicalSheetInvalid");
                });

        list.stream()
                .filter(e -> e.getDepartmentId() == null)
                .findAny()
                .ifPresent(e -> {
                    throw new BadRequestAlertException("Missing department id", ENTITY_NAME, "departmentMissing");
                });

        Long departmentId = list.get(0).getDepartmentId();
        repository.deleteByDepartmentId(departmentId);
        return repository.saveAll(list);
    }

}
