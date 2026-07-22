package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.DiscountApplicableOn;
import com.dazzle.asklepios.domain.enumeration.DiscountType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "discount")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Discount extends AbstractAuditingEntity<Long>
        implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull
    @Column(name = "facility_id",nullable = false)
    private Long facilityId;

    @NotNull
    @Size(max = 50)
    @Column(name = "code",nullable = false, length = 50 )
    private String code;

    @NotNull
    @Size(max = 150)
    @Column(name = "name",nullable = false,length = 150)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type",nullable = false,length = 30)
    private DiscountType discountType;

    @Column(
            name = "percentage",
            precision = 10,
            scale = 4
    )
    private BigDecimal percentage;

    @Column(
            name = "fixed_amount",
            precision = 19,
            scale = 4
    )
    private BigDecimal fixedAmount;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "currency",
            length = 30
    )
    private Currency currency;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(
            name = "applicable_on",
            nullable = false,
            length = 30
    )
    private DiscountApplicableOn applicableOn;

    @NotNull
    @Column(
            name = "valid_from",
            nullable = false
    )
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    @Column(
            name = "maximum_discount_amount",
            precision = 19,
            scale = 4
    )
    private BigDecimal maximumDiscountAmount;

    @Column(
            name = "minimum_invoice_amount",
            precision = 19,
            scale = 4
    )
    private BigDecimal minimumInvoiceAmount;

    @NotNull
    @Builder.Default
    @Column(
            name = "requires_reason",
            nullable = false
    )
    private Boolean requiresReason = false;

    @NotNull
    @Builder.Default
    @Column(
            name = "requires_approval",
            nullable = false
    )
    private Boolean requiresApproval = false;

    @NotNull
    @Builder.Default
    @Column(
            name = "combinable",
            nullable = false
    )
    private Boolean combinable = false;

    @NotNull
    @Builder.Default
    @Column(
            name = "is_default",
            nullable = false
    )
    private Boolean isDefault = false;

    @NotNull
    @Builder.Default
    @Column(name = "active",nullable = false)
    private Boolean active = true;

    @Size(max = 500)
    @Column(name = "description")
    private String description;
}