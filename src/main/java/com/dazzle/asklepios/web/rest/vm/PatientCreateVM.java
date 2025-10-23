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
 * View Model for creating a Patient via REST.
 * يستقبل القيم اللازمة من JSON لإنشاء مريض جديد.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PatientCreateVM(
        @NotEmpty String    firstName,
        @NotEmpty String    lastName,
        LocalDate           dateOfBirth,
        @NotEmpty Gender gender,
        @NotEmpty @Email String email
) implements Serializable {

    /** تحويل من Entity إلى VM (للاستخدام في الردود) */
    public static PatientCreateVM ofEntity(Patient patient) {
        return new PatientCreateVM(
                patient.getFirstName(),
                patient.getLastName(),
                patient.getDateOfBirth(),
                patient.getGender(),
                patient.getEmail()
        );
    }

//    /**
//     * يطبّق قيم الـVM على Entity موجود (أو جديد).
//     */
//    public Patient applyTo(Patient target) {
//        if (target == null) target = new Patient();
//
//        target.setFirstName(firstName);
//        target.setLastName(lastName);
//        target.setDateOfBirth(dateOfBirth);
//        target.setGender(gender);
//        target.setEmail(email);
//
//        return target;
//    }
//
//    /** تحويل مباشر إلى Entity جديد */
//    public Patient toEntity() {
//        return applyTo(null);
//    }
}