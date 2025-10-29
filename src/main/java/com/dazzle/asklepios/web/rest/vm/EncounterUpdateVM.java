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
 * View Model for updating an Encounter via REST.
 * يستقبل القيم اللازمة من JSON بدل ما نرسل الـEntity نفسها.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EncounterUpdateVM(
        @NotNull Long     id,
        @NotNull Long     patientId,
        @NotNull Resource resourceType,
        @NotNull Visit    visitType,
        String            age,
        Status            status
) implements Serializable {

    /** تحويل من Entity إلى VM (للاستخدام في الردود لو حبيت) */
    public static EncounterUpdateVM ofEntity(Encounter encounter) {
        return new EncounterUpdateVM(
                encounter.getId(),                                               // إضافة الـID
                encounter.getPatient() != null ? encounter.getPatient().getId() : null,
                encounter.getResourceType(),
                encounter.getVisitType(),
                encounter.getAge(),
                encounter.getStatus()
        );
    }

}