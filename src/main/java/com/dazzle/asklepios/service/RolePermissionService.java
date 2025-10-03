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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

        // 🗑️ 1. Remove all existing role screens and authorities
        roleScreenRepository.deleteByRoleId(roleId);
        roleAuthorityRepository.deleteByRoleId(roleId);

        // 🗂️ 2. Prepare lists for batch insert
        List<RoleScreen> roleScreensToSave = new ArrayList<>();
        List<RoleAuthority> roleAuthoritiesToSave = new ArrayList<>();

        // ➕ 3. Build new role screens and authorities
        for (RoleScreenRequest req : requests) {
            // Add RoleScreen
            RoleScreen rs = RoleScreen.builder()
                    .role(role)
                    .roleId(roleId)
                    .screen(req.getScreen())
                    .operation(req.getPermission())
                    .build();
            roleScreensToSave.add(rs);

            // Add RoleAuthorities
            if (req.getPermission() == Operation.ALL) {
                // If ALL, fetch all operations from screen_authority
                List<ScreenAuthority> allOps = screenAuthorityRepository.findByScreen(req.getScreen());
                for (ScreenAuthority sa : allOps) {
                    RoleAuthorityId raId = new RoleAuthorityId(roleId, sa.getAuthorityName());
                    RoleAuthority ra = RoleAuthority.builder()
                            .id(raId)
                            .role(role)
                            .build();
                    roleAuthoritiesToSave.add(ra);
                }
            } else {
                // Otherwise, fetch specific screen + operation authorities
                List<ScreenAuthority> screenAuths =
                        screenAuthorityRepository.findByScreenAndOperation(req.getScreen(), req.getPermission());
                for (ScreenAuthority sa : screenAuths) {
                    RoleAuthorityId raId = new RoleAuthorityId(roleId, sa.getAuthorityName());
                    RoleAuthority ra = RoleAuthority.builder()
                            .id(raId)
                            .role(role)
                            .build();
                    roleAuthoritiesToSave.add(ra);
                }
            }
        }

        // 💾 4. Save all in batch
        roleScreenRepository.saveAll(roleScreensToSave);
        roleAuthorityRepository.saveAll(roleAuthoritiesToSave);
    }


    private void addAuthorities(Long roleId, Role role, Screen screen, Operation op) {
        List<ScreenAuthority> screenAuths =
                screenAuthorityRepository.findByScreenAndOperation(screen, op);

        for (ScreenAuthority sa : screenAuths) {
            RoleAuthorityId raId = new RoleAuthorityId(roleId, sa.getAuthorityName());
            RoleAuthority ra = RoleAuthority.builder()
                    .id(raId)
                    .role(role)
                    .build();
            roleAuthorityRepository.save(ra);
        }
    }


    public List<RoleScreenRequest> getRoleScreens(Long roleId) {
        // Fetch role_screen mappings for the given role
        List<RoleScreen> screenRoles = roleScreenRepository.findByRoleId(roleId);

        // Convert to DTO
        return screenRoles.stream()
                .map(sr -> new RoleScreenRequest(
                        sr.getScreen(),      // Enum مباشرة
                        sr.getOperation()    // Enum مباشرة
                ))

                .toList();
    }
}
