package com.dazzle.asklepios.web.rest.vm.practitionerDepartment;

import com.dazzle.asklepios.domain.PractitionerDepartment;
import java.io.Serializable;

/** View Model for reading Practitioner–Department relations. */
public record PractitionerDepartmentResponseVM(
        Long id,
        Long practitionerId,
        Long departmentId,
        String departmentName,
        String practitionerName
) implements Serializable {

    public static PractitionerDepartmentResponseVM ofEntity(PractitionerDepartment pd) {
        return new PractitionerDepartmentResponseVM(
                pd.getId(),
                pd.getPractitioner() != null ? pd.getPractitioner().getId() : null,
                pd.getDepartment() != null ? pd.getDepartment().getId() : null,
                pd.getDepartment() != null ? pd.getDepartment().getName() : null,
                pd.getPractitioner() !=null ?pd.getPractitioner().getFirstName()+pd.getPractitioner().getLastName():null
        );
    }
}


