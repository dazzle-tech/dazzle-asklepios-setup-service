package com.dazzle.asklepios.web.rest.vm.mobileAppConfig;

import com.dazzle.asklepios.domain.MobileAppConfig;
import com.dazzle.asklepios.domain.enumeration.MobileAppConfigKey;
import com.dazzle.asklepios.domain.enumeration.MobileAppConfigType;

import java.time.Instant;

public record MobileAppConfigResponseVM(
        Long id,
        MobileAppConfigKey configKey,
        MobileAppConfigType configType,
        String configValue,
        String createdBy,
        Instant createdDate,
        String lastModifiedBy,
        Instant lastModifiedDate
) {
    public static MobileAppConfigResponseVM ofEntity(MobileAppConfig entity, String resolvedValue) {
        if (entity == null) {
            return null;
        }
        return new MobileAppConfigResponseVM(
                entity.getId(),
                entity.getConfigKey(),
                entity.getConfigType(),
                resolvedValue,
                entity.getCreatedBy(),
                entity.getCreatedDate(),
                entity.getLastModifiedBy(),
                entity.getLastModifiedDate()
        );
    }
}
