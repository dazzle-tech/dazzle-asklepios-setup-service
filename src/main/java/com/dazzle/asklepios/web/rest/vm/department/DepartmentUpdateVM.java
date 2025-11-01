package com.dazzle.asklepios.web.rest.vm.department;

import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.enumeration.DepartmentType;
import com.dazzle.asklepios.domain.enumeration.EncounterType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * View Model for updating a Department via REST.
 */
public record DepartmentUpdateVM(
        @NotNull Long id,
        @NotEmpty String name,
        @NotNull Long facilityId,
        @NotNull DepartmentType departmentType,
        Boolean appointable,
        String departmentCode,
        String phoneNumber,
        @Email
        @Size(min = 5, max = 254)
        String email,
        EncounterType encounterType,
        Boolean isActive,
        Boolean hasMedicalSheets,
        Boolean hasNurseMedicalSheets
) implements Serializable {

        public static DepartmentUpdateVM ofEntity(Department department) {
                return new DepartmentUpdateVM(
                        department.getId(),
                        department.getName(),
                        department.getFacility() != null ? department.getFacility().getId() : null,
                        department.getType(),
                        department.getAppointable(),
                        department.getCode(),
                        department.getPhoneNumber(),
                        department.getEmail(),
                        department.getEncounterType(),
                        department.getIsActive(),
                        department.getHasMedicalSheets(),
                        department.getHasNurseMedicalSheets()
                );
        }
}
