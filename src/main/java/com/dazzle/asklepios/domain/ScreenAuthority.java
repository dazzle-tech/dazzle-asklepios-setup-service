package com.dazzle.asklepios.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;

@Entity
@IdClass(ScreenAuthorityId.class)
@Table(name = "screen_authority")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScreenAuthority implements Serializable {

    @Id
    @NotNull
    @ManyToOne
    @JoinColumn(name = "screen_id", nullable = false)
    private Screen screen;

    @Id
    @NotNull
    @Column(name = "authority_name", nullable = false)
    private String authorityName;

    @Id
    @NotNull
    private String operation;
}
