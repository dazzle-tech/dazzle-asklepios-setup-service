package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Role;
import com.dazzle.asklepios.domain.RoleScreen;
import com.dazzle.asklepios.domain.RoleAuthority;
import com.dazzle.asklepios.domain.RoleAuthorityId;
import com.dazzle.asklepios.domain.enumeration.Operation;
import com.dazzle.asklepios.domain.enumeration.Screen;
import com.dazzle.asklepios.repository.RoleAuthorityRepository;
import com.dazzle.asklepios.repository.RoleRepository;
import com.dazzle.asklepios.repository.RoleScreenRepository;
import com.dazzle.asklepios.service.dto.RoleScreenRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    @Transactional
    public void updateRolePermissions(Long roleId, List<RoleScreenRequest> requests) {
        // 1. Fetch the role from DB or throw if not found
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleId));

        // 2. Fetch existing screens linked to this role
        List<RoleScreen> existingScreens = roleScreenRepository.findByRoleId(roleId);

        // 3. Build a set of current keys (screen + operation)
        Set<String> existingKeys = existingScreens.stream()
                .map(rs -> rs.getScreen() + "_" + rs.getOperation().name())
                .collect(Collectors.toSet());

        // 4. Build a set of requested keys (from incoming requests)
        Set<String> newKeys = requests.stream()
                .map(req -> req.getScreen().name() + "_" + req.getPermission().name())
                .collect(Collectors.toSet());

        // 5. Determine items to delete (present in existing but not in new)
        Set<String> toDelete = new HashSet<>(existingKeys);
        toDelete.removeAll(newKeys);

        // 6. Determine items to add (present in new but not in existing)
        Set<String> toAdd = new HashSet<>(newKeys);
        toAdd.removeAll(existingKeys);

        // 7. Delete old screens/authorities
        existingScreens.stream()
                .filter(rs -> toDelete.contains(rs.getScreen() + "_" + rs.getOperation().name()))
                .forEach(rs -> {
                    // Remove from role_screen
                    roleScreenRepository.delete(rs);

                    // Remove from role_authority
                    String auth = rs.getScreen() + "_" + rs.getOperation().name();
                    roleAuthorityRepository.deleteById(new RoleAuthorityId(roleId, auth));
                });

        // 8. Add new screens/authorities
        requests.stream()
                .filter(req -> toAdd.contains(req.getScreen().name() + "_" + req.getPermission().name()))
                .forEach(req -> {
                    // Insert into role_screen
                    RoleScreen rs = RoleScreen.builder()
                            .role(role)
                            .roleId(roleId)
                            .screen(req.getScreen().name())
                            .operation(req.getPermission())
                            .build();
                    roleScreenRepository.save(rs);

                    // Insert into role_authority
                    String auth = req.getScreen().name() + "_" + req.getPermission().name();
                    RoleAuthorityId raId = new RoleAuthorityId(roleId, auth);
                    RoleAuthority ra = RoleAuthority.builder()
                            .id(raId)
                            .role(role)
                            .build();
                    roleAuthorityRepository.save(ra);
                });
    }

    public List<RoleScreenRequest> getRoleScreens(Long roleId) {
        // Fetch role_screen mappings for the given role
        List<RoleScreen> screenRoles = roleScreenRepository.findByRoleId(roleId);

        // Convert to DTO
        return screenRoles.stream()
                .map(sr -> new RoleScreenRequest(
                        Screen.fromValue(sr.getScreen()),
                        sr.getOperation()
                ))
                .toList();
    }
}
