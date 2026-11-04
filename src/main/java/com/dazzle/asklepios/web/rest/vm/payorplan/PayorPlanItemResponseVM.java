package com.dazzle.asklepios.web.rest.vm.payorplan;

import com.dazzle.asklepios.domain.PayorPlanItem;
import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;
import com.dazzle.asklepios.domain.enumeration.biling.InsuranceCoverageType;

import java.math.BigDecimal;
import java.time.Instant;

public record PayorPlanItemResponseVM(

        Long id,

        Long planId,

        BillingItemTypes itemType,

        BigDecimal amount,

        InsuranceCoverageType coverageType,

        Boolean isActive,

        Boolean preAuthorization,

        Long brandMedicationId,

        Long diagnosticTestId,

        Long serviceId,

        Long procedureId

) {

    public static PayorPlanItemResponseVM ofEntity(PayorPlanItem i) {

        return new PayorPlanItemResponseVM(

                i.getId(),

                i.getPlan().getId(),

                i.getItemType(),

                i.getAmount(),

                i.getCoverageType(),

                i.getIsActive(),

                i.getPreAuthorization(),

                i.getBrandMedication() != null ? i.getBrandMedication().getId() : null,

                i.getDiagnosticTest() != null ? i.getDiagnosticTest().getId() : null,

                i.getService() != null ? i.getService().getId() : null,

                i.getProcedure() != null ? i.getProcedure().getId() : null
        );
    }
}
