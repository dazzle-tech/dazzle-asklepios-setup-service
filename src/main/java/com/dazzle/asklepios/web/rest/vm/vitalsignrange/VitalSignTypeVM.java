package com.dazzle.asklepios.web.rest.vm.vitalsignrange;

import com.dazzle.asklepios.domain.enumeration.VitalSignType;

public record VitalSignTypeVM(String vitalSignKey, String unit) {
    public static VitalSignTypeVM fromType(VitalSignType type) {
        return new VitalSignTypeVM(type.getKey(), type.getUnit());
    }
}
