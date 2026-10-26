package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.DurationUnit;
import com.dazzle.asklepios.domain.enumeration.NumberOfDoses;
import com.dazzle.asklepios.domain.enumeration.RouteOfAdministration;
import com.dazzle.asklepios.domain.enumeration.VaccineType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "vaccine")
public class Vaccine extends AbstractAuditingEntity<Long> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "atc_code", length = 50)
    private String atcCode;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "number_of_doses", nullable = false, length = 20)
    private NumberOfDoses numberOfDoses;


    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private VaccineType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private RouteOfAdministration roa;

    @Column(name = "site_of_administration", length = 255)
    private String siteOfAdministration;

    @Column(name = "post_opening_duration", precision = 10, scale = 2)
    private BigDecimal postOpeningDuration;

    @Enumerated(EnumType.STRING)
    @Column(name = "duration_unit", length = 50)
    private DurationUnit durationUnit;


    @Column(name = "indications", columnDefinition = "text")
    private String indications;

    @Column(name = "possible_reactions", columnDefinition = "text")
    private String possibleReactions;

    @Column(name = "contraindications_and_precautions", columnDefinition = "text")
    private String contraindicationsAndPrecautions;

    @Column(name = "storage_and_handling", columnDefinition = "text")
    private String storageAndHandling;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

}
