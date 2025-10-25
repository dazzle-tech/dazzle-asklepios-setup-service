package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.Practitioner;
import com.dazzle.asklepios.domain.User;
import com.dazzle.asklepios.domain.enumeration.Specialty;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.repository.PractitionersRepository;
import com.dazzle.asklepios.repository.UserRepository;
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

import java.util.Optional;

@Service
@Transactional
public class PractitionerService {

    private static final Logger LOG = LoggerFactory.getLogger(PractitionerService.class);
    private final PractitionersRepository practitionerRepository;
    private final FacilityRepository facilityRepository;
    private final UserRepository userRepository;

    public PractitionerService(PractitionersRepository practitionerRepository,
                               FacilityRepository facilityRepository,
                               UserRepository userRepository) {
        this.practitionerRepository = practitionerRepository;
        this.facilityRepository = facilityRepository;
        this.userRepository = userRepository;
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
            user = userRepository.findById(vm.userId())
                    .orElse(null);
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
                .build();

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
            userRepository.findById(vm.userId()).ifPresent(practitioner::setUser);
        } else {
            practitioner.setUser(null);
        }

    
        if (vm.firstName() != null) practitioner.setFirstName(vm.firstName());
        if (vm.lastName() != null) practitioner.setLastName(vm.lastName());
        if (vm.email() != null && !vm.email().isBlank()) {
            practitioner.setEmail(vm.email());
        } else {
            practitioner.setEmail(null);
        }
        if (vm.phoneNumber() != null) practitioner.setPhoneNumber(vm.phoneNumber());
        if (vm.specialty() != null) practitioner.setSpecialty(vm.specialty());
        if (vm.subSpecialty() != null) practitioner.setSubSpecialty(vm.subSpecialty());
        if (vm.defaultMedicalLicense() != null) practitioner.setDefaultMedicalLicense(vm.defaultMedicalLicense());
        if (vm.secondaryMedicalLicense() != null) practitioner.setSecondaryMedicalLicense(vm.secondaryMedicalLicense());
        if (vm.educationalLevel() != null) practitioner.setEducationalLevel(vm.educationalLevel());
        if (vm.appointable() != null) practitioner.setAppointable(vm.appointable());
        if (vm.defaultLicenseValidUntil() != null) practitioner.setDefaultLicenseValidUntil(vm.defaultLicenseValidUntil());
        if (vm.secondaryLicenseValidUntil() != null) practitioner.setSecondaryLicenseValidUntil(vm.secondaryLicenseValidUntil());
        if (vm.dateOfBirth() != null) practitioner.setDateOfBirth(vm.dateOfBirth());
        if (vm.jobRole() != null) practitioner.setJobRole(vm.jobRole());
        if (vm.gender() != null) practitioner.setGender(vm.gender());
        if (vm.isActive() != null) practitioner.setIsActive(vm.isActive());

        Practitioner updated = practitionerRepository.save(practitioner);
        LOG.debug("Updated Practitioner successfully: {}", updated);

        return Optional.of(updated);
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
}
