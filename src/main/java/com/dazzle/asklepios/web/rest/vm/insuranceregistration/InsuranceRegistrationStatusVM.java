package com.dazzle.asklepios.web.rest.vm.insuranceregistration;

/**
 * Whether a patient insurance or a visit may be registered on an insurance company, policy and class.
 * When it may not, {@code message} names what is inactive and why.
 */
public record InsuranceRegistrationStatusVM(
        boolean allowed,
        String message
) {
    public static InsuranceRegistrationStatusVM allowedStatus() {
        return new InsuranceRegistrationStatusVM(true, null);
    }

    public static InsuranceRegistrationStatusVM blocked(String subject, String reason) {
        String why = reason == null || reason.isBlank() ? "No reason was recorded" : reason.trim();
        return new InsuranceRegistrationStatusVM(
                false,
                subject + " is inactive, so patients and visits cannot be registered under it. Reason: " + why + "."
        );
    }
}
