package com.dazzle.asklepios.web.rest.vm;

import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.enumeration.DepartmentType;
import com.dazzle.asklepios.domain.enumeration.EncounterType;
import java.io.Serializable;

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
        Boolean isActive


) implements Serializable {

        public static DepartmentResponseVM ofEntity(Department department) {

                return new DepartmentResponseVM(
                        department.getId(),
                        department.getFacility()!=null? department.getFacility().getId():null ,
                        department.getName(),
                        department.getDepartmentType(),
                        department.getAppointable(),
                        department.getDepartmentCode(),
                        department.getPhoneNumber(),
                        department.getEmail(),
                        department.getEncounterType(),
                        department.getIsActive()
                );
        }
}
