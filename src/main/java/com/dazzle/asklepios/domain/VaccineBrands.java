package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.MeasurementUnit;
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
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "vaccine_brands")
public class VaccineBrands extends AbstractAuditingEntity<Long> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "vaccine_id", nullable = false)
    private Vaccine vaccine;

    @NotNull
    @Column(nullable = false, length = 255)
    private String name;

    @NotNull
    @Column(name = "manufacture", nullable = false, length = 255)
    private String manufacture;

    @NotNull
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal volume;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MeasurementUnit unit;

    @Column(name = "marketing_authorization_holder", length = 255)
    private String marketingAuthorizationHolder;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
