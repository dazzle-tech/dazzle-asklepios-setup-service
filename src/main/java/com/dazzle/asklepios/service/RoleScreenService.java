package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Role;
import com.dazzle.asklepios.domain.RoleScreen;
import com.dazzle.asklepios.domain.enumeration.Operation;
import com.dazzle.asklepios.domain.enumeration.Screen;
import com.dazzle.asklepios.repository.RoleRepository;
import com.dazzle.asklepios.repository.RoleScreenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleScreenService {

    private final RoleScreenRepository roleScreenRepository;
    private final RoleRepository roleRepository;

    // استرجاع كل الشاشات المرتبطة برول
    public List<RoleScreen> getScreensForRole(Long roleId) {
        return roleScreenRepository.findByRoleId(roleId);
    }

    // إضافة شاشة مع صلاحية لرول
    @Transactional
    public RoleScreen addScreenToRole(Long roleId, Screen screen, Operation operation) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleId));

        RoleScreen rs = RoleScreen.builder()
                .role(role)
                .screen(screen.toString())
                .operation(operation)
                .build();

        return roleScreenRepository.save(rs);
    }


}
