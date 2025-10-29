package com.dazzle.asklepios.web.rest.vm;

import com.dazzle.asklepios.domain.Encounter;
import com.dazzle.asklepios.domain.Patient;
import com.dazzle.asklepios.domain.enumeration.Resource;
import com.dazzle.asklepios.domain.enumeration.Status;
import com.dazzle.asklepios.domain.enumeration.Visit;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * View Model for creating an Encounter via REST.
 * يستقبل القيم اللازمة من JSON بدل ما نرسل الـEntity نفسها.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EncounterCreateVM(

        @NotNull Resource resourceType,
        @NotNull Visit    visitType,
        String            age,
        Status            status
) implements Serializable {

    /** تحويل من Entity إلى VM (للاستخدام في الردود لو حبيت) */
    public static EncounterCreateVM ofEntity(Encounter encounter) {
        return new EncounterCreateVM(

                encounter.getResourceType(),
                encounter.getVisitType(),
                encounter.getAge(),
                encounter.getStatus()
        );
    }

}
