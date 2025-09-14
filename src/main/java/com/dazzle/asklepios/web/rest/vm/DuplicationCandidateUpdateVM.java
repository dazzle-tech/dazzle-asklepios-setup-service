package com.dazzle.asklepios.web.rest.vm;

import com.dazzle.asklepios.domain.DuplicationCandidate;

import java.io.Serializable;

public record DuplicationCandidateUpdateVM(

        Boolean dob,
        Boolean lastName,
        Boolean documentNo,
        Boolean mobileNumber,
        Boolean gender
) implements Serializable {

    public static DuplicationCandidateUpdateVM ofEntity(DuplicationCandidate candidate) {
        return new DuplicationCandidateUpdateVM(
                candidate.getDob(),
                candidate.getLastName(),
                candidate.getDocumentNo(),
                candidate.getMobileNumber(),
                candidate.getGender()
        );
    }
}
