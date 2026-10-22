package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.SupportiveDocumentRequirementCode;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Per-Payor supportive-document configuration header.
 * Presence of a row means the Payor is configured (even with zero selected codes).
 */
@Entity
@Table(
        name = "payor_supportive_document_requirement",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_payor_supportive_document_requirement_payor",
                columnNames = "payor_id"
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class PayorSupportiveDocumentRequirement extends AbstractAuditingEntity<Long> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "payor_id", nullable = false)
    private Long payorId;

    @Builder.Default
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "payor_supportive_document_requirement_code",
            joinColumns = @JoinColumn(
                    name = "configuration_id",
                    nullable = false,
                    foreignKey = @ForeignKey(name = "fk_payor_supportive_document_requirement_code_configuration")
            ),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_payor_supportive_document_requirement_code",
                    columnNames = { "configuration_id", "requirement_code" }
            )
    )
    @Column(name = "requirement_code", nullable = false, length = 64)
    @Enumerated(EnumType.STRING)
    private Set<SupportiveDocumentRequirementCode> requirementCodes = new LinkedHashSet<>();
}
