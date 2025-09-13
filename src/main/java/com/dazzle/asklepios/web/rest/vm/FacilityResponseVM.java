package com.dazzle.asklepios.web.rest.vm;

import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.FacilityType;
import java.io.Serializable;

/**
 * View Model for reading a Facility via REST.
 */
public record FacilityResponseVM(
        Long id,
        String name,
        FacilityType type,
        String emailAddress,
        String phone1,
        String phone2,
        String fax,
        String addressId,
        Currency defaultCurrency
) implements Serializable {

        public static FacilityResponseVM ofEntity(Facility facility) {
                return new FacilityResponseVM(
                        facility.getId(),
                        facility.getName(),
                        facility.getType(),
                        facility.getEmailAddress(),
                        facility.getPhone1(),
                        facility.getPhone2(),
                        facility.getFax(),
                        facility.getAddressId(),
                        facility.getDefaultCurrency()
                );
        }
}
