package com.dazzle.asklepios.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * A package: a set of items, each with a quantity, and an optional fixed price.
 * Named PackageSetup because "Package" clashes with java.lang.Package.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Table(name = "package_setup")
public class PackageSetup extends AbstractAuditingEntity<Long> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(name = "code", nullable = false, length = 50, unique = true)
    private String code;

    @NotBlank
    @Size(max = 255)
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Size(max = 1000)
    @Column(name = "description", length = 1000)
    private String description;

    @DecimalMin("0.0000")
    @Column(name = "price", precision = 19, scale = 4)
    private BigDecimal price;

    /** Empty dates mean no limit on that side. */
    @Column(name = "valid_from")
    private java.time.LocalDate validFrom;

    @Column(name = "valid_to")
    private java.time.LocalDate validTo;

    /** Optional link to a discount setup record. */
    @Column(name = "discount_id")
    private Long discountId;

    /** true: any department; false: only the departments in package_setup_department. */
    @NotNull
    @Column(name = "all_departments", nullable = false)
    @Builder.Default
    private Boolean allDepartments = true;

    /** true: when the package is given to a visit, the visit's default service is cancelled. */
    @NotNull
    @Column(name = "override_default_service", nullable = false)
    @Builder.Default
    private Boolean overrideDefaultService = false;

    @NotNull
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
