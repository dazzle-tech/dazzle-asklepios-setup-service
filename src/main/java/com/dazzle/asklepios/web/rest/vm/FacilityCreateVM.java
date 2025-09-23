package com.dazzle.asklepios.web.rest.vm;

import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.FacilityType;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * View Model for creating a Facility via REST.
 */
public record FacilityCreateVM(
        @NotNull String name,
        @NotNull FacilityType type,
        String emailAddress,
        String phone1,
        String phone2,
        String fax,
        String addressId,
        Currency defaultCurrency,
        Boolean isActive
) implements Serializable {

        public static FacilityCreateVM ofEntity(Facility facility) {
                return new FacilityCreateVM(
                        facility.getName(),
                        facility.getType(),
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
