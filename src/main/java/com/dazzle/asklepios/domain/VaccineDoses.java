package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.AgeUnit;
import com.dazzle.asklepios.domain.enumeration.DoseNumber;
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
@Table(name = "vaccine_doses")
public class VaccineDoses extends AbstractAuditingEntity<Long> implements Serializable {

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
    @Enumerated(EnumType.STRING)
    @Column(name = "dose_number", nullable = false, length = 50)
    private DoseNumber doseNumber;


    @Column(name = "from_age", precision = 10, scale = 2)
    private BigDecimal fromAge;

    @Column(name = "to_age", precision = 10, scale = 2)
    private BigDecimal toAge;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_age_unit", length = 50)
    private AgeUnit fromAgeUnit;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_age_unit", length = 50)
    private AgeUnit toAgeUnit;

    @NotNull
    @Column(name = "is_booster", nullable = false)
    private Boolean isBooster = false;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
