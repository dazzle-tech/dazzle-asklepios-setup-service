package com.dazzle.asklepios.domain.enumeration;

/**
 * Chart inventory type used by Dental Chart Setup age rules.
 * ADULT → permanent Tooth1–Tooth32; CHILD → primary ToothA–ToothT;
 * MIXED → both primary and permanent inventories (chart seeding wired later).
 */
public enum DentalChartType {
    ADULT,
    CHILD,
    MIXED
}
