package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.PriceListItemType;
import com.dazzle.asklepios.domain.enumeration.PricingMethod;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Table(name = "price_list_setup_item")
public class PriceListSetupItem extends AbstractAuditingEntity<Long>
        implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "price_list_setup_id", nullable = false)
    private Long priceListSetupId;

    @Column(name = "waseel_item_mapping_id")
    private Long waseelItemMappingId;

    @Column(name = "sbs_catalog_id")
    private Long sbsCatalogId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 50)
    private PriceListItemType itemType;

    @Column(name = "source_id")
    private Long sourceId;

    @NotBlank
    @Column(name = "item_code", nullable = false, length = 255)
    private String itemCode;

    @Column(name = "item_name", length = 1000)
    private String itemName;

    @NotNull
    @DecimalMin("0.0000")
    @Column(
            name = "unit_price",
            nullable = false,
            precision = 19,
            scale = 4
    )
    private BigDecimal unitPrice;

    @NotNull
    @DecimalMin("0.0000")
    @DecimalMax("100.0000")
    @Column(
            name = "discount_percentage",
            nullable = false,
            precision = 9,
            scale = 4
    )
    @Builder.Default
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    @NotNull
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @NotNull
    @Column(name = "requires_pre_authorization", nullable = false)
    @Builder.Default
    private Boolean requiresPreAuthorization = false;
}