package com.dazzle.asklepios.web.rest.vm.department;

import com.dazzle.asklepios.domain.Department;
import java.io.Serializable;

/**
 * Minimal public read model for patient-facing department selection.
 * Exposes only id and display name.
 */
public record DepartmentPublicOptionVM(Long id, String name) implements Serializable {

    public static DepartmentPublicOptionVM ofEntity(Department department) {
        return new DepartmentPublicOptionVM(department.getId(), department.getName());
    }
}
