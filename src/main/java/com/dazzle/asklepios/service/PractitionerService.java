package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.Practitioner;
import com.dazzle.asklepios.domain.PractitionerDepartment;
import com.dazzle.asklepios.domain.User;
import com.dazzle.asklepios.domain.enumeration.DayOfWeek;
import com.dazzle.asklepios.domain.enumeration.Specialty;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.repository.PractitionerDepartmentRepository;
import com.dazzle.asklepios.repository.PractitionersRepository;
import com.dazzle.asklepios.repository.UserRepository;
import com.dazzle.asklepios.security.SecurityUtils;
import com.dazzle.asklepios.service.dto.workingDay.WorkingDayJson;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.practitioner.PractitionerCreateVM;
import com.dazzle.asklepios.web.rest.vm.practitioner.PractitionerUpdateVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class PractitionerService {

    private static final Logger LOG = LoggerFactory.getLogger(PractitionerService.class);
    private final PractitionersRepository practitionerRepository;
    private final FacilityRepository facilityRepository;
    private final UserRepository userRepository;
    private final PractitionerDepartmentRepository practitionerDepartmentRepository;

    public PractitionerService(
            PractitionersRepository practitionerRepository,
            FacilityRepository facilityRepository,
            UserRepository userRepository,
            PractitionerDepartmentRepository practitionerDepartmentRepository) {
        this.practitionerRepository = practitionerRepository;
        this.facilityRepository = facilityRepository;
        this.userRepository = userRepository;
        this.practitionerDepartmentRepository = practitionerDepartmentRepository;
    }

    public Practitioner create(PractitionerCreateVM vm) {
        LOG.debug("Request to create Practitioner: {}", vm);

        Facility facility = facilityRepository.findById(vm.facilityId())
                .orElseThrow(() -> new BadRequestAlertException(
                        "Facility cannot be null or invalid. Facility not found with id " + vm.facilityId(),
                        "facility",
                        "notfound"
                ));

        User user = null;
        if (vm.userId() != null && vm.userId() > 0) {

            if (practitionerRepository.existsByUserId(vm.userId())) {
                throw new BadRequestAlertException(
                        "User already linked to another practitioner",
                        "practitioner",
                        "userexists"
                );
            }

            user = userRepository.findById(vm.userId())
                    .orElseThrow(() -> new BadRequestAlertException(
                            "User not found with id " + vm.userId(),
                            "user",
                            "notfound"
                    ));
        }

        Practitioner practitioner = Practitioner.builder()
                .facility(facility)
                .firstName(vm.firstName())
                .lastName(vm.lastName())
                .email(vm.email())
                .phoneNumber(vm.phoneNumber())
                .specialty(vm.specialty())
                .subSpecialty(vm.subSpecialty())
                .defaultMedicalLicense(vm.defaultMedicalLicense())
                .secondaryMedicalLicense(vm.secondaryMedicalLicense())
                .educationalLevel(vm.educationalLevel())
                .appointable(vm.appointable())
                .user(user)
                .defaultLicenseValidUntil(vm.defaultLicenseValidUntil())
                .secondaryLicenseValidUntil(vm.secondaryLicenseValidUntil())
                .dateOfBirth(vm.dateOfBirth())
                .jobRole(vm.jobRole())
                .gender(vm.gender())
                .isActive(vm.isActive() != null ? vm.isActive() : true)
                .parallelCapacityValue(vm.parallelCapacityValue() != null ? vm.parallelCapacityValue() : 1)
                .defaultDurationMinutes(vm.defaultDurationMinutes())
                .defaultBufferBeforeMinutes(vm.defaultBufferBeforeMinutes() != null ? vm.defaultBufferBeforeMinutes() : 0)
                .defaultBufferAfterMinutes(vm.defaultBufferAfterMinutes() != null ? vm.defaultBufferAfterMinutes() : 0)
                .workingDays(normalizeWorkingDays(vm.workingDays()))
                .nationalNumber(vm.nationalNumber())
                .build();

        validatePractitioner(practitioner);

        return practitionerRepository.save(practitioner);
    }

    @Transactional
    public Optional<Practitioner> update(Long id, PractitionerUpdateVM vm) {
        LOG.debug("Request to update Practitioner id={} with {}", id, vm);

        Practitioner practitioner = practitionerRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Practitioner not found with id " + id,
                        "practitioner",
                        "notfound"
                ));

        if (vm.facilityId() == null) {
            throw new BadRequestAlertException("Facility cannot be null", "facility", "null");
        }

        Facility facility = facilityRepository.findById(vm.facilityId())
                .orElseThrow(() -> new BadRequestAlertException(
                        "Facility not found with id " + vm.facilityId(),
                        "facility",
                        "notfound"
                ));
        practitioner.setFacility(facility);

        if (vm.userId() != null && vm.userId() > 0) {
            if (!vm.userId().equals(practitioner.getUser() != null ? practitioner.getUser().getId() : null)
                    && practitionerRepository.existsByUserId(vm.userId())) {
                throw new BadRequestAlertException(
                        "User already linked to another practitioner",
                        "practitioner",
                        "userexists"
                );
            }

            User user = userRepository.findById(vm.userId())
                    .orElseThrow(() -> new BadRequestAlertException(
                            "User not found with id " + vm.userId(),
                            "user",
                            "notfound"
                    ));
            practitioner.setUser(user);
        } else {
            practitioner.setUser(null);
        }

        if (vm.firstName() != null) practitioner.setFirstName(vm.firstName());
        if (vm.lastName() != null) practitioner.setLastName(vm.lastName());

        if (vm.email() != null && !vm.email().isBlank()) practitioner.setEmail(vm.email());
        else if (vm.email() != null) practitioner.setEmail(null);

        if (vm.phoneNumber() != null) practitioner.setPhoneNumber(vm.phoneNumber());
        if (vm.specialty() != null) practitioner.setSpecialty(vm.specialty());
        if (vm.subSpecialty() != null) practitioner.setSubSpecialty(vm.subSpecialty());
        if (vm.defaultMedicalLicense() != null) practitioner.setDefaultMedicalLicense(vm.defaultMedicalLicense());
        if (vm.secondaryMedicalLicense() != null) practitioner.setSecondaryMedicalLicense(vm.secondaryMedicalLicense());
        if (vm.educationalLevel() != null) practitioner.setEducationalLevel(vm.educationalLevel());
        if (vm.appointable() != null) practitioner.setAppointable(vm.appointable());
        if (vm.defaultLicenseValidUntil() != null)
            practitioner.setDefaultLicenseValidUntil(vm.defaultLicenseValidUntil());
        if (vm.secondaryLicenseValidUntil() != null)
            practitioner.setSecondaryLicenseValidUntil(vm.secondaryLicenseValidUntil());
        if (vm.dateOfBirth() != null) practitioner.setDateOfBirth(vm.dateOfBirth());
        if (vm.jobRole() != null) practitioner.setJobRole(vm.jobRole());
        if (vm.gender() != null) practitioner.setGender(vm.gender());
        if (vm.isActive() != null) practitioner.setIsActive(vm.isActive());

        if (vm.parallelCapacityValue() != null) practitioner.setParallelCapacityValue(vm.parallelCapacityValue());
        if (vm.defaultDurationMinutes() != null) practitioner.setDefaultDurationMinutes(vm.defaultDurationMinutes());
        if (vm.defaultBufferBeforeMinutes() != null)
            practitioner.setDefaultBufferBeforeMinutes(vm.defaultBufferBeforeMinutes());
        if (vm.defaultBufferAfterMinutes() != null)
            practitioner.setDefaultBufferAfterMinutes(vm.defaultBufferAfterMinutes());
        if (vm.workingDays() != null) {
            practitioner.setWorkingDays(normalizeWorkingDays(vm.workingDays()));
        }
         practitioner.setNationalNumber(vm.nationalNumber());

        validatePractitioner(practitioner);

        Practitioner updated = practitionerRepository.save(practitioner);
        LOG.debug("Updated Practitioner successfully: {}", updated);

        return Optional.of(updated);
    }

    private void validatePractitioner(Practitioner practitioner) {
        if (Boolean.TRUE.equals(practitioner.getAppointable())) {
            if (practitioner.getDefaultDurationMinutes() == null || practitioner.getDefaultDurationMinutes() <= 0) {
                throw new BadRequestAlertException(
                        "defaultDurationMinutes must be greater than 0 when appointable is true",
                        "practitioner",
                        "defaultdurationinvalid"
                );
            }

            if (practitioner.getDefaultBufferBeforeMinutes() == null || practitioner.getDefaultBufferBeforeMinutes() < 0) {
                throw new BadRequestAlertException(
                        "defaultBufferBeforeMinutes must be 0 or greater when appointable is true",
                        "practitioner",
                        "bufferbeforeinvalid"
                );
            }

            if (practitioner.getDefaultBufferAfterMinutes() == null || practitioner.getDefaultBufferAfterMinutes() < 0) {
                throw new BadRequestAlertException(
                        "defaultBufferAfterMinutes must be 0 or greater when appointable is true",
                        "practitioner",
                        "bufferafterinvalid"
                );
            }
        }

        validateWorkingDays(practitioner.getWorkingDays());
    }

    private List<WorkingDayJson> normalizeWorkingDays(List<WorkingDayJson> workingDays) {
        if (workingDays == null || workingDays.isEmpty()) {
            return List.of();
        }

        return workingDays.stream()
                .map(item -> WorkingDayJson.builder()
                        .dayOfWeek(item.getDayOfWeek())
                        .isWorking(item.getIsWorking())
                        .build())
                .toList();
    }

    private void validateWorkingDays(List<WorkingDayJson> workingDays) {
        if (workingDays == null || workingDays.isEmpty()) {
            return;
        }

        Set<DayOfWeek> uniqueDays = workingDays.stream()
                .map(WorkingDayJson::getDayOfWeek)
                .collect(Collectors.toSet());

        if (uniqueDays.size() != workingDays.size()) {
            throw new BadRequestAlertException(
                    "Duplicate working day entries",
                    "practitionerWorkingDay",
                    "duplicateworkingday"
            );
        }
    }

    @Transactional(readOnly = true)
    public Page<Practitioner> findAll(Pageable pageable) {
        return practitionerRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Practitioner> findByFacilityId(Long facilityId, Pageable pageable) {
        return practitionerRepository.findByFacilityId(facilityId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Practitioner> findBySpecialty(Specialty specialty, Pageable pageable) {
        return practitionerRepository.findBySpecialty(specialty, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Practitioner> findBySubSpecialty(String specialty, Pageable pageable) {
        return practitionerRepository.findBySubSpecialtyAndIsActiveTrue(specialty, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Practitioner> findByFirstNameOrLastName(String name, Pageable pageable) {
        return practitionerRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(name, name, pageable);
    }

    public Page<Practitioner> findActiveAppointable(Pageable pageable) {
        LOG.debug("Fetching Active Appointable  Practitionerpageable={} is", pageable);
        return practitionerRepository.findByIsActiveTrueAndAppointableTrue(pageable);
    }

    public Page<Practitioner> findActiveAppointableBasedOnLoggedInFacility(Pageable pageable) {
        LOG.debug("Fetching Active Appointable  Practitionerpageable={} is", pageable);
        Long facilityId = getFacility();
        return practitionerRepository.findByIsActiveTrueAndAppointableTrueAndFacility_Id(facilityId, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Practitioner> findOne(Long id) {
        return practitionerRepository.findById(id);
    }

    public Optional<Practitioner> toggleIsActive(Long id) {
        return practitionerRepository.findById(id)
                .map(p -> {
                    p.setIsActive(!Boolean.TRUE.equals(p.getIsActive()));
                    return practitionerRepository.save(p);
                });
    }

    @Transactional(readOnly = true)
    public List<Practitioner> findByIds(List<Long> ids) {
        return practitionerRepository.findAllById(ids);
    }

    @Transactional(readOnly = true)
    public Optional<Practitioner> findByUser(Long userId) {
        return practitionerRepository.findByUserId(userId);
    }
    @Transactional(readOnly = true)
    public Page<Practitioner> findAllActive(Pageable pageable) {
        return practitionerRepository.findByIsActiveTrue(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Practitioner> findSpecialistPractitionersByFacilityAndSubSpecialty(
            Long facilityId,
            String subSpecialty,
            Pageable pageable
    ) {
        if (facilityId == null) {
            throw new BadRequestAlertException(
                    "Facility id is required",
                    "practitioner",
                    "facility.required"
            );
        }

        if (subSpecialty == null || subSpecialty.isBlank()) {
            throw new BadRequestAlertException(
                    "Sub specialty is required",
                    "practitioner",
                    "subSpecialty.required"
            );
        }

        return practitionerRepository
                .findByFacilityIdAndSubSpecialtyAndSpecialtyAndUserIdIsNotNullAndIsActiveTrue(
                        facilityId,
                        subSpecialty,
                        Specialty.SPECIALIST,
                        pageable
                );
    }

    @Transactional(readOnly = true)
    public Page<Practitioner> findPractitionerByDepartment(Long departmentId, Pageable pageable) {
        LOG.debug("Fetching paged practitioner by department departmentId={} pageable={}", departmentId, pageable);

        List<PractitionerDepartment> items =
                practitionerDepartmentRepository.findByDepartmentIdAndPractitioner_AppointableIsTrueAndPractitioner_IsActiveIsTrue(departmentId);

        if (items == null || items.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> practitionerIds = items.stream()
                .map(serviceItems -> serviceItems.getPractitioner().getId())
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (practitionerIds.isEmpty()) {
            return Page.empty(pageable);
        }

        return practitionerRepository.findByIdIn(practitionerIds, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Practitioner> findSpecialistPractitionersByDepartment(Long departmentId, Pageable pageable) {
        LOG.debug("Fetching paged specialist practitioners by department departmentId={} pageable={}", departmentId, pageable);

        if (departmentId == null) {
            throw new BadRequestAlertException("Department ID is required", "practitioner", "departmentid.required");
        }

        List<PractitionerDepartment> items =
                practitionerDepartmentRepository.findByDepartmentIdAndPractitioner_IsActiveIsTrue(departmentId);

        if (items == null || items.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> practitionerIds = items.stream()
                .map(pd -> pd.getPractitioner().getId())
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (practitionerIds.isEmpty()) {
            return Page.empty(pageable);
        }

        return practitionerRepository.findByIdInAndSpecialtyAndIsActiveTrue(
                practitionerIds,
                Specialty.SPECIALIST,
                pageable
        );
    }

    @Transactional(readOnly = true)
    public Page<Practitioner> findActiveByFacilityId(Long facilityId, Pageable pageable) {
        LOG.debug("Fetching ACTIVE Practitioners by facilityId={} pageable={}", facilityId, pageable);
        return practitionerRepository.findByFacilityIdAndIsActiveTrue(facilityId, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Practitioner> findByUserLogin(String login) {
        return practitionerRepository.findByUser_LoginIgnoreCase(login);
    }

    private Long getFacility() {

        return SecurityUtils.getCurrentUserFacility()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing mandatory claim 'tenant' in JWT."));

    }

    @Transactional(readOnly = true)
    public Optional<Practitioner> resolvePractitioner(Long practitionerId, String login) {
        if (practitionerId != null) {
            Optional<Practitioner> practitioner = practitionerRepository.findById(practitionerId);
            if (practitioner.isPresent()) {
                return practitioner;
            }
        }

        if (login != null && !login.isBlank()) {
            return practitionerRepository.findByUser_LoginIgnoreCase(login.trim());
        }

        return Optional.empty();
    }
}
