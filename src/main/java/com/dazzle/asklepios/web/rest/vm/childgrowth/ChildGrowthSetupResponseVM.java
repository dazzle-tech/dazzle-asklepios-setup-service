package com.dazzle.asklepios.web.rest.vm.childgrowth;

import com.dazzle.asklepios.domain.ChildGrowthSetup;
import com.dazzle.asklepios.domain.childgrowth.ChildGrowthDocument;
import com.dazzle.asklepios.domain.enumeration.ChildGrowthIndicator;
import com.dazzle.asklepios.domain.enumeration.ConfigurationScope;

import java.util.List;
import java.util.Map;

public record ChildGrowthSetupResponseVM(
        Long id,
        ConfigurationScope scope,
        Long facilityId,
        List<Long> userIds,
        boolean saved,
        List<ChildGrowthDocument.Chart> charts,
        ChildGrowthDocument.Rules rules,
        Map<ChildGrowthIndicator, List<ChildGrowthDocument.Band>> bandsByIndicator,
        ChildGrowthDocument.Limits limits,
        ChildGrowthDocument.Alerts alerts
) {
    public static ChildGrowthSetupResponseVM fromEntity(ChildGrowthSetup entity) {
        return of(
                entity.getId(),
                entity.getScopeType(),
                entity.getFacility() == null ? null : entity.getFacility().getId(),
                entity.getUser() == null ? List.of() : List.of(entity.getUser().getId()),
                true,
                entity.getDocument()
        );
    }

    public static ChildGrowthSetupResponseVM saved(
            Long id,
            ConfigurationScope scope,
            Long facilityId,
            List<Long> userIds,
            ChildGrowthDocument document
    ) {
        return of(id, scope, facilityId, userIds == null ? List.of() : List.copyOf(userIds), true, document);
    }

    public static ChildGrowthSetupResponseVM unsaved(ConfigurationScope scope, Long facilityId, List<Long> userIds) {
        return of(null, scope, facilityId, userIds == null ? List.of() : List.copyOf(userIds), false, ChildGrowthDocument.example());
    }

    private static ChildGrowthSetupResponseVM of(
            Long id,
            ConfigurationScope scope,
            Long facilityId,
            List<Long> userIds,
            boolean saved,
            ChildGrowthDocument document
    ) {
        return new ChildGrowthSetupResponseVM(
                id,
                scope,
                facilityId,
                userIds,
                saved,
                document.getCharts(),
                document.getRules(),
                document.getBandsByIndicator(),
                document.getLimits(),
                document.getAlerts()
        );
    }
}
