package com.dazzle.asklepios.web.rest.vm;

import com.dazzle.asklepios.domain.DuplicationCandidate;

import java.time.Instant;

public record DuplicationCandidateResponseVM(
        Long id,
        String role,
        Boolean dob,
        Boolean lastName,
        Boolean documentNo,
        Boolean mobileNumber,
        Boolean gender,
        String createdBy,
        Instant createdDate,
        String lastModifiedBy,
        Instant lastModifiedDate
) {
    public static DuplicationCandidateResponseVM ofEntity(DuplicationCandidate entity) {
        return new DuplicationCandidateResponseVM(
                entity.getId(),
                entity.getRole(),
                entity.getDob(),
                entity.getLastName(),
                entity.getDocumentNo(),
                entity.getMobileNumber(),
                entity.getGender(),
                entity.getCreatedBy(),
                entity.getCreatedDate(),
                entity.getLastModifiedBy(),
                entity.getLastModifiedDate()

                );
    }}