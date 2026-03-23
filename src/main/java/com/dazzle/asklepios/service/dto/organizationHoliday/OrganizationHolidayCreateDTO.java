package com.dazzle.asklepios.service.dto.organizationHoliday;

import com.dazzle.asklepios.domain.enumeration.HolidayType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record OrganizationHolidayCreateDTO(

        @NotNull Long organizationDefinitionId,
        @NotBlank String name,
        @NotNull HolidayType holidayType,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        String reason,
        @NotNull Boolean isActive,
        @NotNull Boolean allFacilities,
        String facilityIds,
        @NotNull Boolean recurring
) {
}