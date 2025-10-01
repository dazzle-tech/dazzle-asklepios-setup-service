// src/main/java/com/dazzle/asklepios/service/MenuService.java
package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.enumeration.Operation;
import com.dazzle.asklepios.domain.enumeration.Screen;
import com.dazzle.asklepios.repository.MenuRepository;
import com.dazzle.asklepios.web.rest.vm.MenuItemVM;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class MenuService {

    private static final Logger LOG = LoggerFactory.getLogger(MenuService.class);

    private final MenuRepository menuRepository;

    public List<MenuItemVM> getMenu(Long userId, Long facilityId) {
        var rows = menuRepository.findScreensForUserAndFacility(userId, facilityId);

        // screen -> union(operations)
        Map<Screen, EnumSet<Operation>> byScreen = new EnumMap<>(Screen.class);

        for (var row : rows) {
            Screen screen = tryParseScreen(row.getScreen());
            if (screen == null) {
                LOG.warn("Skipping unknown screen value from DB: {}", row.getScreen());
                continue;
            }
            Operation op = tryParseOperation(row.getOperation());
            if (op == null) {
                LOG.warn("Skipping unknown operation value from DB: {}", row.getOperation());
                continue;
            }
            byScreen.computeIfAbsent(screen, k -> EnumSet.noneOf(Operation.class)).add(op);
        }

        if (byScreen.isEmpty()) {
            // No grants in this facility for this user → frontend will show only Dashboard
            return List.of();
        }

        return byScreen.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().name()))
                .map(e -> MenuItemVM.of(e.getKey(), e.getKey().toValue(), e.getValue()))
                .toList();
    }

    private Screen tryParseScreen(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        // 1) Try exact enum (USERS, ICD_10, etc.)
        try { return Screen.valueOf(s.toUpperCase()); } catch (Exception ignore) {}
        // 2) Replace hyphens/slashes with spaces and use your @JsonCreator(fromValue)
        String cleaned = s.replace('-', ' ').replace('/', ' ').replaceAll("\\s+", " ").trim();
        try { return Screen.fromValue(cleaned); } catch (Exception ignore) {}
        // 3) Strip non-alphanumerics → spaces, then fromValue again
        String alpha = s.replaceAll("[^A-Za-z0-9]+", " ").trim();
        try { return Screen.fromValue(alpha); } catch (Exception ignore) {}
        return null;
    }

    private Operation tryParseOperation(String raw) {
        if (raw == null) return null;
        try { return Operation.valueOf(raw.trim().toUpperCase()); } catch (Exception e) { return null; }
    }
}
