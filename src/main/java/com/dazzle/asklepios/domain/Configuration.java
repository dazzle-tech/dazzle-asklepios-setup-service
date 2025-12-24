package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.ConfigurationKeys;
import com.dazzle.asklepios.domain.enumeration.ConfigurationReferenceType;
import com.dazzle.asklepios.domain.enumeration.ConfigurationValueType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class Configuration extends AbstractAuditingEntity<Long> implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nullable.
     * NULL = organization-level configuration
     * NOT NULL = facility-level configuration
     */
    @ManyToOne
    @JoinColumn(
            name = "facility_id",
            foreignKey = @ForeignKey(name = "fk_config_facility_id")
    )
    private Facility facility;

    @NotNull
    @Column(name = "key", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ConfigurationKeys key;

    @NotNull
    @Column(nullable = false, length = 50)
    private String value;

    @NotNull
    @Column(name = "valueType", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ConfigurationValueType valueType;

    @NotNull
    @Column(name = "referenceType", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ConfigurationReferenceType referenceType;

    @NotNull
    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
