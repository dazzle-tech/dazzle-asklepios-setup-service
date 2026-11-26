package com.dazzle.asklepios.service;

import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;

/**
 * Shared rule for setup records whose deactivation blocks patient registration:
 * deactivating needs a reason (shown to users when registration is refused),
 * reactivating clears it.
 */
public final class InactiveReason {

    private InactiveReason() {}

    /**
     * @param deactivating true when the record is going from active to inactive
     * @param label        what is being deactivated, used in the error message
     * @return the reason to store: trimmed when deactivating, null when reactivating
     */
    public static String forToggle(boolean deactivating, String reason, String entityName, String label) {
        if (!deactivating) {
            return null;
        }
        if (reason == null || reason.isBlank()) {
            throw new BadRequestAlertException(
                    "A reason is required to deactivate the " + label + ".",
                    entityName,
                    "inactiveReasonRequired"
            );
        }
        return reason.trim();
    }
}
