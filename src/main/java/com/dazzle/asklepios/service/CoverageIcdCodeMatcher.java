package com.dazzle.asklepios.service;

import java.util.Locale;

final class CoverageIcdCodeMatcher {

    private CoverageIcdCodeMatcher() {
    }

    static String normalize(String code) {
        if (code == null || code.isBlank()) {
            return "";
        }
        StringBuilder normalized = new StringBuilder(code.length());
        for (int i = 0; i < code.length(); i++) {
            char ch = code.charAt(i);
            if (ch == '.' || Character.isWhitespace(ch)) {
                continue;
            }
            normalized.append(ch);
        }
        return normalized.toString().toUpperCase(Locale.ROOT);
    }

    static boolean covers(String ruleCode, String encounterCode) {
        String rule = normalize(ruleCode);
        String encounter = normalize(encounterCode);
        if (rule.isEmpty() || encounter.isEmpty()) {
            return false;
        }
        if (encounter.equals(rule)) {
            return true;
        }
        return encounter.startsWith(rule) && Character.isDigit(encounter.charAt(rule.length()));
    }

    static boolean covers(String ruleCode, String encounterCode, String encounterCategoryCode) {
        if (covers(ruleCode, encounterCode)) {
            return true;
        }
        String rule = normalize(ruleCode);
        String category = normalize(encounterCategoryCode);
        return !rule.isEmpty() && rule.equals(category);
    }
}
