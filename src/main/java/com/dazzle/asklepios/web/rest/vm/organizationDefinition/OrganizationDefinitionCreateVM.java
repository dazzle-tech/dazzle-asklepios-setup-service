package com.dazzle.asklepios.web.rest.vm.organizationDefinition;

import com.dazzle.asklepios.domain.OrganizationDefinition;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record OrganizationDefinitionCreateVM(
        @NotNull String name,
        String description,
        String address,
        String contactName,
        String contactAddress,
        String contactEmail,
        String contactMobile,
        String contactLandNumber,
        @NotNull BigDecimal taxValue
) {
    public static OrganizationDefinitionCreateVM ofEntity(OrganizationDefinition entity) {
        if (entity == null) return null;
        return new OrganizationDefinitionCreateVM(
                entity.getName(),
                entity.getDescription(),
                entity.getAddress(),
                entity.getContactName(),
                entity.getContactAddress(),
                entity.getContactEmail(),
                entity.getContactMobile(),
                entity.getContactLandNumber(),
                entity.getTaxValue()
        );
    }
}
