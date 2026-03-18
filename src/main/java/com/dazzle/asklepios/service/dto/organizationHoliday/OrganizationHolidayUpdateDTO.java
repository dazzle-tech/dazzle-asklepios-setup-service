package com.dazzle.asklepios.service.dto.organizationHoliday;

import com.dazzle.asklepios.domain.enumeration.HolidayType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record OrganizationHolidayUpdateDTO(
        @NotNull Long id,
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
}