package com.dazzle.asklepios.web.rest.vm.facility;

import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.FacilityType;
import com.dazzle.asklepios.service.dto.workingDay.WorkingDayJson;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * View Model for creating a Facility via REST.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FacilityCreateVM(
        @NotNull String name,
        @NotNull String code,
        @NotNull FacilityType type,
        @Email String emailAddress,
        String phone1,
        String phone2,
        String fax,
        String addressId,
        @NotNull Currency defaultCurrency,
        Boolean isActive,
        LocalDate registrationDate,
        String timeZone,
        List<WorkingDayJson> workingDays,
        Long defaultLabDepartmentId,
        Long defaultRadDepartmentId
) implements Serializable {

    public static FacilityCreateVM ofEntity(Facility facility) {
        return new FacilityCreateVM(
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
                facility.getRegistrationDate(),
                facility.getTimeZone(),
                facility.getWorkingDays(),
                facility.getDefaultLabDepartment() != null ? facility.getDefaultLabDepartment().getId() : null,
                facility.getDefaultRadDepartment() != null ? facility.getDefaultRadDepartment().getId() : null
        );
    }
}