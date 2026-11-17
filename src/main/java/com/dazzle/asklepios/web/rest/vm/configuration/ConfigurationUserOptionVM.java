package com.dazzle.asklepios.web.rest.vm.configuration;

import com.dazzle.asklepios.domain.User;
import com.dazzle.asklepios.domain.enumeration.JobRole;

import java.io.Serializable;

/**
 * Lightweight user row for the optional searchable user field on the configuration form.
 */
public record ConfigurationUserOptionVM(
        Long id,
        String login,
        String firstName,
        String lastName,
        String email,
        JobRole jobRole
) implements Serializable {

    public static ConfigurationUserOptionVM ofEntity(User user) {
        return new ConfigurationUserOptionVM(
                user.getId(),
                user.getLogin(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getJobRole()
        );
    }
}
