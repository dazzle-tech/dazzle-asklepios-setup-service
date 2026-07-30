package com.dazzle.asklepios.domain.enumeration.biling;

/**
 * Clinical / billing engine events that can create or update charge lines.
 */
public enum BillingEventType {

    ENCOUNTER_CREATED,

    TREATMENT_STARTED,

    ITEM_ORDERED,

    ITEM_DISPENSED,

    SERVICE_COMPLETED,

    ITEM_UPDATED,

    ITEM_CANCELLED,

    ENCOUNTER_CANCELLED,

    CHECKOUT,

    MANUAL
}
