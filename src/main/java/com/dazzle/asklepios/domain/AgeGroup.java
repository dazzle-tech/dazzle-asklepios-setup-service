package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.AgeGroupType;
import com.dazzle.asklepios.domain.enumeration.AgeUnit;
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
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@Table(name = "age_group")
public class AgeGroup extends AbstractAuditingEntity<Long> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "age_group")
    private AgeGroupType ageGroup;

    @NotNull
    @PositiveOrZero
    @Column(name = "from_age")
    private BigDecimal fromAge;

    @NotNull
    @PositiveOrZero
    @Column(name = "to_age")
    private BigDecimal toAge;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "from_age_unit")
    private AgeUnit fromAgeUnit;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "to_age_unit")
    private AgeUnit toAgeUnit;

    @NotNull
    @Column(name = "is_active")
    private Boolean isActive;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;
}
