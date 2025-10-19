package com.dazzle.asklepios.web.rest.vm;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Map;

/**
 * ViewModel for updating a DuplicationCandidate.
 * Fields are stored as a JSON object.
 */
public record DuplicationCandidateUpdateVM(
        @JsonProperty("fields") Map<String, Boolean> fields,
        @JsonProperty("isActive") Boolean isActive
) implements Serializable { }
