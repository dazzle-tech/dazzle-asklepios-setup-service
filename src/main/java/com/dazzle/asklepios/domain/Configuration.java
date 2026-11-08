package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.ConfigurationKeys;
import com.dazzle.asklepios.domain.enumeration.ConfigurationReferenceType;
import com.dazzle.asklepios.domain.enumeration.ConfigurationScope;
import com.dazzle.asklepios.domain.enumeration.ConfigurationValueType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Configuration extends AbstractAuditingEntity<Long> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /**
     * ORGANIZATION: facility empty, users empty. Global default for every facility.
     * FACILITY:     facility selected, users empty. Override for one clinic or hospital.
     * USER:         facility and users selected. Personal or role-specific override.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type", nullable = false, length = 20)
    @Builder.Default
    private ConfigurationScope scopeType = ConfigurationScope.ORGANIZATION;

    @ManyToOne
    @JoinColumn(
            name = "facility_id",
            foreignKey = @ForeignKey(name = "fk_config_facility_id")
    )
    private Facility facility;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "configuration_user",
            joinColumns = @JoinColumn(name = "configuration_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<User> users = new HashSet<>();

    @NotNull
    @Column(name = "key", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ConfigurationKeys key;

    @NotNull
    @Column(nullable = false, columnDefinition = "text")
    private String value;

    @NotNull
    @Column(name = "value_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ConfigurationValueType valueType;

    @NotNull
    @Column(name = "reference_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ConfigurationReferenceType referenceType;

    @NotNull
    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @NotNull
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
