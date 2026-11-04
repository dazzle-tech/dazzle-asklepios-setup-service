package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.biling.PriceAttributes;
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
@Table(name = "price_list_attributes")
public class PriceListAttribute  implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK to price_list (real FK in DB)
    @NotNull(message = "Price List cannot be empty")
    @Column(name = "price_list_id", nullable = false)
    private Long priceListId;

    @NotNull(message = "Attribute Type cannot be empty")
    @Enumerated(EnumType.STRING)
    @Column(name = "attribute_type", nullable = false, length = 40)
    private PriceAttributes attributeType;

    @NotNull(message = "Attribute List cannot be empty")
    @Column(name = "attribute", nullable = false, length = 50)
    private String attribute;

    @NotNull(message = "Price  cannot be empty")
    @Column(name = "price", nullable = false, precision = 19, scale = 4)
    private BigDecimal price;


    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
