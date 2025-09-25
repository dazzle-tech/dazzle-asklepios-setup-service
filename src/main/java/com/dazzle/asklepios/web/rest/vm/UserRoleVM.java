package com.dazzle.asklepios.web.rest.vm;

import com.dazzle.asklepios.domain.UserRole;

public record UserRoleVM(Long userId, Long roleId) {

    public static UserRoleVM ofEntity(UserRole ur) {
        return new UserRoleVM(
                ur.getId().getUserId(),
                ur.getId().getRoleId()
        );
    }
}
