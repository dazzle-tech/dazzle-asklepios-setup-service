package com.dazzle.asklepios.web.rest.vm.childgrowth;

import com.dazzle.asklepios.domain.childgrowth.ChildGrowthDocument;
import com.dazzle.asklepios.domain.enumeration.ConfigurationScope;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

public record ChildGrowthSetupSaveVM(
        @NotNull ConfigurationScope scope,
        Long facilityId,
        List<Long> userIds,
        @NotNull List<ChildGrowthDocument.Chart> charts,
        @NotNull ChildGrowthDocument.Rules rules,
        @NotNull Map<String, List<ChildGrowthDocument.Band>> bandsByIndicator,
        @NotNull ChildGrowthDocument.Limits limits,
        @NotNull ChildGrowthDocument.Alerts alerts
) {
}
