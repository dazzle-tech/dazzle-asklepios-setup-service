package com.dazzle.asklepios.web.rest.vm;

import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.FacilityType;

import jakarta.validation.constraints.NotNull;

/**
 *  View Model for creating/updating a Facility via REST.
 */
public record FacilityVM(
        @NotNull String name,
        @NotNull FacilityType type,
        @NotNull String code,
        String emailAddress,
        String phone1,
        String phone2,
        String fax,
        String addressId,
        Currency defaultCurrency,
        Boolean isActive
) {
        public static FacilityVM ofEntity(Facility facility) {
                return new FacilityVM(
                        facility.getName(),
                        facility.getType(),
                        facility.getCode(),
                        facility.getEmailAddress(),
                        facility.getPhone1(),
                        facility.getPhone2(),
                        facility.getFax(),
                        facility.getAddressId(),
                        facility.getDefaultCurrency(),
                        facility.getIsActive()
                );
        }
}
