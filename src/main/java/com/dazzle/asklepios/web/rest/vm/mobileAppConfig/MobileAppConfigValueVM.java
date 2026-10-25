package com.dazzle.asklepios.web.rest.vm.mobileAppConfig;

import com.dazzle.asklepios.domain.MobileAppConfig;
import com.dazzle.asklepios.domain.enumeration.MobileAppConfigKey;
import com.dazzle.asklepios.domain.enumeration.MobileAppConfigType;

/** Value of one mobile app configuration key. Used by the mobile application. */
public record MobileAppConfigValueVM(
        MobileAppConfigKey configKey,
        MobileAppConfigType configType,
        String configValue
) {
    public static MobileAppConfigValueVM ofEntity(MobileAppConfig entity, String resolvedValue) {
        return new MobileAppConfigValueVM(entity.getConfigKey(), entity.getConfigType(), resolvedValue);
    }
}
