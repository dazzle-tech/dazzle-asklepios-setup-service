package com.dazzle.asklepios.web.rest.vm;

// Create VM
public record DuplicationCandidateCreateVM(
        Boolean dob,
        Boolean lastName,
        Boolean documentNo,
        Boolean mobileNumber,

        Boolean gender
) {}

