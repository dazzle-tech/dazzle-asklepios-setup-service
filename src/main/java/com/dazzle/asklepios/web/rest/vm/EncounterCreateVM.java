package com.dazzle.asklepios.web.rest.vm;

import com.dazzle.asklepios.domain.Encounter;
import com.dazzle.asklepios.domain.Patient;
import com.dazzle.asklepios.domain.enumeration.Resource;
import com.dazzle.asklepios.domain.enumeration.Status;
import com.dazzle.asklepios.domain.enumeration.Visit;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.function.Function;

/**
 * View Model for creating an Encounter via REST.
 * يستقبل القيم اللازمة من JSON بدل ما نرسل الـEntity نفسها.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EncounterCreateVM(
        @NotNull Long     patientId,
        @NotNull Resource resource,
        @NotNull Visit    visit,
        String            age,
        Status            status
) implements Serializable {

    /** تحويل من Entity إلى VM (للاستخدام في الردود لو حبيت) */
    public static EncounterCreateVM ofEntity(Encounter encounter) {
        return new EncounterCreateVM(
                encounter.getPatient() != null ? encounter.getPatient().getId() : null,
                encounter.getResource(),
                encounter.getVisit(),
                encounter.getAge(),
                encounter.getStatus()
        );
    }

//    /**
//     * يطبّق قيم الـVM على Entity موجود (أو جديد) باستثناء ربط الـPatient.
//     * ربط الـPatient يتم عبر الـloader (عادةً: patientRepository::getReferenceById).
//     */
//    public Encounter applyTo(Encounter target, Function<Long, Patient> patientLoader) {
//        if (target == null) target = new Encounter();
//
//        // اربط الـPatient عبر الـloader لتجنب SELECT إضافي
//        if (patientId != null) {
//            target.setPatient(patientLoader.apply(patientId));
//        }
//
//        target.setAge(age);
//        target.setStatus(status);
//        target.setResource(resource);
//        target.setVisit(visit);
//        return target;
//    }
}
