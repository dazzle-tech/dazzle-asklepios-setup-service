package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.DepartmentMedicalSheetsNurseVisbility;
import com.dazzle.asklepios.repository.DepartmentMedicalSheetsNurseVisibilityRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DepartmentMedicalSheetsNurseVisibilityService {

    private final DepartmentMedicalSheetsNurseVisibilityRepository repository;
    private static final String ENTITY_NAME = "departmentMedicalSheetsNurseVisibility";

    public DepartmentMedicalSheetsNurseVisibilityService(DepartmentMedicalSheetsNurseVisibilityRepository repository) {
        this.repository = repository;
    }

    public DepartmentMedicalSheetsNurseVisbility create(DepartmentMedicalSheetsNurseVisbility entity) {
        if (entity.getMedicalSheet() == null) {
            throw new BadRequestAlertException("Invalid medical sheet code", ENTITY_NAME, "medicalSheetInvalid");
        }
        if (entity.getDepartmentId() == null) {
            throw new BadRequestAlertException("Missing department id", ENTITY_NAME, "departmentMissing");
        }
        return repository.save(entity);
    }

    @Transactional(readOnly = true)
    public List<DepartmentMedicalSheetsNurseVisbility> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<DepartmentMedicalSheetsNurseVisbility> findByDepartmentId(Long departmentId) {
        return repository.findByDepartmentId(departmentId);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Transactional
    public List<DepartmentMedicalSheetsNurseVisbility> bulkSave(List<DepartmentMedicalSheetsNurseVisbility> list) {
        if (list.isEmpty()) {
            throw new BadRequestAlertException("Bulk save list cannot be empty", ENTITY_NAME, "listEmpty");
        }

        // validation
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
