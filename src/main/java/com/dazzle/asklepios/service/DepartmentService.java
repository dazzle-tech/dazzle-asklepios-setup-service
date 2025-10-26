package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.enumeration.DepartmentType;
import com.dazzle.asklepios.repository.DepartmentsRepository;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.repository.UserDepartmentRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.department.DepartmentCreateVM;
import com.dazzle.asklepios.web.rest.vm.department.DepartmentUpdateVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DepartmentService {
    private static final Logger LOG = LoggerFactory.getLogger(DepartmentService.class);
    private final DepartmentsRepository departmentRepository;
    private final FacilityRepository facilityRepository;
    private final UserDepartmentRepository userDepartmentRepository;

    public DepartmentService(DepartmentsRepository departmentRepository, FacilityRepository facilityRepository, UserDepartmentRepository userDepartmentRepository) {
        this.departmentRepository = departmentRepository;
        this.facilityRepository = facilityRepository;
        this.userDepartmentRepository = userDepartmentRepository;
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
                .type(departmentVM.departmentType())
                .appointable(departmentVM.appointable())
                .code(departmentVM.departmentCode())
                .phoneNumber(departmentVM.phoneNumber())
                .email(departmentVM.email())
                .encounterType(departmentVM.encounterType())
                .isActive(departmentVM.isActive())
                .build();
        LOG.debug("Created department: {}", department);

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
        if (departmentVM.departmentType() != null) department.setType(departmentVM.departmentType());
        if (departmentVM.appointable() != null) department.setAppointable(departmentVM.appointable());
        if (departmentVM.departmentCode() != null) department.setCode(departmentVM.departmentCode());
        if (departmentVM.phoneNumber() != null) department.setPhoneNumber(departmentVM.phoneNumber());
        if (departmentVM.email() != null) department.setEmail(departmentVM.email());
        if (departmentVM.encounterType() != null) department.setEncounterType(departmentVM.encounterType());
        if (departmentVM.isActive() != null) department.setIsActive(departmentVM.isActive());

        Department updated = departmentRepository.save(department);
        LOG.debug("Updated department: {}", updated);

        return Optional.of(updated);
    }

    @Transactional(readOnly = true)
    public Page<Department> findAll(Pageable pageable) {
        LOG.debug("Request to get Departments with pagination: {}", pageable);
        return departmentRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Department> findByFacilityId(Long facilityId, Pageable pageable) {
        LOG.debug("Request to get Departments by Facility with pagination facilityId={} pageable={}", facilityId, pageable);
        return departmentRepository.findByFacilityId(facilityId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Department> findByDepartmentType(DepartmentType type, Pageable pageable) {
        LOG.debug("Request to get Departments by Type with pagination type={} pageable={}", type, pageable);
        return departmentRepository.findByType(type, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Department> findByDepartmentName(String name, Pageable pageable) {
        LOG.debug("Request to get Departments by Name with pagination name='{}' pageable={}", name, pageable);
        return departmentRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Department> findOne(Long id) {
        LOG.debug("Request to get Department : {}", id);
        return departmentRepository.findById(id);
    }

    public Optional<Department> toggleIsActive(Long id) {
        LOG.debug("Request to toggle Department isActive id={}", id);
        return departmentRepository.findById(id)
                .map(department -> {
                    boolean isActive = !Boolean.TRUE.equals(department.getIsActive());
                    department.setIsActive(isActive);
                    userDepartmentRepository.updateActiveByDepartmentId(department.getId(), isActive);

                    return departmentRepository.save(department);
                });
    }
    @Transactional(readOnly = true)
    public List<Department> findActiveByFacilityId(Long facilityId) {
        LOG.debug("Request to get ACTIVE Departments by Facility facility_id={}", facilityId);
        return departmentRepository.findByFacilityIdAndIsActiveTrue(facilityId);
    }
}
