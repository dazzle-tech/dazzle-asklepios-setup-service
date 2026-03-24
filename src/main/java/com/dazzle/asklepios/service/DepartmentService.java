package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.Resource;
import com.dazzle.asklepios.domain.enumeration.DepartmentType;
import com.dazzle.asklepios.domain.enumeration.EncounterType;
import com.dazzle.asklepios.repository.DepartmentsRepository;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.repository.ResourceRepository;
import com.dazzle.asklepios.repository.UserDepartmentRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.department.DepartmentCreateVM;
import com.dazzle.asklepios.web.rest.vm.department.DepartmentUpdateVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
public class DepartmentService {

    private static final Logger LOG = LoggerFactory.getLogger(DepartmentService.class);

    private final DepartmentsRepository departmentRepository;
    private final FacilityRepository facilityRepository;
    private final UserDepartmentRepository userDepartmentRepository;
    private final ResourceRepository resourceRepository;

    public DepartmentService(
            DepartmentsRepository departmentRepository,
            FacilityRepository facilityRepository,
            UserDepartmentRepository userDepartmentRepository,
            ResourceRepository resourceRepository
    ) {
        this.departmentRepository = departmentRepository;
        this.facilityRepository = facilityRepository;
        this.userDepartmentRepository = userDepartmentRepository;
        this.resourceRepository = resourceRepository;
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
                .hasMedicalSheets(departmentVM.hasMedicalSheets())
                .hasNurseMedicalSheets(departmentVM.hasNurseMedicalSheets())
                .parallelCapacityEnabled(departmentVM.parallelCapacityEnabled())
                .parallelCapacityValue(departmentVM.parallelCapacityValue())
                .defaultDurationMinutes(departmentVM.defaultDurationMinutes())
                .defaultBufferBeforeMinutes(departmentVM.defaultBufferBeforeMinutes())
                .defaultBufferAfterMinutes(departmentVM.defaultBufferAfterMinutes())
                .requirePractitioner(departmentVM.requirePractitioner())
                .requireBilling(departmentVM.requireBilling())
                .requirePreAssessment(departmentVM.requirePreAssessment())
                .build();

        validateDepartment(department);

        LOG.debug("Created department: {}", department);
        return departmentRepository.save(department);
    }

