package com.dazzle.asklepios.web.rest.vm.facility;

import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.FacilityType;
import com.dazzle.asklepios.service.dto.workingDay.WorkingDayJson;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * View Model for reading a Facility via REST.
 */
public record FacilityResponseVM(
        Long id,
        String name,
        String code,
        FacilityType type,
        String emailAddress,
        String phone1,
        String phone2,
        String fax,
        String addressId,
        Currency defaultCurrency,
        LocalDate registrationDate,
        Boolean isActive,
        Long ruleId,
        String timeZone,
        List<WorkingDayJson> workingDays,
        Long defaultLabDepartmentId,
        String defaultLabDepartmentName,
        Long defaultRadDepartmentId,
        String defaultRadDepartmentName,
        boolean approvingDiagnosticTestSettlePayment

) implements Serializable {

    public static FacilityResponseVM ofEntity(Facility facility) {
        return new FacilityResponseVM(
                facility.getId(),
                facility.getName(),
                facility.getCode(),
                facility.getType(),
                facility.getEmailAddress(),
                facility.getPhone1(),
                facility.getPhone2(),
                facility.getFax(),
                facility.getAddressId(),
                facility.getDefaultCurrency(),
                facility.getRegistrationDate(),
                facility.getIsActive(),
                facility.getRuleId(),
                facility.getTimeZone(),
                facility.getWorkingDays(),
                facility.getDefaultLabDepartment() != null ? facility.getDefaultLabDepartment().getId() : null,
                facility.getDefaultLabDepartment() != null ? facility.getDefaultLabDepartment().getName() : null,
                facility.getDefaultRadDepartment() != null ? facility.getDefaultRadDepartment().getId() : null,
                facility.getDefaultRadDepartment() != null ? facility.getDefaultRadDepartment().getName() : null,
                facility.getApprovingDiagnosticTestSettlePayment()
        );
    }
}
