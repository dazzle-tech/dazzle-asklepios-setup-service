package com.dazzle.asklepios.domain;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "user_role")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRole implements Serializable {

    @EmbeddedId
    private UserRoleId id;


    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserRoleId implements java.io.Serializable {
        private Long userId;
        private Long roleId;
    }
}