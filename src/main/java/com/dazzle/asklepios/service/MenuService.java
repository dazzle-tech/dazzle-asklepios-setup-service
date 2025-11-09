package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.enumeration.Operation;
import com.dazzle.asklepios.domain.enumeration.Screen;
import com.dazzle.asklepios.repository.MenuRepository;
import com.dazzle.asklepios.web.rest.vm.MenuItemVM;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MenuService {

    private static final Logger LOG = LoggerFactory.getLogger(MenuService.class);

    private final MenuRepository menuRepository;

    public List<MenuItemVM> getMenu(Long userId, Long facilityId) {
        List<MenuRepository.MenuRow> rows = menuRepository.findScreensForUserAndFacility(userId, facilityId);

        // screen -> union(xoperations)
        Map<Screen, EnumSet<Operation>> byScreen = new EnumMap<>(Screen.class);

        for (MenuRepository.MenuRow row : rows) {
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

        LOG.info("Total screens: {}", byScreen);

        if (byScreen.isEmpty()) {
            // No screen in this facility for this user → frontend will show only Dashboard
            LOG.info("This User without any authority : {}", userId);
            return List.of();
        }

        return byScreen.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().name()))
                .map(e -> MenuItemVM.of(e.getKey(), e.getValue()))
                .toList();
    }


    private Screen tryParseScreen(String raw) {
    if (raw == null) return null;
    try {
        return Screen.valueOf(raw.trim());
    } catch (Exception e) {
        LOG.warn("Skipping unknown screen value from DB (no normalization applied): {}", raw);
        return null;
    }
}

    private Operation tryParseOperation(String raw) {
        if (raw == null) return null;
        try { return Operation.valueOf(raw.trim().toUpperCase()); } catch (Exception e) { return null; }
    }
}
