package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.DepartmentMedicalSheetsVisibility;
import com.dazzle.asklepios.repository.DepartmentMedicalSheetsVisibilityRepository;
import com.dazzle.asklepios.web.rest.vm.DepartmentMedicalSheetsVisibilityVM;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DepartmentMedicalSheetsVisibilityService {

    private final DepartmentMedicalSheetsVisibilityRepository repository;

    public DepartmentMedicalSheetsVisibilityService(DepartmentMedicalSheetsVisibilityRepository repository) {
        this.repository = repository;
    }

    public DepartmentMedicalSheetsVisibilityVM create(DepartmentMedicalSheetsVisibilityVM vm) {
        DepartmentMedicalSheetsVisibility entity = new DepartmentMedicalSheetsVisibility();
        entity.setDepartmentId(vm.departmentId());
        entity.setMedicalSheet(vm.medicalSheet());
        DepartmentMedicalSheetsVisibility saved = repository.save(entity);
        return DepartmentMedicalSheetsVisibilityVM.ofEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<DepartmentMedicalSheetsVisibilityVM> findAll() {
        return repository.findAll().stream()
                .map(DepartmentMedicalSheetsVisibilityVM::ofEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DepartmentMedicalSheetsVisibilityVM> findByDepartmentId(Long departmentId) {
        return repository.findByDepartmentId(departmentId).stream()
                .map(DepartmentMedicalSheetsVisibilityVM::ofEntity)
                .toList();
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }




    @Transactional
    public List<DepartmentMedicalSheetsVisibilityVM> bulkSave(List<DepartmentMedicalSheetsVisibilityVM> list) {
        if (list.isEmpty()) return List.of();

        Long departmentId = list.get(0).departmentId();
        repository.deleteByDepartmentId(departmentId);

        var entities = list.stream()
                .map(vm -> {
                    DepartmentMedicalSheetsVisibility e = new DepartmentMedicalSheetsVisibility();
                    e.setDepartmentId(vm.departmentId());
                    e.setMedicalSheet(vm.medicalSheet());
                    return e;
                })
                .toList();

        var saved = repository.saveAll(entities);
        return saved.stream().map(DepartmentMedicalSheetsVisibilityVM::ofEntity).toList();
    }



}
