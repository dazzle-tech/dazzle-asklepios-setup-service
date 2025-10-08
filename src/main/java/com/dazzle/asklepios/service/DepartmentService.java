package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.enumeration.DepartmentType;
import com.dazzle.asklepios.repository.DepartmentsRepository;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.DepartmentCreateVM;
import com.dazzle.asklepios.web.rest.vm.DepartmentResponseVM;
import com.dazzle.asklepios.web.rest.vm.DepartmentUpdateVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DepartmentService {
    private static final Logger LOG = LoggerFactory.getLogger(DepartmentService.class);
    private final DepartmentsRepository departmentRepository;
    private final FacilityRepository facilityRepository;

    public DepartmentService(DepartmentsRepository departmentRepository, FacilityRepository facilityRepository) {
        this.departmentRepository = departmentRepository;
        this.facilityRepository = facilityRepository;
    }

    public Department create(DepartmentCreateVM departmentVM) {
        LOG.debug("Request to create Department : {}", departmentVM);

        Facility facility = facilityRepository.findById(departmentVM.facilityId())
                .orElseThrow(() -> new BadRequestAlertException(
                        "Facility not found with id " + departmentVM.facilityId(),
                        "facility",
                        "notfound"
                ));

        Department department = Department.builder()
                .name(departmentVM.name())
                .facility(facility)
                .departmentType(departmentVM.departmentType())
                .appointable(departmentVM.appointable())
                .departmentCode(departmentVM.departmentCode())
                .phoneNumber(departmentVM.phoneNumber())
                .email(departmentVM.email())
                .encounterType(departmentVM.encounterType())
                .isActive(departmentVM.isActive())
                .createdBy(departmentVM.createdBy())
                .build();

        return departmentRepository.save(department);
    }

    public Optional<Department> update(Long id, DepartmentUpdateVM departmentVM) {
        LOG.debug("Request to update Department id={} with {}", id, departmentVM);

        Facility facility = facilityRepository.findById(departmentVM.facilityId())
                .orElseThrow(() -> new BadRequestAlertException(
                        "Facility not found with id " + departmentVM.facilityId(),
                        "facility",
                        "notfound"
                ));

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Department not found with id " + id,
                        "department",
                        "notfound"
                ));
        if (departmentVM.name() != null) department.setName(departmentVM.name());
        if (facility != null) department.setFacility(facility);
        if (departmentVM.departmentType() != null) department.setDepartmentType(departmentVM.departmentType());
        if (departmentVM.appointable() != null) department.setAppointable(departmentVM.appointable());
        if (departmentVM.departmentCode() != null) department.setDepartmentCode(departmentVM.departmentCode());
        if (departmentVM.phoneNumber() != null) department.setPhoneNumber(departmentVM.phoneNumber());
        if (departmentVM.email() != null) department.setEmail(departmentVM.email());
        if (departmentVM.encounterType() != null) department.setEncounterType(departmentVM.encounterType());
        if (departmentVM.isActive() != null) department.setIsActive(departmentVM.isActive());
        if (departmentVM.lastModifiedBy() != null) department.setLastModifiedBy(departmentVM.lastModifiedBy());

        department.setLastModifiedDate(LocalDateTime.now());
        Department updated = departmentRepository.save(department);

        return Optional.of(updated);
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponseVM> findAll() {
        LOG.debug("Request to get all Departments (no pagination)");
        return departmentRepository.findAll()
                .stream()
                .map(DepartmentResponseVM::ofEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<DepartmentResponseVM> findAll(Pageable pageable) {
        LOG.debug("Request to get Departments with pagination: {}", pageable);
        return departmentRepository.findAll(pageable).map(DepartmentResponseVM::ofEntity);
    }

    @Transactional(readOnly = true)
    public Page<DepartmentResponseVM> findByFacilityId(Long facilityId, Pageable pageable) {
        return departmentRepository.findByFacilityId(facilityId, pageable).map(DepartmentResponseVM::ofEntity);
    }

    @Transactional(readOnly = true)
    public Page<DepartmentResponseVM> findByDepartmentType(DepartmentType type, Pageable pageable) {
        return departmentRepository.findByDepartmentType(type, pageable).map(DepartmentResponseVM::ofEntity);
    }

    @Transactional(readOnly = true)
    public Page<DepartmentResponseVM> findByDepartmentName(String name, Pageable pageable) {
        return departmentRepository.findByNameContainingIgnoreCase(name, pageable).map(DepartmentResponseVM::ofEntity);
    }

    @Transactional(readOnly = true)
    public Optional<Department> findOne(Long id) {
        LOG.debug("Request to get Department : {}", id);
        return departmentRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Department> findByFacilityId(Long facilityId) {
        LOG.debug("Request to get Departments by Facility facility_id={}", facilityId);
        return departmentRepository.findByFacilityId(facilityId);
    }

    @Transactional(readOnly = true)
    public List<Department> findByDepartmentType(DepartmentType departmentType) {
        LOG.debug("Request to get Departments by Department Type department_type={}", departmentType);
        return departmentRepository.findByDepartmentType(departmentType);
    }

    @Transactional(readOnly = true)
    public List<Department> findByDepartmentName(String name) {
        LOG.debug("Request to get Departments by Name name={}", name);
        return departmentRepository.findByNameContainingIgnoreCase(name);
    }

    public Optional<Department> toggleIsActive(Long id) {
        return departmentRepository.findById(id)
                .map(department -> {
                    department.setIsActive(!Boolean.TRUE.equals(department.getIsActive()));
                    return departmentRepository.save(department);
                });
    }
}
