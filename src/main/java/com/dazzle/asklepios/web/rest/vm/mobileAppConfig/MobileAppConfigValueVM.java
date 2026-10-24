package com.dazzle.asklepios.web.rest.vm.mobileAppConfig;

import com.dazzle.asklepios.domain.MobileAppConfig;
import com.dazzle.asklepios.domain.enumeration.MobileAppConfigKey;

/** Value of one mobile app configuration key. Used by the mobile application. */
public record MobileAppConfigValueVM(
        MobileAppConfigKey configKey,
        String configValue
) {
    public static MobileAppConfigValueVM ofEntity(MobileAppConfig entity) {
        return new MobileAppConfigValueVM(entity.getConfigKey(), entity.getConfigValue());
    }
}
