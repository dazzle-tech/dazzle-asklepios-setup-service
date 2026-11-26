package com.dazzle.asklepios.web.rest.vm.coverage;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record CoverageClassPolicyLinkVM(
        @NotNull
        Long insuranceCompanyPolicyId
) implements Serializable {}
