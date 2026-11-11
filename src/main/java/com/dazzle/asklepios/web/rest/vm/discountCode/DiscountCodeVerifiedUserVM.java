package com.dazzle.asklepios.web.rest.vm.discountCode;

import java.io.Serializable;

public record DiscountCodeVerifiedUserVM(
        Long userId,
        String login,
        String firstName,
        String lastName,
        String authorizationToken
) implements Serializable {
}
