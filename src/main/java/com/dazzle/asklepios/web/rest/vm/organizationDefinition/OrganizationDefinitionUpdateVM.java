package com.dazzle.asklepios.web.rest.vm.organizationDefinition;

import com.dazzle.asklepios.domain.OrganizationDefinition;
import com.dazzle.asklepios.domain.enumeration.TimeZone;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record OrganizationDefinitionUpdateVM(
        @NotNull Long id,
        @NotNull String name,
        String description,
        String address,
        String contactName,
        String contactAddress,
        String contactEmail,
        String contactMobile,
        String contactLandNumber,
        @NotNull BigDecimal taxValue,
        @NotNull TimeZone defaultTimeZone,
        @NotNull Long defaultLanguageId
) {
    public static OrganizationDefinitionUpdateVM ofEntity(OrganizationDefinition entity) {
        if (entity == null) return null;
        return new OrganizationDefinitionUpdateVM(
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
                entity.getDefaultLanguage()!=null?entity.getDefaultLanguage().getId():null
        );
    }
}
