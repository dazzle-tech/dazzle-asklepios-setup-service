package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.UserRole;
import com.dazzle.asklepios.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserRoleService {

    private final UserRoleRepository userRoleRepository;

    public List<UserRole> findAll() {
        return userRoleRepository.findAll();
    }

    public List<UserRole> findByUserId(Long userId) {
        return userRoleRepository.findByIdUserId(userId);
    }

    public List<UserRole> findByRoleId(Long roleId) {
        return userRoleRepository.findByIdRoleId(roleId);
    }

    public UserRole save(Long userId, Long roleId) {
        return userRoleRepository.save(
                UserRole.builder()
                        .id(new UserRole.UserRoleId(userId, roleId))
                        .build()
        );

    }

    public void delete(Long userId, Long roleId) {
        userRoleRepository.deleteById(new UserRole.UserRoleId(userId, roleId));
    }
}

