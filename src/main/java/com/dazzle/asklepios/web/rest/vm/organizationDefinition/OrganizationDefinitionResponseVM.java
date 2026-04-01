package com.dazzle.asklepios.web.rest.vm.organizationDefinition;

import com.dazzle.asklepios.domain.Language;
import com.dazzle.asklepios.domain.OrganizationDefinition;
import com.dazzle.asklepios.domain.enumeration.TimeZone;
import com.dazzle.asklepios.service.dto.workingDay.WorkingDayJson;

import java.math.BigDecimal;
import java.util.List;

public record OrganizationDefinitionResponseVM(
        Long id,
        String name,
        String description,
        String address,
        String contactName,
        String contactAddress,
        String contactEmail,
        String contactMobile,
        String contactLandNumber,
        BigDecimal taxValue,
        TimeZone defaultTimeZone,
        Long defaultLanguageId,
        String defaultLanguageName,
        List<WorkingDayJson> workingDays
) {
    public static OrganizationDefinitionResponseVM ofEntity(OrganizationDefinition entity) {
        if (entity == null) return null;
        return new OrganizationDefinitionResponseVM(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getAddress(),
                entity.getContactName(),
                entity.getContactAddress(),
                entity.getContactEmail(),
                entity.getContactMobile(),
                entity.getContactLandNumber(),
                entity.getTaxValue(),
                entity.getDefaultTimeZone(),
                entity.getDefaultLanguage() != null ? entity.getDefaultLanguage().getId() : null,
                entity.getDefaultLanguage() != null ? entity.getDefaultLanguage().getLangName() : null,
                entity.getWorkingDays()

        );
    }
}
