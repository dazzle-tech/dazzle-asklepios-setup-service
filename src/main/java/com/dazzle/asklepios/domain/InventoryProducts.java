package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.InventoryType;
import com.dazzle.asklepios.domain.enumeration.ProductTypes;
import com.dazzle.asklepios.domain.enumeration.Unit;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@Table(name = "inventory_products")
public class InventoryProducts extends AbstractAuditingEntity<Long> implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ProductTypes type;

    @Column(length = 50)
    private String code;

    @Column(length = 100)
    private String barcode;

    @Column(name = "brand_id")
    private Long brandId;

    @Column(name = "uom_group_id")
    private Long uomGroupId;

    @Column(name = "base_uom",nullable = false)
    private Long baseUom;

    @Column(name = "dispense_uom")
    private Long dispenseUom;

    @Column(name = "controlled_substance")
    private Boolean controlledSubstance;

    @Column(name = "hazardous_biohazardous_tag")
    private String hazardousBiohazardousTag;

    @Column(name = "allergy_risk")
    private Boolean allergyRisk;

    @Column(name = "item_average_cost", precision = 10, scale = 3)
    private BigDecimal itemAverageCost;

    @Column(name = "price_per_base_uom", precision = 10, scale = 3)
    private BigDecimal pricePerBaseUom;

    @Column(name = "warranty_start_date")
    private Instant warrantyStartDate;

    @Column(name = "warranty_end_date")
    private Instant warrantyEndDate;

    @Column(name = "maintenance_schedule", precision = 10, scale = 3)
    private BigDecimal maintenanceSchedule;

    @Column(name = "maintenance_schedule_type", length = 50)
    private String maintenanceScheduleType;

    @Column(name = "critical_equipment")
    private Boolean criticalEquipment;

    @Column(name = "calibration_required")
    private Boolean calibrationRequired;

    @Column(name = "training_required")
    private Boolean trainingRequired;

    @Column(name = "batch_managed")
    private Boolean batchManaged;

    @Column(name = "expiry_date_mandatory")
    private Boolean expiryDateMandatory;

    @Column(name = "reusable")
    private Boolean reusable;

    @Column(name = "inventory_type", length = 50)
    @Enumerated(EnumType.STRING)
    private InventoryType inventoryType;

    @Column(name = "shelf_life")
    private Integer shelfLife;

    @Column(name = "shelf_life_unit", length = 50)
    @Enumerated(EnumType.STRING)
    private Unit shelfLifeUnit;

    @Column(name = "lead_time")
    private Integer leadTime;

    @Column(name = "lead_time_unit", length = 50)
    @Enumerated(EnumType.STRING)
    private Unit leadTimeUnit;

    @Column(name = "erp_integration_id", length = 50)
    private String erpIntegrationId;

    @Column(name = "is_active")
    private Boolean isActive;
}