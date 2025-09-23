package com.dazzle.asklepios.web.rest.vm;

import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.enumeration.DepartmentType;
import com.dazzle.asklepios.domain.enumeration.EncounterType;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * View Model for updating a Department via REST.
 */
public record DepartmentUpdateVM(
        @NotNull Long id,
        @NotNull String name,
        @NotNull Long facilityId,
        @NotNull DepartmentType departmentType,
        Boolean appointable,
        String departmentCode,
        String phoneNumber,
        String email,
        EncounterType encounterType,
        Boolean isActive,
        String lastModifiedBy
) implements Serializable {

        public static DepartmentUpdateVM ofEntity(Department department) {
                return new DepartmentUpdateVM(
                        department.getId(),
                        department.getName(),
                        department.getFacility() != null ? department.getFacility().getId() : null,
                        department.getDepartmentType(),
                        department.getAppointable(),
                        department.getDepartmentCode(),
                        department.getPhoneNumber(),
                        department.getEmail(),
                        department.getEncounterType(),
                        department.getIsActive(),
                        department.getLastModifiedBy()
                );
        }
}
