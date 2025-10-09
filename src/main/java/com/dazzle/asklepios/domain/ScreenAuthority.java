package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.Operation;
import com.dazzle.asklepios.domain.enumeration.Screen;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "screen_authority")
public class ScreenAuthority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull(message = "screen must not be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "screen", nullable = false, length = 100)
    private Screen screen;

    @NotNull(message = "operation must not be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "operation", nullable = false, length = 50)
    private Operation operation;

    @NotNull(message = "authorityName must not be null")
    @Column(name = "authority_name", nullable = false, length = 100)
    private String authorityName;
}