    public Optional<Department> update(Long id, DepartmentUpdateVM departmentVM) {
        LOG.debug("Request to update Department id={} with {}", id, departmentVM);

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Department not found with id " + id,
                        "department",
                        "notfound"
                ));

        if (departmentVM.facilityId() != null) {
            Facility facility = facilityRepository.findById(departmentVM.facilityId())
                    .orElseThrow(() -> new BadRequestAlertException(
                            "Facility not found with id " + departmentVM.facilityId(),
                            "facility",
                            "notfound"
                    ));
            department.setFacility(facility);
        }

        if (departmentVM.name() != null) department.setName(departmentVM.name());
        if (departmentVM.departmentType() != null) department.setType(departmentVM.departmentType());
        if (departmentVM.appointable() != null) department.setAppointable(departmentVM.appointable());
        if (departmentVM.departmentCode() != null) department.setCode(departmentVM.departmentCode());
        if (departmentVM.phoneNumber() != null) department.setPhoneNumber(departmentVM.phoneNumber());
        if (departmentVM.email() != null) department.setEmail(departmentVM.email());
        if (departmentVM.encounterType() != null) department.setEncounterType(departmentVM.encounterType());
        if (departmentVM.isActive() != null) department.setIsActive(departmentVM.isActive());

        if (departmentVM.hasMedicalSheets() != null) {
            department.setHasMedicalSheets(departmentVM.hasMedicalSheets());
        }
        if (departmentVM.hasNurseMedicalSheets() != null) {
            department.setHasNurseMedicalSheets(departmentVM.hasNurseMedicalSheets());
        }

        if (departmentVM.parallelCapacityEnabled() != null) {
            department.setParallelCapacityEnabled(departmentVM.parallelCapacityEnabled());
        }
        if (departmentVM.parallelCapacityValue() != null) {
            department.setParallelCapacityValue(departmentVM.parallelCapacityValue());
        }
        if (departmentVM.defaultDurationMinutes() != null) {
            department.setDefaultDurationMinutes(departmentVM.defaultDurationMinutes());
        }
        if (departmentVM.defaultBufferBeforeMinutes() != null) {
            department.setDefaultBufferBeforeMinutes(departmentVM.defaultBufferBeforeMinutes());
        }
        if (departmentVM.defaultBufferAfterMinutes() != null) {
            department.setDefaultBufferAfterMinutes(departmentVM.defaultBufferAfterMinutes());
        }
        if (departmentVM.requirePractitioner() != null) {
            department.setRequirePractitioner(departmentVM.requirePractitioner());
        }
        if (departmentVM.requireBilling() != null) {
            department.setRequireBilling(departmentVM.requireBilling());
        }
        if (departmentVM.requirePreAssessment() != null) {
            department.setRequirePreAssessment(departmentVM.requirePreAssessment());
        }

        validateDepartment(department);

        Department updated = departmentRepository.save(department);
        LOG.debug("Updated department: {}", updated);

        return Optional.of(updated);
    }

    private void validateDepartment(Department department) {
        validateParallelCapacity(department);
        validateAppointableRequirements(department);
    }

    private void validateParallelCapacity(Department department) {
        if (Boolean.TRUE.equals(department.getParallelCapacityEnabled())) {
            if (department.getParallelCapacityValue() == null || department.getParallelCapacityValue() < 1) {
                throw new BadRequestAlertException(
                        "parallelCapacityValue must be at least 1 when parallelCapacityEnabled is true",
                        "department",
                        "parallelcapacityinvalid"
                );
            }
        }
    }

    private void validateAppointableRequirements(Department department) {
        if (Boolean.TRUE.equals(department.getAppointable())) {
            if (department.getDefaultDurationMinutes() == null || department.getDefaultDurationMinutes() <= 0) {
                throw new BadRequestAlertException(
                        "defaultDurationMinutes must be greater than 0 when appointable is true",
                        "department",
                        "defaultdurationinvalid"
                );
            }

            if (department.getDefaultBufferBeforeMinutes() == null || department.getDefaultBufferBeforeMinutes() < 0) {
                throw new BadRequestAlertException(
                        "defaultBufferBeforeMinutes must be 0 or greater when appointable is true",
                        "department",
                        "bufferbeforeinvalid"
                );
            }

            if (department.getDefaultBufferAfterMinutes() == null || department.getDefaultBufferAfterMinutes() < 0) {
                throw new BadRequestAlertException(
                        "defaultBufferAfterMinutes must be 0 or greater when appointable is true",
                        "department",
                        "bufferafterinvalid"
                );
            }

            if (department.getRequirePractitioner() == null) {
                throw new BadRequestAlertException(
                        "requirePractitioner must not be null when appointable is true",
                        "department",
                        "requirepractitionerinvalid"
                );
            }

            if (department.getRequireBilling() == null) {
                throw new BadRequestAlertException(
                        "requireBilling must not be null when appointable is true",
                        "department",
                        "requirebillinginvalid"
                );
            }

            if (department.getRequirePreAssessment() == null) {
                throw new BadRequestAlertException(
                        "requirePreAssessment must not be null when appointable is true",
                        "department",
                        "requirepreassessmentinvalid"
                );
            }
        }
    }

    @Transactional(readOnly = true)
    public Page<Department> findAll(Pageable pageable) {
        LOG.debug("Request to get Departments with pagination: {}", pageable);
        return departmentRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<Department> findAll() {
        LOG.debug("Request to get Departments");
        return departmentRepository.findAll();
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
    public Page<Department> findActiveAppointableByDepartmentType(DepartmentType type, Pageable pageable) {
        LOG.debug("Request to get Active Appointable Departments by Type with pagination type={} pageable={}", type, pageable);
        return departmentRepository.findByAppointableTrueAndIsActiveTrueAndType(type, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Department> findByTypeAndFacilityId(DepartmentType type, Long facilityId, Pageable pageable) {
        LOG.debug("Request to get Departments by Type and Facility with pagination type={} facilityId={} pageable={}",
                type, facilityId, pageable);
        return departmentRepository.findByTypeAndFacilityId(type, facilityId, pageable);
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

    @Transactional(readOnly = true)
    public Page<Department> findAppointableByDepartmentType(DepartmentType type, Long facilityId, Pageable pageable) {
        LOG.debug("Request to get appoitable Departments by Type with pagination type={} pageable={}", type, pageable);
        return departmentRepository.findByTypeAndAppointableTrueAndIsActiveTrueAndFacilityId(type, facilityId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Department> findAppointableDepartment(Long facilityId, Pageable pageable) {
        LOG.debug("Request to get appoitable Departments  with pagination  pageable={}", pageable);
        return departmentRepository.findByAppointableTrueAndIsActiveTrueAndFacilityId(facilityId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Department> findAppointableActiveByFacilityAndEncounterType(
            Long facilityId,
            EncounterType encounterType,
            Pageable pageable) {

        LOG.debug("Request to get appointable & active Departments by facilityId={} encounterType={} pageable={}",
                facilityId, encounterType, pageable);

        return departmentRepository
                .findByAppointableTrueAndIsActiveTrueAndFacilityIdAndEncounterType(
                        facilityId,
                        encounterType,
                        pageable
                );
    }

    @Transactional(readOnly = true)
    public List<Department> findDepartmentsLinkedToResourceType(String resourceType) {
        LOG.debug("Request to get Departments linked to resources of type={}", resourceType);

        List<Resource> resources =
                resourceRepository.findByResourceTypeAndIsActiveTrue(resourceType);

        List<Long> deptIds = resources.stream()
                .map(Resource::getResourceKey)
                .filter(Objects::nonNull)
                .map(Long::valueOf)
                .toList();

        if (deptIds.isEmpty()) {
            return List.of();
        }

        return departmentRepository.findByIdIn(deptIds);
    }

    @Transactional(readOnly = true)
    public List<Department> findDepartmentsLinkedToEmergencyResource() {
        return findDepartmentsLinkedToResourceType("EMERGENCY");
    }

    @Transactional(readOnly = true)
    public List<Department> findByIds(List<Long> ids) {
        return departmentRepository.findAllById(ids);
    }
}