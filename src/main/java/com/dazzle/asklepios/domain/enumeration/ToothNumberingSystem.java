package com.dazzle.asklepios.domain.enumeration;

/**
 * Display numbering system for dental chart setup tooth labels.
 * Persisted selection uses stable internal identifiers (Tooth1–Tooth32 / ToothA–ToothT);
 * this enum only controls how those identifiers are labeled in Setup UI.
 */
public enum ToothNumberingSystem {
    UNIVERSAL,
    FDI,
    PALMER
}
