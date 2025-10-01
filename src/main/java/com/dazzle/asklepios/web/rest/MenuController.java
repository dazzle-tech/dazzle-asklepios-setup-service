// src/main/java/com/dazzle/asklepios/web/rest/MenuController.java
package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.service.MenuService;
import com.dazzle.asklepios.web.rest.vm.MenuItemVM;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/setup/menu")
public class MenuController {

    private static final Logger LOG = LoggerFactory.getLogger(MenuController.class);
    private final MenuService menuService;

    /**
     * Example: GET /api/menu?userId=10&facilityId=3
     */
    @GetMapping
    public ResponseEntity<List<MenuItemVM>> getMenu(@RequestParam Long userId,
                                                    @RequestParam Long facilityId) {
        LOG.debug("Menu request userId={}, facilityId={}", userId, facilityId);
        return ResponseEntity.ok(menuService.getMenu(userId, facilityId));
    }
}
