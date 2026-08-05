package com.dazzle.asklepios.domain.enumeration.biling;

/**
 * Where a billed amount appears in the accounting flow after its trigger fires.
 */
public enum BillingSettlementPath {

    REMAINING_TO_PAY,

    LEDGER_DEBIT_AT_CHECKOUT,

    MANUAL
}
