package com.dazzle.asklepios.web.rest.vm.department;

import com.dazzle.asklepios.domain.DepartmentServices;
import com.dazzle.asklepios.domain.enumeration.patient.EncounterReason;

import java.io.Serializable;

public record DepartmentServicesResponseVM(
        Long id,
        EncounterReason service
) implements Serializable {

    public static DepartmentServicesResponseVM ofEntity(DepartmentServices entity) {
        return new DepartmentServicesResponseVM(
                entity.getId(),
                entity.getService()
        );
    }
}