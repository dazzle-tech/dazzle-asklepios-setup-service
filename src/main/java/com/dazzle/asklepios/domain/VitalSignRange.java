package com.dazzle.asklepios.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Configurable normal range for one Vital Signs measurement key (e.g. heartRate).
 * A vital sign can have several age rules; each rule covers [ageFromMonths, ageToMonths) and its limits stay
 * empty (null) until configured in Setup.
 */
@Entity
@Table(name = "vital_sign_range")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class VitalSignRange extends AbstractAuditingEntity<Long> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Measurement key already used by the Vital Signs form, e.g. bloodPressureSystolic. */
    @Column(name = "vital_sign_key", nullable = false, updatable = false, length = 50)
    private String vitalSignKey;

    @Column(name = "unit", length = 30)
    private String unit;

    /** Age interval [ageFromMonths, ageToMonths) in whole months. ageToMonths null = no upper limit. */
    @Column(name = "age_from_months", nullable = false)
    private Integer ageFromMonths;

    @Column(name = "age_to_months")
    private Integer ageToMonths;

    @Column(name = "normal_min", precision = 10, scale = 2)
    private BigDecimal normalMin;

    @Column(name = "normal_max", precision = 10, scale = 2)
    private BigDecimal normalMax;
}
