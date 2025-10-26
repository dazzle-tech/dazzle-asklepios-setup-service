package com.dazzle.asklepios.web.rest.vm;

import com.dazzle.asklepios.domain.DepartmentMedicalSheetsNurseVisbility;
import com.dazzle.asklepios.domain.enumeration.MedicalSheets;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record DepartmentMedicalSheetsNurseVisibilityVM (
        @NotNull Long departmentId,
        @NotNull MedicalSheets medicalSheet
) implements Serializable {

    public static DepartmentMedicalSheetsNurseVisibilityVM ofEntity(DepartmentMedicalSheetsNurseVisbility entity) {
        return new DepartmentMedicalSheetsNurseVisibilityVM(
                entity.getDepartmentId(),
                entity.getMedicalSheet()

        );
    }}

