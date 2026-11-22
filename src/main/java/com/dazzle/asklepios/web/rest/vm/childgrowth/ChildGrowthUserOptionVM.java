package com.dazzle.asklepios.web.rest.vm.childgrowth;

import com.dazzle.asklepios.domain.User;

public record ChildGrowthUserOptionVM(
        Long id,
        String firstName,
        String lastName,
        String login
) {
    public static ChildGrowthUserOptionVM from(User user) {
        return new ChildGrowthUserOptionVM(user.getId(), user.getFirstName(), user.getLastName(), user.getLogin());
    }
}
