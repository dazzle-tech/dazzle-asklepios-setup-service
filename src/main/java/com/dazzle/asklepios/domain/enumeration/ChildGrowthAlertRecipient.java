package com.dazzle.asklepios.domain.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Who is told when a growth result falls in a status band. A band can alert more than one recipient.
 * An empty list means nobody is alerted.
 */
public enum ChildGrowthAlertRecipient {
    PHYSICIAN,
    NURSE,
    TASK;

    @JsonCreator
    public static ChildGrowthAlertRecipient fromValue(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        for (ChildGrowthAlertRecipient recipient : values()) {
            if (recipient.name().equalsIgnoreCase(raw.trim())) {
                return recipient;
            }
        }
        throw new IllegalArgumentException("Unknown alert recipient: " + raw);
    }

    /**
     * Earlier drafts stored a sentence instead of the recipient list.
     */
    public static List<ChildGrowthAlertRecipient> fromLegacyLabel(String raw) {
        if (raw == null || raw.isBlank() || raw.equalsIgnoreCase("No alert")) {
            return List.of();
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        if (normalized.equals("physician + task")) {
            return List.of(PHYSICIAN, TASK);
        }
        if (normalized.equals("nurse flag") || normalized.equals("nurse")) {
            return List.of(NURSE);
        }
        ChildGrowthAlertRecipient recipient = fromValue(raw);
        return recipient == null ? List.of() : List.of(recipient);
    }

    public static List<ChildGrowthAlertRecipient> copyOf(List<ChildGrowthAlertRecipient> recipients) {
        return recipients == null ? new ArrayList<>() : new ArrayList<>(recipients);
    }
}
