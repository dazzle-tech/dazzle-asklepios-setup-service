package com.dazzle.asklepios.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

final class CoverageDiagnosisSelection {

    private CoverageDiagnosisSelection() {}

    static List<Long> ids(Collection<Long> stored, Long fallback) {
        LinkedHashSet<Long> ids = new LinkedHashSet<>();
        addAll(ids, stored);
        if (fallback != null) {
            ids.add(fallback);
        }
        return List.copyOf(ids);
    }

    static List<Long> unique(Collection<Long> ids) {
        LinkedHashSet<Long> unique = new LinkedHashSet<>();
        addAll(unique, ids);
        return List.copyOf(unique);
    }

    static List<Long> ensureMutable(List<Long> stored) {
        return stored == null ? new ArrayList<>() : stored;
    }

    static void replace(List<Long> target, Collection<Long> ids) {
        target.clear();
        target.addAll(unique(ids));
    }

    static Long first(Collection<Long> ids) {
        if (ids == null) {
            return null;
        }
        for (Long id : ids) {
            if (id != null) {
                return id;
            }
        }
        return null;
    }

    static boolean isEmpty(Collection<Long> stored, Long fallback) {
        return ids(stored, fallback).isEmpty();
    }

    private static void addAll(Set<Long> target, Collection<Long> source) {
        if (source == null) {
            return;
        }
        for (Long id : source) {
            if (id != null) {
                target.add(id);
            }
        }
    }
}
