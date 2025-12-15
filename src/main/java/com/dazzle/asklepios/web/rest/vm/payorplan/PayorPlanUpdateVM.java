package com.dazzle.asklepios.web.rest.vm.payorplan;
import com.dazzle.asklepios.domain.enumeration.biling.PayorPlanType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

public record PayorPlanUpdateVM(
        @NotNull Long id,
        @NotNull Long payorId,

        @NotBlank @Size(max=255)
        String name,

        @NotNull
        PayorPlanType planType,

        Boolean isActive
) implements Serializable {}

