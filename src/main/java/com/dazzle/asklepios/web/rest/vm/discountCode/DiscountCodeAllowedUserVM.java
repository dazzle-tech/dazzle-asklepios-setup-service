package com.dazzle.asklepios.web.rest.vm.discountCode;

import com.dazzle.asklepios.domain.DiscountCodeAllowedUser;
import com.dazzle.asklepios.domain.User;

import java.io.Serializable;

public record DiscountCodeAllowedUserVM(
        Long id,
        Long userId,
        String login,
        String firstName,
        String lastName,
        String email
) implements Serializable {

    public static DiscountCodeAllowedUserVM ofEntity(DiscountCodeAllowedUser entity) {
        User user = entity.getUser();
        return new DiscountCodeAllowedUserVM(
                entity.getId(),
                user != null ? user.getId() : null,
                user != null ? user.getLogin() : null,
                user != null ? user.getFirstName() : null,
                user != null ? user.getLastName() : null,
                user != null ? user.getEmail() : null
        );
    }
}
