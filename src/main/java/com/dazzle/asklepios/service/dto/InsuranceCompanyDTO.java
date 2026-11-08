package com.dazzle.asklepios.service.dto;

import com.dazzle.asklepios.domain.enumeration.InsuranceCompanySource;

public record InsuranceCompanyDTO(

        Long id,

        InsuranceCompanySource source,

        Long payerId,

        Long nphiesPayerId,

        String code,

        String name,

        String nameAr,

        String nphiesId,

        Boolean isActive

) {
}
