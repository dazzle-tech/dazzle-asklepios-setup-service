package com.dazzle.asklepios.web.rest.vm.facility;

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
        boolean approvingDiagnosticTestSettlePayment,
        Long countryId,
        Long districtId,
        String streetAddress,
        String postalCode
) implements Serializable {
}
