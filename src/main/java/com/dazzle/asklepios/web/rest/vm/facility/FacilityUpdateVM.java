package com.dazzle.asklepios.web.rest.vm.facility;

import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.FacilityType;
import com.dazzle.asklepios.service.dto.workingDay.WorkingDayJson;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import org.wildfly.common.annotation.NotNull;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * View Model for updating a Facility via REST.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FacilityUpdateVM(
        @NotNull Long id,
        String name,
        @NotNull String code,
        @NotNull FacilityType type,
        @Email String emailAddress,
        String phone1,
        String phone2,
        String fax,
        String addressId,
        @NotNull Currency defaultCurrency,
        Boolean isActive,
        Long ruleId,
        LocalDate registrationDate,
        String timeZone,
        List<WorkingDayJson> workingDays,
        Long defaultLabDepartmentId,
        Long defaultRadDepartmentId,
        boolean approvingDiagnosticTestSettlePayment
) implements Serializable {

    public static FacilityUpdateVM ofEntity(Facility facility) {
        return new FacilityUpdateVM(
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
                facility.getIsActive(),
                facility.getRuleId(),
                facility.getRegistrationDate(),
                facility.getTimeZone(),
                facility.getWorkingDays(),
                facility.getDefaultLabDepartment() != null ? facility.getDefaultLabDepartment().getId() : null,
                facility.getDefaultRadDepartment() != null ? facility.getDefaultRadDepartment().getId() : null,
                facility.getApprovingDiagnosticTestSettlePayment()

        );
    }
}
