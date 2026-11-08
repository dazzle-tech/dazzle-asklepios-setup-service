package com.dazzle.asklepios.web.rest.vm.nphiespayer;

import com.dazzle.asklepios.web.rest.vm.jackson.FlexibleLongDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.Serializable;
import java.util.List;

public record NphiesPayerTpasUpdateVM(
        @JsonDeserialize(contentUsing = FlexibleLongDeserializer.class)
        List<Long> tpaIds
) implements Serializable {}
