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
@Table(name = "brand_medication")
public class BrandMedication extends AbstractAuditingEntity<Long> implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String code;

    @Column(length = 50)
    private String manufacturer;

    @Column(name = "dosage_form", nullable = false, length = 50)
    private String dosageForm;

    @Column(name = "usage_instructions", columnDefinition = "text")
    private String usageInstructions;

    @Column(name = "storage_requirements", columnDefinition = "text")
    private String storageRequirements;

    @Column(name = "expires_after_opening")
    private Boolean expiresAfterOpening;

    @Column(name = "expires_after_opening_value", precision = 10, scale = 3)
    private BigDecimal expiresAfterOpeningValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "expires_after_opening_unit", length = 50)
    private Unit expiresAfterOpeningUnit;

    @Column(name = "single_patient_use")
    private Boolean useSinglePatient;

    @Column(name = "high_cost_medication")
    private Boolean highCostMedication;

    @Column(name = "cost_category", length = 50)
    private String costCategory;

    @Column(length = 200)
    private String roa;

    @Column(name = "is_active")
    private Boolean isActive = true;

   @NotNull
   @ManyToOne(optional = false)
   @JoinColumn(name = "uom_group_id", nullable = false, foreignKey = @ForeignKey(name = "fk_bm_uom_group_id"))
    private UomGroup uomGroup;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "uom_group_unit_id", nullable = false, foreignKey = @ForeignKey(name = "fk_bm_uom_group_unit_id"))
    private UomGroupUnit uomGroupUnit;

}
