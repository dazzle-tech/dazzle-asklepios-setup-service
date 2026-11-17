package com.dazzle.asklepios.web.rest.vm.mobileAppConfig;

import com.dazzle.asklepios.domain.MobileAppConfig;
import com.dazzle.asklepios.domain.enumeration.MobileAppConfigKey;
import com.dazzle.asklepios.domain.enumeration.MobileAppConfigType;

import java.time.Instant;
import java.util.List;

public record MobileAppConfigResponseVM(
        Long id,
        MobileAppConfigKey configKey,
        MobileAppConfigType configType,
        String configValue,
        List<String> configValues,
        String description,
        String createdBy,
        Instant createdDate,
        String lastModifiedBy,
        Instant lastModifiedDate
) {
    public static MobileAppConfigResponseVM ofEntity(
            MobileAppConfig entity,
            String resolvedValue,
            List<String> resolvedValues
    ) {
        if (entity == null) {
            return null;
        }
        return new MobileAppConfigResponseVM(
                entity.getId(),
                entity.getConfigKey(),
                entity.getConfigType(),
                resolvedValue,
                resolvedValues,
                entity.getDescription(),
                entity.getCreatedBy(),
                entity.getCreatedDate(),
                entity.getLastModifiedBy(),
                entity.getLastModifiedDate()
        );
    }
}
