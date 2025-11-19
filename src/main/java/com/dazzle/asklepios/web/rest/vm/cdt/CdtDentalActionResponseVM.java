package com.dazzle.asklepios.web.rest.vm.cdt;


import com.dazzle.asklepios.domain.CdtCode;
import com.dazzle.asklepios.domain.CdtDentalAction;
import lombok.Builder;

@Builder
public record CdtDentalActionResponseVM(
        Long id,
        Long dentalActionId,
        CdtCode cdtId
) {
    public static CdtDentalActionResponseVM ofEntity(CdtDentalAction entity) {
        return CdtDentalActionResponseVM.builder()
                .id(entity.getId())
                .dentalActionId(entity.getDentalAction() != null ? entity.getDentalAction().getId() : null)
                .cdtId(entity.getCdtId())
                .build();
    }
}


