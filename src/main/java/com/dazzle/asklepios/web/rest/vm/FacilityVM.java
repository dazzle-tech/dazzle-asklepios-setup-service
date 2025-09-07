package com.dazzle.asklepios.web.rest.vm;

import com.dazzle.asklepios.domain.Facility;
import jakarta.validation.constraints.NotNull;

/**
 *  View Model for creating/updating a Facility via REST.
 */
public record FacilityVM(
        @NotNull String name,
        @NotNull String type,
        String emailAddress,
        String phone1,
        String phone2,
        String fax,
        String addressId,
        String defaultCurrencyLkey
) {
        public static FacilityVM ofEntity(Facility facility) {
                return new FacilityVM(
                        facility.getName(),
                        facility.getType(),
                        facility.getEmailAddress(),
                        facility.getPhone1(),
                        facility.getPhone2(),
                        facility.getFax(),
                        facility.getAddressId(),
                        facility.getDefaultCurrencyLkey()
                );
        }
}
