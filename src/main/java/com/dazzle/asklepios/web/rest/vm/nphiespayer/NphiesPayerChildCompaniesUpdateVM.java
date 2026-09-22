package com.dazzle.asklepios.web.rest.vm.nphiespayer;

import com.dazzle.asklepios.web.rest.vm.jackson.FlexibleLongDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.Serializable;
import java.util.List;

public record NphiesPayerChildCompaniesUpdateVM(
        @JsonDeserialize(contentUsing = FlexibleLongDeserializer.class)
        List<Long> childCompanyIds
) implements Serializable {}
