package com.dazzle.asklepios.web.rest.vm.visitDuration;

import com.dazzle.asklepios.domain.VisitDuration;
import com.dazzle.asklepios.domain.enumeration.VisitType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VisitDurationCreateVM(
        @NotNull VisitType visitType,
        @NotNull @PositiveOrZero Integer durationInMinutes,
        @NotNull Boolean resourceSpecific
) implements Serializable {

    public static VisitDurationCreateVM ofEntity(VisitDuration entity) {
        return new VisitDurationCreateVM(
                entity.getVisitType(),
                entity.getDurationInMinutes(),
                entity.getResourceSpecific()
        );
    }
}
