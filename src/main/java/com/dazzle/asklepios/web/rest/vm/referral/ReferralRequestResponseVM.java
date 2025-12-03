package com.dazzle.asklepios.web.rest.vm.referral;

import com.dazzle.asklepios.domain.ReferralRequest;
import com.dazzle.asklepios.domain.enumeration.ReferralPriority;
import com.dazzle.asklepios.domain.enumeration.ReferralType;

import java.time.Instant;

public record ReferralRequestResponseVM(
        Long id,
        Long patientId,
        Long encounterId,

        ReferralType referralType,
        Long facilityId,
        Long departmentId,

        String referralReason,
        ReferralPriority priority,

        Boolean isActive,

        Instant createdDate,
        Instant lastModifiedDate
) {
    public static ReferralRequestResponseVM ofEntity(ReferralRequest r) {
        return new ReferralRequestResponseVM(
                r.getId(),
                r.getPatientId(),
                r.getEncounterId(),
                r.getReferralType(),
                r.getFacilityId(),
                r.getDepartmentId(),
                r.getReferralReason(),
                r.getPriority(),
                r.getIsActive(),
                r.getCreatedDate(),
                r.getLastModifiedDate()
        );
    }
}
