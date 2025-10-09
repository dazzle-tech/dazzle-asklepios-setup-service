package com.dazzle.asklepios.web.rest.vm;

import com.dazzle.asklepios.domain.DuplicationCandidate;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public record DuplicationCandidateUpdateVM(

        Boolean dob,
        Boolean lastName,
        Boolean documentNo,
        Boolean mobileNumber,
        Boolean gender,
        Boolean isActive
,
        String role
) implements Serializable {}

