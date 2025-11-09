package com.dazzle.asklepios.web.rest.vm;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Map;

/**
 * ViewModel for creating a DuplicationCandidate.
 * Fields are stored as JSON (key = field name, value = boolean).
 */
public record DuplicationCandidateCreateVM(

        @JsonProperty("id") Long id,
        @JsonProperty("fields") Map<String, Boolean> fields,
        @JsonProperty("isActive") Boolean isActive
) implements Serializable { }

