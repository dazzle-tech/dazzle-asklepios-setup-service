package com.dazzle.asklepios.web.rest.vm.department;

import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.enumeration.DepartmentType;
import com.dazzle.asklepios.domain.enumeration.EncounterType;
import com.dazzle.asklepios.service.dto.workingDay.WorkingDayJson;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.List;

/**
 * View Model for creating a Department via REST.
 **/
public record DepartmentCreateVM(
        @NotEmpty String name,
        @NotNull Long facilityId,
        @NotNull DepartmentType departmentType,
        Boolean appointable,
        String departmentCode,
        String phoneNumber,
        @Email
        String email,
        EncounterType encounterType,
        Boolean isActive,
        String createdBy,
        Boolean hasMedicalSheets,
        Boolean hasNurseMedicalSheets,
        Boolean parallelCapacityEnabled,
        Integer parallelCapacityValue,
        Integer defaultDurationMinutes,
        Integer defaultBufferBeforeMinutes,
        Integer defaultBufferAfterMinutes,
        Boolean requirePractitioner,
        Boolean requireBilling,
        Boolean requirePreAssessment,
        List<WorkingDayJson> workingDays
) implements Serializable {

        public static DepartmentCreateVM ofEntity(Department department) {
                return new DepartmentCreateVM(
                        department.getName(),
                        department.getFacility() != null ? department.getFacility().getId() : null,
                        department.getType(),
                        department.getAppointable(),
                        department.getCode(),
                        department.getPhoneNumber(),
                        department.getEmail(),
                        department.getEncounterType(),
                        department.getIsActive(),
                        department.getCreatedBy(),
                        department.getHasMedicalSheets(),
                        department.getHasNurseMedicalSheets() ,
                        department.getParallelCapacityEnabled(),
                        department.getParallelCapacityValue(),
                        department.getDefaultDurationMinutes(),
                        department.getDefaultBufferBeforeMinutes(),
                        department.getDefaultBufferAfterMinutes(),
                        department.getRequirePractitioner(),
                        department.getRequireBilling(),
                        department.getRequirePreAssessment(),
                        department.getWorkingDays()
                );
        }
}
