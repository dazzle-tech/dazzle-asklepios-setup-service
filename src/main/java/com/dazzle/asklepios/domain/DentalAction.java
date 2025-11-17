package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.DentalActionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a dental action configuration.
 */
@Entity
@Table(name = "dental_action")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DentalAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "description", nullable = false)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private DentalActionType type; // Enum name DentalActionType

    @Column(name = "image_name")
    private String imageName;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
