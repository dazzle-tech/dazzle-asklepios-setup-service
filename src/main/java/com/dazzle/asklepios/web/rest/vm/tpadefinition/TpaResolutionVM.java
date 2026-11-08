package com.dazzle.asklepios.web.rest.vm.tpadefinition;

import java.io.Serializable;
import java.util.List;

public record TpaResolutionVM(
        Status status,
        String tpaNphiesId,
        String tpaName,
        List<Candidate> candidates
) implements Serializable {

    public enum Status {
        /** The payer is not managed by a TPA, so requests go to the payer directly. */
        NO_TPA,
        RESOLVED,
        /** The payer has several TPAs and nothing identifies the one for this policy. */
        SELECTION_REQUIRED,
        /** The TPA is known but its NPHIES ID has not been entered in setup. */
        NPHIES_ID_MISSING
    }

    public record Candidate(Long id, String name, String nphiesId) implements Serializable {
    }
}
