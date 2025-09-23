package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Role;
import com.dazzle.asklepios.domain.RoleScreen;
import com.dazzle.asklepios.domain.ScreenAuthority;
import com.dazzle.asklepios.domain.RoleAuthority;
import com.dazzle.asklepios.domain.RoleAuthorityId;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RolePermissionService {

    private final RoleScreenRepository roleScreenRepository;
    private final RoleAuthorityRepository roleAuthorityRepository;
    private final ScreenAuthorityRepository screenAuthorityRepository;
    private final RoleRepository roleRepository;

    @Transactional
    public void updateRolePermissions(Long roleId, List<RoleScreenRequest> requests) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleId));

        // 1. جيب الموجود من DB
        List<RoleScreen> existingScreens = roleScreenRepository.findByRoleId(roleId);

        // 2. حوله إلى مفاتيح screen+operation
        Set<String> existingKeys = existingScreens.stream()
                .map(rs -> rs.getScreen() + "_" + rs.getOperation().name())
                .collect(Collectors.toSet());

        // 3. جهز الـ requests الجديدة كمفاتيح
        Set<String> newKeys = requests.stream()
                .map(req -> req.getScreen().name() + "_" + req.getPermission().name())
                .collect(Collectors.toSet());

        // 4. حدد الفرق
        Set<String> toDelete = new HashSet<>(existingKeys);
        toDelete.removeAll(newKeys);

        Set<String> toAdd = new HashSet<>(newKeys);
        toAdd.removeAll(existingKeys);

        // 5. احذف RoleScreens + RoleAuthorities المرتبطة
        existingScreens.stream()
                .filter(rs -> toDelete.contains(rs.getScreen() + "_" + rs.getOperation().name()))
                .forEach(rs -> {
                    // أولاً نحذف authorities المرتبطة
                    List<ScreenAuthority> screenAuths =
                            screenAuthorityRepository.findByScreenAndOperation(rs.getScreen(), rs.getOperation());
                    for (ScreenAuthority sa : screenAuths) {
                        roleAuthorityRepository.deleteById(
                                new RoleAuthorityId(roleId, sa.getAuthorityName())
                        );
                    }

                    // بعدها نحذف الـ role_screen
                    roleScreenRepository.delete(rs);
                });

        // 6. أضف الجديد فقط
        requests.stream()
                .filter(req -> toAdd.contains(req.getScreen().name() + "_" + req.getPermission().name()))
                .forEach(req -> {
                    RoleScreen rs = RoleScreen.builder()
                            .role(role)
                            .roleId(roleId)
                            .screen(req.getScreen().name())
                            .operation(req.getPermission())
                            .build();
                    roleScreenRepository.save(rs);

                    // authorities
                    if (req.getPermission() == Operation.ALL) {
                        for (Operation op : List.of(Operation.VIEW, Operation.EDIT)) {
                            addAuthorities(roleId, role, req.getScreen(), op);
                        }
                    } else {
                        addAuthorities(roleId, role, req.getScreen(), req.getPermission());
                    }
                });
    }






    private void addAuthorities(Long roleId, Role role, Screen screen, Operation op) {
        List<ScreenAuthority> screenAuths =
                screenAuthorityRepository.findByScreenAndOperation(screen.name(), op);

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
        // 1. Fetch authorities
        List<RoleAuthority> roleAuthorities = roleAuthorityRepository.findByIdRoleId(roleId);
        System.out.println(">>> ROLE_AUTHORITY records found: " + roleAuthorities.size());

        List<String> authorityNames = roleAuthorities.stream()
                .map(ra -> ra.getId().getAuthorityName())
                .toList();

        // 2. Match against screen_authority
        List<ScreenAuthority> screenAuthorities =
                screenAuthorityRepository.findByAuthorityNameIn(authorityNames);

        if (screenAuthorities.isEmpty()) {
            System.out.println(">>> No matching screen_authority found for roleId=" + roleId);
        }

        // 3. Return DTO
        return screenAuthorities.stream()
                .map(sa -> new RoleScreenRequest(
                        Screen.fromValue(sa.getScreen()),  // safer mapping
                        sa.getOperation()
                ))
                .toList();
    }
}
