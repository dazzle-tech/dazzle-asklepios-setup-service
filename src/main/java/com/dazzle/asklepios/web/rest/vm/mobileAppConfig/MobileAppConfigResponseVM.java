package com.dazzle.asklepios.web.rest.vm.mobileAppConfig;

import com.dazzle.asklepios.domain.MobileAppConfig;
import com.dazzle.asklepios.domain.enumeration.MobileAppConfigKey;

import java.time.Instant;

public record MobileAppConfigResponseVM(
        Long id,
        MobileAppConfigKey configKey,
        String configValue,
        String createdBy,
        Instant createdDate,
        String lastModifiedBy,
        Instant lastModifiedDate
) {
    public static MobileAppConfigResponseVM ofEntity(MobileAppConfig entity) {
        if (entity == null) {
            return null;
        }
        return new MobileAppConfigResponseVM(
                entity.getId(),
                entity.getConfigKey(),
                entity.getConfigValue(),
                entity.getCreatedBy(),
                entity.getCreatedDate(),
                entity.getLastModifiedBy(),
                entity.getLastModifiedDate()
        );
    }
}
