package com.dazzle.asklepios.web.rest.vm;

import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.FacilityType;
import org.wildfly.common.annotation.NotNull;

import java.io.Serializable;

/**
 * View Model for reading a Facility via REST.
 */
public record FacilityResponseVM(
        Long id,
        String name,
        @NotNull String code,
        FacilityType type,
        String emailAddress,
        String phone1,
        String phone2,
        String fax,
        String addressId,
        Currency defaultCurrency,
        Boolean isActive,
        Long ruleId
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
                        facility.getIsActive(),
                        facility.getRuleId()
                );
        }
}
