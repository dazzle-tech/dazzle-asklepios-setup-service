package com.dazzle.asklepios.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InsuranceCompanyCreateVM(

        @Size(max = 50)
        String code,

        @NotBlank
        @Size(max = 255)
        String name,

        @Size(max = 100)
        String nphiesId

) {
}
