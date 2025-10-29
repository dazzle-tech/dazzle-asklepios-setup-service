package com.dazzle.asklepios.web.rest.vm;

import com.dazzle.asklepios.domain.Patient;
import com.dazzle.asklepios.domain.enumeration.Gender;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * View Model for updating a Patient via REST.
 * يستقبل القيم اللازمة من JSON لتحديث مريض موجود.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PatientUpdateVM(
        @NotNull Long       id,
        @NotEmpty String    firstName,
        @NotEmpty String    lastName,
        LocalDate           dateOfBirth,
        @NotNull Gender     gender,
        @NotEmpty @Email String email
) implements Serializable {

    /** تحويل من Entity إلى VM (للاستخدام في الردود) */
    public static PatientUpdateVM ofEntity(Patient patient) {
        return new PatientUpdateVM(
                patient.getId(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getDateOfBirth(),
                patient.getGender(),
                patient.getEmail()
        );
    }
}