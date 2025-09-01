package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.AllergenType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Allergens implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @NotNull
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    private AllergenType type;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @NotNull
    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;

    @Column(name = "created_date")
    private Instant createdDate;

    @Column(name = "last_modified_by", length = 50)
    private String lastModifiedBy;

    @Column(name = "last_modified_date")
    private Instant lastModifiedDate;
}
