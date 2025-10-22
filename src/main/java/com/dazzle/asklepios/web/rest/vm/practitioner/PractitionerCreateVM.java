package com.dazzle.asklepios.web.rest.vm.practitioner;

import com.dazzle.asklepios.domain.Practitioner;
import com.dazzle.asklepios.domain.enumeration.Gender;
import com.dazzle.asklepios.domain.enumeration.Specialty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * View Model for creating a Practitioner via REST.
 */
public record PractitionerCreateVM(
        Long facilityId,
        @NotEmpty String firstName,
        @NotEmpty String lastName,
        @Email  String email,
        String phoneNumber,
        @NotNull Specialty specialty,
        String subSpecialty,
        String defaultMedicalLicense,
        String secondaryMedicalLicense,
        String educationalLevel,
        Boolean appointable,
       Long userId,
        LocalDate defaultLicenseValidUntil,
        LocalDate secondaryLicenseValidUntil,
        LocalDate dateOfBirth,
        String jobRole,
        Gender gender,
        Boolean isActive
) implements Serializable {

    public static PractitionerCreateVM ofEntity(Practitioner practitioner) {
        return new PractitionerCreateVM(
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
                practitioner.getIsActive()
        );
    }
}
