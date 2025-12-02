package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.biling.PriceListItemType;
import com.dazzle.asklepios.domain.enumeration.inventory.ProductTypes;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@EqualsAndHashCode(callSuper = false)
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
    private PriceListItemType itemType;



    // polymorphic target
    @Column(name = "service_id")
    private Long serviceId;

    @Column(name = "product_id")
    private Long productId;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", length = 30)
    private ProductTypes productType;


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
