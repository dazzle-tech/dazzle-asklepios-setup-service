package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Role;
import com.dazzle.asklepios.domain.User;
import com.dazzle.asklepios.domain.UserRole;
import com.dazzle.asklepios.repository.RoleRepository;
import com.dazzle.asklepios.repository.UserRepository;
import com.dazzle.asklepios.repository.UserRoleRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserRoleService {

    private final UserRoleRepository userRoleRepository;
   private final UserRepository userRepository;
   private final RoleRepository roleRepository;
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundAlertException("user","notfound","User not found:"+userId ));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new NotFoundAlertException("role","notfound","Role not found: " + roleId));

        UserRole userRole = UserRole.builder()
                .id(new UserRole.UserRoleId())
                .user(user)
                .role(role)
                .build();
        return userRoleRepository.save(userRole);
    }

    public void delete(Long userId, Long roleId) {
        userRoleRepository.deleteById(new UserRole.UserRoleId(userId, roleId));
    }
    public List<User> findUsersByFacilityId(Long facilityId) {
        return userRoleRepository.findDistinctUsersByFacilityId(facilityId);
    }
}

