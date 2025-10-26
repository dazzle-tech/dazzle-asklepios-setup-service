package com.dazzle.asklepios.web.rest.vm;

import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.FacilityType;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * View Model for reading a Facility via REST.
 */
public record FacilityResponseVM(
        Long id,
        String name,
        String code,
        FacilityType type,
        String code,
        String emailAddress,
        String phone1,
        String phone2,
        String fax,
        String addressId,
        Currency defaultCurrency,
        Boolean isActive
) implements Serializable {

        public static FacilityResponseVM ofEntity(Facility facility) {
                return new FacilityResponseVM(
                        facility.getId(),
                        facility.getName(),
                        facility.getCode(),
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
