package com.dazzle.asklepios.web.rest.vm;

import com.dazzle.asklepios.domain.Encounter;
import com.dazzle.asklepios.domain.enumeration.Resource;
import com.dazzle.asklepios.domain.enumeration.Status;
import com.dazzle.asklepios.domain.enumeration.Visit;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * View Model لردود Encounter.
 * نفس حقول EncounterCreateVM بالضبط + id.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EncounterResponseVM(
        Long     id,
        Long     patientId,
        Resource resourceType,
        Visit    visitType,
        String   age,
        Status   status,
        PatientResponseVM patient

) implements Serializable {

    /** تحويل من Entity إلى Response VM */
    public static EncounterResponseVM ofEntity(Encounter encounter) {
        return new EncounterResponseVM(
                encounter.getId(),
                encounter.getPatient() != null ? encounter.getPatient().getId() : null,
                encounter.getResourceType(),
                encounter.getVisitType(),
                encounter.getAge(),
                encounter.getStatus(),
                PatientResponseVM.ofEntity(encounter.getPatient())

        );
    }
}
