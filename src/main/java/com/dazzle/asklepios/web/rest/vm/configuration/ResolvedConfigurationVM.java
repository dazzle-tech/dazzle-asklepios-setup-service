package com.dazzle.asklepios.web.rest.vm.configuration;

import com.dazzle.asklepios.domain.Configuration;
import com.dazzle.asklepios.domain.enumeration.ConfigurationKeys;
import com.dazzle.asklepios.domain.enumeration.ConfigurationScope;
import com.dazzle.asklepios.domain.enumeration.ConfigurationValueType;

import java.io.Serializable;

/**
 * Winning stored configuration for a feature check.
 */
public record ResolvedConfigurationVM(
        ConfigurationKeys key,
        String value,
        ConfigurationValueType valueType,
        ConfigurationScope scope,
        Long configurationId
) implements Serializable {

    public static ResolvedConfigurationVM fromRow(Configuration configuration) {
        return new ResolvedConfigurationVM(
                configuration.getKey(),
                configuration.getValue(),
                configuration.getValueType(),
                configuration.getScopeType(),
                configuration.getId()
        );
    }
}
