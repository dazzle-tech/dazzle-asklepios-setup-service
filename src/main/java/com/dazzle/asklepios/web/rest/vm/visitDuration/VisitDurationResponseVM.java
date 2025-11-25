package com.dazzle.asklepios.web.rest.vm.visitDuration;

import com.dazzle.asklepios.domain.VisitDuration;
import com.dazzle.asklepios.domain.enumeration.VisitType;

import java.io.Serializable;

public record VisitDurationResponseVM(
        Long id,
        VisitType visitType,
        Integer durationInMinutes,
        Boolean resourceSpecific
) implements Serializable {

    public static VisitDurationResponseVM ofEntity(VisitDuration entity) {
        return new VisitDurationResponseVM(
                entity.getId(),
                entity.getVisitType(),
                entity.getDurationInMinutes(),
                entity.getResourceSpecific()
        );
    }
}
