package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.*;
import com.dazzle.asklepios.domain.enumeration.Operation;
import com.dazzle.asklepios.domain.enumeration.Screen;
import com.dazzle.asklepios.repository.RoleAuthorityRepository;
import com.dazzle.asklepios.repository.RoleRepository;
import com.dazzle.asklepios.repository.RoleScreenRepository;
import com.dazzle.asklepios.repository.ScreenAuthorityRepository;
import com.dazzle.asklepios.service.dto.RoleScreenRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RolePermissionService {

    private final RoleScreenRepository roleScreenRepository;
    private final RoleRepository roleRepository;
    private final RoleAuthorityRepository roleAuthorityRepository;
    private final ScreenAuthorityRepository screenAuthorityRepository;

    @Transactional
    public void updateRolePermissions(Long roleId, List<RoleScreenRequest> requests) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleId));

        // 🧹 1. Delete all existing role screens and authorities for this role
        roleScreenRepository.deleteByIdRoleId(roleId);
        roleAuthorityRepository.deleteByRoleId(roleId);

        // 📋 2. Prepare lists for batch insert
        List<RoleScreen> roleScreensToSave = new ArrayList<>();
        List<RoleAuthority> roleAuthoritiesToSave = new ArrayList<>();

        // ➕ 3. Build new role screen and authority entries
        for (RoleScreenRequest req : requests) {
            Screen screenEnum = req.getScreen();
            Operation operationEnum = req.getPermission();

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

    public List<RoleScreenRequest> getRoleScreens(Long roleId) {
        // 📥 Fetch all role_screen mappings for the given role
        List<RoleScreen> screenRoles = roleScreenRepository.findByIdRoleId(roleId);

        // 🔄 Convert entities to DTOs
        return screenRoles.stream()
                .map(sr -> new RoleScreenRequest(
                        sr.getId().getScreen(),
                        sr.getId().getOperation()
                ))
                .toList();
    }
}
