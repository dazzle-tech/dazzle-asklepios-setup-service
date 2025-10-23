package com.dazzle.asklepios.web.rest.vm;

import com.dazzle.asklepios.domain.Patient;
import com.dazzle.asklepios.domain.enumeration.Gender;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * View Model for Patient responses.
 * يُستخدم لإرجاع بيانات المريض في الـresponses.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PatientResponseVM(
        Long id,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        Gender gender,
        String email
) implements Serializable {

    /** تحويل من Entity إلى Response VM */
    public static PatientResponseVM ofEntity(Patient patient) {
        return new PatientResponseVM(
                patient.getId(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getDateOfBirth(),
                patient.getGender(),
                patient.getEmail()
        );
    }
}