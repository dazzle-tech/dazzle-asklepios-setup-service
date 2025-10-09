package com.dazzle.asklepios.web.rest.vm;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

// Create VM
public record DuplicationCandidateCreateVM(
        Boolean dob,
        Boolean lastName,
        Boolean documentNo,
        Boolean mobileNumber,
        Boolean gender,
        Boolean isActive
        ,
        String role
) implements Serializable {}
