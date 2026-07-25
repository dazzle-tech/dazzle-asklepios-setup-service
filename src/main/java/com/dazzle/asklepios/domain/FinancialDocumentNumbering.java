package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.BillingConfigurationStatus;
import com.dazzle.asklepios.domain.enumeration.BillingResetFrequency;
import com.dazzle.asklepios.domain.enumeration.biling.FinancialDocumentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(
        name = "financial_document_numbering"
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialDocumentNumbering
        extends AbstractAuditingEntity<Long>
        implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull
    @Column(name = "facility_id", nullable = false)
    private Long facilityId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 30)
    private FinancialDocumentType documentType;

    @NotNull
    @Size(max = 20)
    @Column(name = "prefix", nullable = false, length = 20)
    private String prefix;

    @NotNull
    @Builder.Default
    @Column(name = "sequence_length", nullable = false)
    private Integer sequenceLength = 6;

    @NotNull
    @Builder.Default
    @Column(name = "include_year", nullable = false)
    private Boolean includeYear = true;

    @NotNull
    @Builder.Default
    @Column(name = "include_facility_code", nullable = false)
    private Boolean includeFacilityCode = false;

    @NotNull
    @Size(max = 5)
    @Builder.Default
    @Column(name = "number_separator", nullable = false, length = 5)
    private String numberSeparator = "-";

    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "reset_frequency", nullable = false, length = 20)
    private BillingResetFrequency resetFrequency =
            BillingResetFrequency.YEARLY;

    @NotNull
    @Builder.Default
    @Column(name = "starting_number", nullable = false)
    private Long startingNumber = 1L;

    @NotNull
    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private BillingConfigurationStatus status =
            BillingConfigurationStatus.DRAFT;
}
