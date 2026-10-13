package com.dazzle.asklepios.web.rest.vm.cdtservicemapping;

import com.dazzle.asklepios.domain.CdtServiceMapping;

public record CdtServiceMappingResponseVM(
        Long id,
        Long cdtCodeId,
        Long serviceId
) {
    public static CdtServiceMappingResponseVM fromEntity(CdtServiceMapping e) {
        return new CdtServiceMappingResponseVM(
                e.getId(),
                e.getCdtCode().getId(),
                e.getService().getId()
        );
    }
}
