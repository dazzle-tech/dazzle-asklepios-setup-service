package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Role;
import com.dazzle.asklepios.domain.RoleAuthority;
import com.dazzle.asklepios.domain.RoleAuthorityId;
import com.dazzle.asklepios.repository.RoleAuthorityRepository;
import com.dazzle.asklepios.repository.RoleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleAuthorityService {

    private final RoleAuthorityRepository roleAuthorityRepository;
    private final RoleRepository roleRepository;

    public List<String> getAuthoritiesForRole(Long roleId) {
        return roleAuthorityRepository.findAuthorityNamesByRoleId(roleId);
    }

    @Transactional
    public void addAuthorityToRole(Long roleId, String authorityName) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleId));
        RoleAuthority ra = RoleAuthority.builder()
                .id(new RoleAuthorityId(roleId, authorityName))
                .role(role)
                .build();
        roleAuthorityRepository.save(ra);
    }

    @Transactional
    public void removeAuthorityFromRole(Long roleId, String authorityName) {
        roleAuthorityRepository.deleteById(new RoleAuthorityId(roleId, authorityName));
    }

    @Transactional
    public void clearAuthoritiesForRole(Long roleId) {
        roleAuthorityRepository.deleteByIdRoleId(roleId);
    }
}
