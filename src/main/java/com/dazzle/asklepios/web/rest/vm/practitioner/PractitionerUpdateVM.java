package com.dazzle.asklepios.web.rest.vm.practitioner;

import com.dazzle.asklepios.domain.Practitioner;
import com.dazzle.asklepios.domain.enumeration.Gender;
import com.dazzle.asklepios.domain.enumeration.Specialty;
import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * View Model for updating a Practitioner via REST.
 * Added improved validation annotations for data integrity.
 */
public record PractitionerUpdateVM(


        @NotNull(message = "Facility ID cannot be null")
        Long facilityId,

        @NotBlank(message = "First name cannot be blank")
        @Size(max = 100, message = "First name cannot exceed 100 characters")
        String firstName,

        @NotBlank(message = "Last name cannot be blank")
        @Size(max = 100, message = "Last name cannot exceed 100 characters")
        String lastName,

        @Email String email,


        String phoneNumber,

        @NotNull(message = "Specialty is required")
        Specialty specialty,


        String subSpecialty,


        String defaultMedicalLicense,


        String secondaryMedicalLicense,


        String educationalLevel,


        Boolean appointable,

        Long userId,

        @FutureOrPresent(message = "Default license valid until date must be in the present or future")
        LocalDate defaultLicenseValidUntil,

        @FutureOrPresent(message = "Secondary license valid until date must be in the present or future")
        LocalDate secondaryLicenseValidUntil,

        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth,

        String jobRole,

        Gender gender,

        Boolean isActive
) implements Serializable {

    public static PractitionerUpdateVM ofEntity(Practitioner practitioner) {
        return new PractitionerUpdateVM(

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
