package com.dazzle.asklepios.web.rest.vm.referral;
import com.dazzle.asklepios.domain.enumeration.ReferralPriority;
import com.dazzle.asklepios.domain.enumeration.ReferralType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

public record ReferralRequestUpdateVM(

        @NotNull Long id,

        @NotNull Long patientId,
        Long encounterId,

        @NotNull ReferralType referralType,
        Long facilityId,

        @NotNull Long departmentId,

        @NotBlank @Size(max = 1000)
        String referralReason,

        @NotNull ReferralPriority priority,

        Boolean isActive

) implements Serializable {}
