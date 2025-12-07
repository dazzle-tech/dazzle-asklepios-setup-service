package com.dazzle.asklepios.web.rest.vm.referral;

import com.dazzle.asklepios.domain.enumeration.ReferralPriority;
import com.dazzle.asklepios.domain.enumeration.ReferralType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

public record ReferralRequestSaveVM(

        @NotNull Long patientId,
        Long encounterId,

        @NotNull ReferralType referralType, // default handled in service if null
        Long facilityId,                    // mandatory only if EXTERNAL

        @NotNull Long departmentId,

        @NotBlank @Size(max = 1000)
        String referralReason,

        @NotNull ReferralPriority priority,

        Boolean isActive

) implements Serializable {}
