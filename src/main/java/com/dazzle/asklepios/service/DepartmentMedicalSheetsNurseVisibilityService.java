package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.DepartmentMedicalSheetsNurseVisbility;
import com.dazzle.asklepios.repository.DepartmentMedicalSheetsNurseVisibilityRepository;

import com.dazzle.asklepios.web.rest.vm.DepartmentMedicalSheetsNurseVisibilityVM;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@Transactional
public class DepartmentMedicalSheetsNurseVisibilityService{
    private final DepartmentMedicalSheetsNurseVisibilityRepository repository;

    public DepartmentMedicalSheetsNurseVisibilityService(DepartmentMedicalSheetsNurseVisibilityRepository repository) {
        this.repository = repository;
    }

    public DepartmentMedicalSheetsNurseVisibilityVM create(DepartmentMedicalSheetsNurseVisibilityVM vm) {
        DepartmentMedicalSheetsNurseVisbility entity = new DepartmentMedicalSheetsNurseVisbility();
        entity.setDepartmentId(vm.departmentId());
        entity.setMedicalSheet(vm.medicalSheet());
        DepartmentMedicalSheetsNurseVisbility saved = repository.save(entity);
        return DepartmentMedicalSheetsNurseVisibilityVM.ofEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<DepartmentMedicalSheetsNurseVisibilityVM> findAll() {
        return repository.findAll().stream()
                .map(DepartmentMedicalSheetsNurseVisibilityVM::ofEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DepartmentMedicalSheetsNurseVisibilityVM> findByDepartmentId(Long departmentId) {
        return repository.findByDepartmentId(departmentId).stream()
                .map(DepartmentMedicalSheetsNurseVisibilityVM::ofEntity)
                .toList();
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }




    @Transactional
    public List<DepartmentMedicalSheetsNurseVisibilityVM> bulkSave(List<DepartmentMedicalSheetsNurseVisibilityVM> list) {
        if (list.isEmpty()) return List.of();

        Long departmentId = list.get(0).departmentId();
        repository.deleteByDepartmentId(departmentId);

        var entities = list.stream()
                .map(vm -> {
                    DepartmentMedicalSheetsNurseVisbility e = new DepartmentMedicalSheetsNurseVisbility();
                    e.setDepartmentId(vm.departmentId());
                    e.setMedicalSheet(vm.medicalSheet());
                    return e;
                })
                .toList();

        var saved = repository.saveAll(entities);
        return saved.stream().map(DepartmentMedicalSheetsNurseVisibilityVM::ofEntity).toList();
    }


}
