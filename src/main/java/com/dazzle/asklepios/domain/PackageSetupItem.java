package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.PriceListItemType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * One item of a package. sourceId points to diagnostic_test (LABORATORY / RADIOLOGY / PATHOLOGY),
 * service, procedure, brand_medication or vaccine_brands depending on itemType.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Table(name = "package_setup_item")
public class PackageSetupItem extends AbstractAuditingEntity<Long> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "package_setup_id", nullable = false)
    private Long packageSetupId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 50)
    private PriceListItemType itemType;

    @NotNull
    @Column(name = "source_id", nullable = false)
    private Long sourceId;

    @Column(name = "item_code", length = 255)
    private String itemCode;

    @Column(name = "item_name", length = 1000)
    private String itemName;

    @NotNull
    @Min(1)
    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    /** Inactive items stay in the setup but are left out of packages given to patients. */
    @NotNull
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
