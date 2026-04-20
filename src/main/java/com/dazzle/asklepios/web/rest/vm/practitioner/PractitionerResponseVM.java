package com.dazzle.asklepios.web.rest.vm.practitioner;

import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.Practitioner;
import com.dazzle.asklepios.domain.enumeration.Gender;
import com.dazzle.asklepios.domain.enumeration.JobRole;
import com.dazzle.asklepios.domain.enumeration.Specialty;
import com.dazzle.asklepios.service.dto.workingDay.WorkingDayJson;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * View Model for reading a Practitioner via REST.
 */
public record PractitionerResponseVM(
        Long id,
        Long facilityId,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        Specialty specialty,
        String subSpecialty,
        String defaultMedicalLicense,
        String secondaryMedicalLicense,
        String educationalLevel,
        Boolean appointable,
        Long userId,
        LocalDate defaultLicenseValidUntil,
        LocalDate secondaryLicenseValidUntil,
        LocalDate dateOfBirth,
        JobRole jobRole,
        Gender gender,
        Boolean isActive,
        Facility facility,
        Integer parallelCapacityValue,
        Integer defaultDurationMinutes,
        Integer defaultBufferBeforeMinutes,
        Integer defaultBufferAfterMinutes,
        List<WorkingDayJson> workingDays
) implements Serializable {

    public static PractitionerResponseVM ofEntity(Practitioner practitioner) {
        return new PractitionerResponseVM(
                practitioner.getId(),
                practitioner.getFacility() != null ? practitioner.getFacility().getId() : null,
                practitioner.getFirstName(),
                practitioner.getLastName(),
                practitioner.getEmail(),
                practitioner.getPhoneNumber(),
                practitioner.getSpecialty(),
                practitioner.getSubSpecialty(),
                practitioner.getDefaultMedicalLicense(),
                practitioner.getSecondaryMedicalLicense(),
                practitioner.getEducationalLevel(),
                practitioner.getAppointable(),
                practitioner.getUser() != null ? practitioner.getUser().getId() : null,
                practitioner.getDefaultLicenseValidUntil(),
                practitioner.getSecondaryLicenseValidUntil(),
                practitioner.getDateOfBirth(),
                practitioner.getJobRole(),
                practitioner.getGender(),
                practitioner.getIsActive(),
                practitioner.getFacility(),
                practitioner.getParallelCapacityValue(),
                practitioner.getDefaultDurationMinutes(),
                practitioner.getDefaultBufferBeforeMinutes(),
                practitioner.getDefaultBufferAfterMinutes(),
                practitioner.getWorkingDays()
        );
    }
}
