package com.dazzle.asklepios.web.rest.vm.coverage;

public record CoverageContractResolveResponse(
        boolean matched,
        String matchReason,
        CoverageContractResponseVM contract,
        CoverageCopaymentVM copayment,
        boolean coverageConfigured,
        boolean uncovered,
        CoverageReadingVM coverage,
        CoverageReadingVM limit,
        CoverageReadingVM cashLimit,
        CoverageDiscountVM discount,
        CoverageExclusionVM exclusion,
        CoveragePreApprovalReadingVM preApproval
) {
    public static CoverageContractResolveResponse unmatched(String matchReason) {
        return new CoverageContractResolveResponse(
                false,
                matchReason,
                null,
                null,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}
