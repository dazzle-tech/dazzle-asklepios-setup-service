package com.dazzle.asklepios.web.rest.vm;

import com.dazzle.asklepios.domain.Facility;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 *  View Model for creating/updating a Facility via REST.
 */
@Getter
@ToString
@Builder
public class FacilityVM {

        @NotNull
        private final String name;

        @NotNull
        private final String type;

        private final String emailAddress;

        private final String phone1;

        private final String phone2;

        private final String fax;

        private final String addressId;

        private final String defaultCurrencyLkey;

        @NotNull
        private final Boolean isValid;
        public static FacilityVM ofEntity(Facility facility) {
                return FacilityVM.builder()
                        .name(facility.getName())
                        .type(facility.getType())
                        .emailAddress(facility.getEmailAddress())
                        .phone1(facility.getPhone1())
                        .phone2(facility.getPhone2())
                        .fax(facility.getFax())
                        .addressId(facility.getAddressId())
                        .defaultCurrencyLkey(facility.getDefaultCurrencyLkey())
                        .build();
        }
}
