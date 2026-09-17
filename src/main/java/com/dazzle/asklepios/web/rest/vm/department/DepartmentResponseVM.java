package com.dazzle.asklepios.web.rest.vm.department;

import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.enumeration.AgeUnit;
import com.dazzle.asklepios.domain.enumeration.DepartmentType;
import com.dazzle.asklepios.domain.enumeration.EncounterType;
import com.dazzle.asklepios.service.dto.workingDay.WorkingDayJson;

import java.io.Serializable;
import java.util.List;

/**
 * View Model for reading a Department via REST.
 */
public record DepartmentResponseVM(
        Long id,
        Long facilityId,
        String name,
        DepartmentType departmentType,
        Boolean appointable,
        String departmentCode,
        String phoneNumber,
        String email,
        EncounterType encounterType,
        Boolean isActive,
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
        Boolean ageSpecific,
        Integer fromAge,
        AgeUnit fromAgeUnit,
        Integer toAge,
        AgeUnit toAgeUnit,
        List<WorkingDayJson> workingDays

) implements Serializable {

        public static DepartmentResponseVM ofEntity(Department department) {

                return new DepartmentResponseVM(
                        department.getId(),
                        department.getFacility()!=null? department.getFacility().getId():null ,
                        department.getName(),
                        department.getType(),
                        department.getAppointable(),
                        department.getCode(),
                        department.getPhoneNumber(),
                        department.getEmail(),
                        department.getEncounterType(),
                        department.getIsActive(),
                        department.getHasMedicalSheets(),
                        department.getHasNurseMedicalSheets(),
                        department.getParallelCapacityEnabled(),
                        department.getParallelCapacityValue(),
                        department.getDefaultDurationMinutes(),
                        department.getDefaultBufferBeforeMinutes(),
                        department.getDefaultBufferAfterMinutes(),
                        department.getRequirePractitioner(),
                        department.getRequireBilling(),
                        department.getRequirePreAssessment(),
                        department.getAgeSpecific(),
                        department.getFromAge(),
                        department.getFromAgeUnit(),
                        department.getToAge(),
                        department.getToAgeUnit(),
                        department.getWorkingDays()
                );
        }
}
