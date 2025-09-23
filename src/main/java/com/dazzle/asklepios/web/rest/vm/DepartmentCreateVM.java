package com.dazzle.asklepios.web.rest.vm;

import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.enumeration.DepartmentType;
import com.dazzle.asklepios.domain.enumeration.EncounterType;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * View Model for creating/updating a Department via REST.
 */
public record DepartmentCreateVM(
        @NotNull String name,
        @NotNull Long facilityId,
        @NotNull DepartmentType departmentType,
        Boolean appointable,
        String departmentCode,
        String phoneNumber,
        String email,
        EncounterType encounterType,
        Boolean isActive,
        String createdBy
) implements Serializable {

        public static DepartmentCreateVM ofEntity(Department department) {
                return new DepartmentCreateVM(
                        department.getName(),
                        department.getFacility() != null ? department.getFacility().getId() : null,
                        department.getDepartmentType(),
                        department.getAppointable(),
                        department.getDepartmentCode(),
                        department.getPhoneNumber(),
                        department.getEmail(),
                        department.getEncounterType(),
                        department.getIsActive(),
                        department.getCreatedBy()
                );
        }
}
