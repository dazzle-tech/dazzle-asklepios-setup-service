package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "price_list_item")
public class PriceListItem extends AbstractAuditingEntity<Long> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Price list id cannot be null")
    @Column(name = "price_list_id", nullable = false)
    private Long priceListId;

    @NotNull(message = "Item type cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", length = 20, nullable = false)
    private BillingItemTypes itemType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private ServiceSetup service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_medication_id")
    private BrandMedication brandMedication;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnostic_test_id")
    private DiagnosticTest diagnosticTest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedure_id")
    private Procedure procedure;

    @NotNull(message = "Price cannot be null")
    @Column(name = "price", precision = 19, scale = 4, nullable = false)
    private BigDecimal price;

    @NotNull
    @Column(name = "discount_allowed", nullable = false)
    private Boolean discountAllowed = false;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}