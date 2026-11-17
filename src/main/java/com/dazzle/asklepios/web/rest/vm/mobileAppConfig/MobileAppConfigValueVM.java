package com.dazzle.asklepios.web.rest.vm.mobileAppConfig;

import com.dazzle.asklepios.domain.MobileAppConfig;
import com.dazzle.asklepios.domain.enumeration.MobileAppConfigKey;
import com.dazzle.asklepios.domain.enumeration.MobileAppConfigType;

import java.util.List;

/** Value of one mobile app configuration key. Used by the mobile application. */
public record MobileAppConfigValueVM(
        MobileAppConfigKey configKey,
        MobileAppConfigType configType,
        List<String> configValues,
        String description
) {
    public static MobileAppConfigValueVM ofEntity(
            MobileAppConfig entity,
            List<String> resolvedValues
    ) {
        return new MobileAppConfigValueVM(
                entity.getConfigKey(),
                entity.getConfigType(),
                resolvedValues,
                entity.getDescription()
        );
    }
}
