package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Role;
import com.dazzle.asklepios.domain.RoleAuthority;
import com.dazzle.asklepios.domain.RoleAuthorityId;
import com.dazzle.asklepios.domain.RoleScreen;
import com.dazzle.asklepios.domain.RoleScreenId;
import com.dazzle.asklepios.domain.ScreenAuthority;
import com.dazzle.asklepios.domain.enumeration.Operation;
import com.dazzle.asklepios.domain.enumeration.Screen;
import com.dazzle.asklepios.repository.RoleAuthorityRepository;
import com.dazzle.asklepios.repository.RoleRepository;
import com.dazzle.asklepios.repository.RoleScreenRepository;
import com.dazzle.asklepios.repository.ScreenAuthorityRepository;
import com.dazzle.asklepios.web.rest.RoleController;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import com.dazzle.asklepios.web.rest.vm.RoleScreenVM;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Service
@RequiredArgsConstructor
public class RolePermissionService {
    private static final Logger LOG = LoggerFactory.getLogger(RoleController.class);

    private static final int MAX_FETCH_LIMIT = 2000;

    private final RoleScreenRepository roleScreenRepository;
    private final RoleRepository roleRepository;
    private final RoleAuthorityRepository roleAuthorityRepository;
    private final ScreenAuthorityRepository screenAuthorityRepository;


    @Transactional
    public void updateRolePermissions(Long roleId, List<RoleScreenVM> requests) {

        // ✅ 1. Verify that the role exists, otherwise throw 404
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new NotFoundAlertException(
                        "Role not found",
                        "role",
                        "roleNotFound"
                ));

        // 🧹 2. Remove all existing role screens and authorities
        roleScreenRepository.deleteByIdRoleId(roleId);
        roleAuthorityRepository.deleteByRoleId(roleId);

        // 📋 3. Prepare new lists for batch saving
        List<RoleScreen> roleScreensToSave = new ArrayList<>();
        List<RoleAuthority> roleAuthoritiesToSave = new ArrayList<>();

        // ⚡ 4. Load all ScreenAuthority records once (to avoid N+1 queries)
        List<ScreenAuthority> allAuthorities = screenAuthorityRepository.findAll();

        LOG.info("Starting permission update for roleId={} with {} requests", roleId, requests.size());

        // 🔁 5. Iterate over all incoming requests from the frontend
        for (RoleScreenVM req : requests) {
            Screen screen = req.screen();
            Operation operation = req.permission();

            // 🧩 Create RoleScreen entity (link between role, screen, and operation)
            RoleScreen roleScreen = RoleScreen.builder()
                    .id(new RoleScreenId(roleId, screen, operation))
                    .role(role)
                    .build();
            roleScreensToSave.add(roleScreen);

            // 🔍 6. Find matching authorities for this screen and operation
            List<RoleAuthority> matchedAuthorities = new ArrayList<>();

            for (ScreenAuthority sa : allAuthorities) {
                if (sa.getScreen() == screen && sa.getOperation() == operation) {
                    RoleAuthority roleAuthority = RoleAuthority.builder()
                            .id(new RoleAuthorityId(roleId, sa.getAuthorityName()))
                            .role(role)
                            .build();
                    matchedAuthorities.add(roleAuthority);
                }
            }

            // ➕ 7. Collect all matched authorities for batch saving
            roleAuthoritiesToSave.addAll(matchedAuthorities);

            // 🧾 8. Log details for debugging and traceability
            LOG.debug("Added {} authorities for screen={} operation={}",
                    matchedAuthorities.size(), screen.name(), operation.name());
        }

        // 💾 9. Save all records in batch to minimize DB operations
        roleScreenRepository.saveAll(roleScreensToSave);
        roleAuthorityRepository.saveAll(roleAuthoritiesToSave);

        // ✅ 10. Final summary log
        LOG.info("Role permissions updated successfully for roleId={} ({} screens, {} authorities)",
                roleId, roleScreensToSave.size(), roleAuthoritiesToSave.size());
    }



    @Transactional
    public List<RoleScreenVM> getRoleScreens(Long roleId) {
        LOG.debug("Fetching role screens for roleId={}", roleId);

        if (!roleRepository.existsById(roleId)) {
            LOG.debug("Role not found for roleId={}", roleId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found");
        }

        List<RoleScreen> screenRoles = roleScreenRepository.findByIdRoleId(roleId);
        LOG.debug("Found {} screenRoles for roleId={}", screenRoles.size(), roleId);

        if (screenRoles.size() > MAX_FETCH_LIMIT) {
            LOG.debug("Too many records to fetch for roleId={} (limit={})", roleId, MAX_FETCH_LIMIT);
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Too many records to fetch");
        }

        return screenRoles.stream()
                .filter(Objects::nonNull)
                .filter(sr -> sr.getId() != null && sr.getId().getScreen() != null && sr.getId().getOperation() != null)
                .map(sr -> new RoleScreenVM(
                        sr.getId().getScreen(),
                        sr.getId().getOperation()
                ))
                .toList();
    }

}
