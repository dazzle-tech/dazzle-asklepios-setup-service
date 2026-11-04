package com.dazzle.asklepios.web.rest.vm;


import com.dazzle.asklepios.domain.OrganizationHoliday;
import com.dazzle.asklepios.domain.enumeration.HolidayType;

import java.time.LocalDate;

public record OrganizationHolidayResponseVM(
        Long id,
        Long organizationDefinitionId,
        String name,
        HolidayType holidayType,
        LocalDate startDate,
        LocalDate endDate,
        String reason,
        Boolean isActive,
        Boolean allFacilities,
        String facilityIds,
        Boolean recurring
) {
    public static OrganizationHolidayResponseVM ofEntity(OrganizationHoliday entity) {
        if (entity == null) return null;

        return new OrganizationHolidayResponseVM(
                entity.getId(),
                entity.getOrganizationDefinition() != null ? entity.getOrganizationDefinition().getId() : null,
                entity.getName(),
                entity.getHolidayType(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getReason(),
                entity.getIsActive(),
                entity.getAllFacilities(),
                entity.getFacilityIds(),
                entity.getRecurring()
        );
    }
}
