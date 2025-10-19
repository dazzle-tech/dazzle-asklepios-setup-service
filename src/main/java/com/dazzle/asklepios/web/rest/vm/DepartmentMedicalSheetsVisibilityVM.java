package com.dazzle.asklepios.web.rest.vm;

import com.dazzle.asklepios.domain.DepartmentMedicalSheetsVisibility;
import com.dazzle.asklepios.domain.enumeration.MedicalSheets;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record DepartmentMedicalSheetsVisibilityVM(
        @NotNull Long departmentId,
        @NotNull MedicalSheets medicalSheet
) implements Serializable {

    public static DepartmentMedicalSheetsVisibilityVM ofEntity(DepartmentMedicalSheetsVisibility entity) {
        return new DepartmentMedicalSheetsVisibilityVM(
                entity.getDepartmentId(),
                entity.getMedicalSheet()

        );
    }}

