// src/main/java/com/dazzle/asklepios/web/rest/MenuController.java
package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.User;
import com.dazzle.asklepios.repository.UserRepository;
import com.dazzle.asklepios.security.SecurityUtils;
import com.dazzle.asklepios.service.MenuService;
import com.dazzle.asklepios.web.rest.vm.MenuItemVM;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/setup/menu")
public class MenuController {

    private static final Logger LOG = LoggerFactory.getLogger(MenuController.class);
    private final MenuService menuService;
    private final UserRepository userRepository;

    /**
     * Example: GET /api/menu
     */
    @GetMapping
    public ResponseEntity<List<MenuItemVM>> getMenu() {
        String login = SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated."));

        Long userId = userRepository.findByLogin(login)
                .map(User::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found: " + login));

        Long facilityId = SecurityUtils.getCurrentUserFacility()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing mandatory claim 'tenant' in JWT."));

        return ResponseEntity.ok(menuService.getMenu(userId, facilityId));
    }


}
