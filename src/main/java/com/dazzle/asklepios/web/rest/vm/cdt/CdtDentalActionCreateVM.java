package com.dazzle.asklepios.web.rest.vm.cdt;


import com.dazzle.asklepios.domain.CdtCode;
import com.dazzle.asklepios.domain.CdtDentalAction;
import com.dazzle.asklepios.domain.DentalAction;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CdtDentalActionCreateVM(
        @NotNull Long dentalActionId,
        @NotNull String cdtCode
) {
    public CdtDentalAction toEntity() {
        return CdtDentalAction.builder()
                .dentalAction(DentalAction.builder().id(dentalActionId).build())
                .cdtCode(CdtCode.builder().code(cdtCode).build())
                .build();
    }
}

