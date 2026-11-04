package com.dazzle.asklepios.web.rest.vm.facility;

import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.enumeration.FacilityType;

import java.io.Serializable;

/**
 * Lightweight facility payload for nested API responses.
 * Contains only simple non-relational fields to avoid circular serialization.
 */
public record FacilityDTO(
        Long id,
        String name,
        String code,
        FacilityType type,
        String emailAddress,
        String phone1,
        String phone2,
        Boolean isActive,
        String timeZone
) implements Serializable {

    public static FacilityDTO ofEntity(Facility facility) {
        if (facility == null) {
            return null;
        }

        return new FacilityDTO(
                facility.getId(),
                facility.getName(),
                facility.getCode(),
                facility.getType(),
                facility.getEmailAddress(),
                facility.getPhone1(),
                facility.getPhone2(),
                facility.getIsActive(),
                facility.getTimeZone()
        );
    }
}

