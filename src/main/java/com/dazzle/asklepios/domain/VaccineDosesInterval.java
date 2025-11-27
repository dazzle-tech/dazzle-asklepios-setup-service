package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.AgeUnit;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
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
@Table( name = "vaccine_doses_interval")
public class VaccineDosesInterval extends AbstractAuditingEntity<Long> implements Serializable {

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
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "from_dose_id", nullable = false)
    private VaccineDoses fromDose;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "to_dose_id", nullable = false)
    private VaccineDoses toDose;

    @NotNull
    @Column(name = "interval_between_doses", nullable = false, precision = 10, scale = 2)
    private BigDecimal intervalBetweenDoses;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "unit", nullable = false, length = 50)
    private AgeUnit unit;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
