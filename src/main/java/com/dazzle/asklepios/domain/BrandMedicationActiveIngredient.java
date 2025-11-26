package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.Unit;
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
import jakarta.persistence.Table;
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
@Table(name = "brand_medication_active_ingredient")
public class BrandMedicationActiveIngredient extends AbstractAuditingEntity<Long> implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "brand_id", nullable = false, foreignKey = @ForeignKey(name = "fk_bma_brand_id"))
    private BrandMedication brandMedication;

    @ManyToOne(optional = false)
    @JoinColumn(name = "active_ingredient_id", nullable = false, foreignKey = @ForeignKey(name = "fk_bma_active_ingredient_id"))
    private ActiveIngredients activeIngredients;

    @Column(precision = 10, scale = 3)
    private BigDecimal strength;

    @Column(length = 50)
    private String unit;
}
