package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.enumeration.DepartmentType;
import com.dazzle.asklepios.repository.DepartmentsRepository;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.web.rest.vm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Facility not found with id " + departmentVM.facilityId()
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
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Facility not found with id " + departmentVM.facilityId()
                ));
        return departmentRepository.findById(id).map(existing -> {
            if (departmentVM.name() != null) existing.setName(departmentVM.name());
            if (facility != null) existing.setFacility(facility);
            if (departmentVM.departmentType() != null) existing.setDepartmentType(departmentVM.departmentType());
            if (departmentVM.appointable() != null) existing.setAppointable(departmentVM.appointable());
            if (departmentVM.departmentCode() != null) existing.setDepartmentCode(departmentVM.departmentCode());
            if (departmentVM.phoneNumber() != null) existing.setPhoneNumber(departmentVM.phoneNumber());
            if (departmentVM.email() != null) existing.setEmail(departmentVM.email());
            if (departmentVM.encounterType() != null) existing.setEncounterType(departmentVM.encounterType());
            if (departmentVM.isActive() != null) existing.setIsActive(departmentVM.isActive());
            if (departmentVM.lastModifiedBy() != null) existing.setLastModifiedBy(departmentVM.lastModifiedBy());
            existing.setLastModifiedDate(LocalDateTime.now());
            Department updated = departmentRepository.save(existing);
            return updated;
        });
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponseVM> findAll() {
        LOG.debug("Request to get all Facilities");
        return departmentRepository.findAll()
                .stream()
                .map(DepartmentResponseVM::ofEntity)
                .collect(Collectors.toList());
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
