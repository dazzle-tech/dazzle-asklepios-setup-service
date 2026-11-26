package com.dazzle.asklepios.web.rest.vm.payor;

import com.dazzle.asklepios.domain.Payor;
import com.dazzle.asklepios.domain.enumeration.biling.PayorCategory;

import java.time.Instant;

public record PayorResponseVM(
        Long id,
        String code,
        String name,
        PayorCategory category,

        String address,
        String phone,
        String email,
        String contractManagerContact,

        String nphiesId,
        String waseelPayerId,
        String tpaNphiesId,
        Boolean isWaseelEnabled,

        Boolean isActive,
        Instant createdDate,
        Instant lastModifiedDate
) {
    public static PayorResponseVM ofEntity(Payor p) {
        return new PayorResponseVM(
                p.getId(),
                p.getCode(),
                p.getName(),
                p.getCategory(),

                p.getAddress(),
                p.getPhone(),
                p.getEmail(),
                p.getContractManagerContact(),

                p.getNphiesId(),
                p.getWaseelPayerId(),
                p.getTpaNphiesId(),
                p.getIsWaseelEnabled(),

                p.getIsActive(),
                p.getCreatedDate(),
                p.getLastModifiedDate()
        );
    }
}