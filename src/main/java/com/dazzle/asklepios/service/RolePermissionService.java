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
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleId));

        // 🧹 1. Delete all existing role screens and authorities for this role
        roleScreenRepository.deleteByIdRoleId(roleId);
        roleAuthorityRepository.deleteByRoleId(roleId);

        // 📋 2. Prepare lists for batch insert
        List<RoleScreen> roleScreensToSave = new ArrayList<>();
        List<RoleAuthority> roleAuthoritiesToSave = new ArrayList<>();

        // ➕ 3. Build new role screen and authority entries
        for (RoleScreenVM req : requests) {
            Screen screenEnum = req.screen();
            Operation operationEnum = req.permission();

            // 🧠 Composite primary key
            RoleScreenId rsId = new RoleScreenId(roleId, screenEnum, operationEnum);

            // 📝 Build entity without a separate auto-generated ID
            RoleScreen rs = RoleScreen.builder()
                    .id(rsId)
                    .role(role)
                    .build();
            roleScreensToSave.add(rs);

            // 🔐 Create role authorities for this screen + operation
            List<ScreenAuthority> screenAuths =
                    screenAuthorityRepository.findByScreenAndOperation(screenEnum, operationEnum);
            for (ScreenAuthority sa : screenAuths) {
                RoleAuthorityId raId = new RoleAuthorityId(roleId, sa.getAuthorityName());
                RoleAuthority ra = RoleAuthority.builder()
                        .id(raId)
                        .role(role)
                        .build();
                roleAuthoritiesToSave.add(ra);
            }
        }

        // 💾 4. Save all records in batch
        roleScreenRepository.saveAll(roleScreensToSave);
        roleAuthorityRepository.saveAll(roleAuthoritiesToSave);
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
