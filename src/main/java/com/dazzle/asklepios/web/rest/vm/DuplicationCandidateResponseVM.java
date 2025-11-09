package com.dazzle.asklepios.web.rest.vm;

import com.dazzle.asklepios.domain.DuplicationCandidate;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

/**
 * Response ViewModel for DuplicationCandidate entity.
 * Reflects the new structure where field values are stored as JSON.
 */
public record DuplicationCandidateResponseVM(
        Long id,
        String rule,
        @JsonProperty("fields") Map<String, Boolean> fields,
        String createdBy,
        Instant createdDate,
        String lastModifiedBy,
        Instant lastModifiedDate,
        Boolean isActive
) implements Serializable {

    public static DuplicationCandidateResponseVM ofEntity(DuplicationCandidate entity) {
        return new DuplicationCandidateResponseVM(
                entity.getId(),
                entity.getRule(),
                entity.getFields(), // assumes entity has a method getFieldsMap() returning Map<String, Boolean>
                entity.getCreatedBy(),
                entity.getCreatedDate(),
                entity.getLastModifiedBy(),
                entity.getLastModifiedDate(),
                entity.getIsActive()
        );
    }
}
