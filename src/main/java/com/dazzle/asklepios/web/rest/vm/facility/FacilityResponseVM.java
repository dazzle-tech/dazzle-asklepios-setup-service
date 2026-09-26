package com.dazzle.asklepios.web.rest.vm.facility;

import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.FacilityType;
import com.dazzle.asklepios.service.dto.workingDay.WorkingDayJson;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        boolean approvingDiagnosticTestSettlePayment,
        String vatRegistrationNumber,
        Long countryId,
        String countryName,
        Long districtId,
        String districtName,
        String streetAddress,
        String postalCode,
        String facilityAddress
) implements Serializable {

    public static FacilityResponseVM ofEntity(Facility facility) {
        Long countryId = facility.getCountry() != null ? facility.getCountry().getId() : null;
        String countryName = facility.getCountry() != null && facility.getCountry().getName() != null
                ? formatEnumLabel(facility.getCountry().getName().name())
                : null;
        Long districtId = facility.getDistrict() != null ? facility.getDistrict().getId() : null;
        String districtName = facility.getDistrict() != null ? facility.getDistrict().getName() : null;

        String facilityAddress = Stream.of(districtName, countryName, facility.getStreetAddress(), facility.getPostalCode())
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.joining(", "));

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
                Boolean.TRUE.equals(facility.getApprovingDiagnosticTestSettlePayment()),
                facility.getVatRegistrationNumber(),
                countryId,
                countryName,
                districtId,
                districtName,
                facility.getStreetAddress(),
                facility.getPostalCode(),
                facilityAddress.isBlank() ? null : facilityAddress
        );
    }

    private static String formatEnumLabel(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Stream.of(value.split("_"))
                .filter(part -> !part.isBlank())
                .map(part -> part.substring(0, 1).toUpperCase() + part.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }
}
