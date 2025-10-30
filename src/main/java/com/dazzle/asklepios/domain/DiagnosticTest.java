package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.AgeGroupType;
import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.TestType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jakarta.validation.constraints.NotNull;
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class DiagnosticTest extends AbstractAuditingEntity<Long> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Type cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 255)
    private TestType type;

    @NotNull(message = "Name cannot be null")
    @Column(name = "name", length = 255)
    private String name;

    @NotNull(message = "Code cannot be null")
    @Column(name = "internal_code", length = 255)
    private String internalCode;

    @Column(name = "age_specific")
    private Boolean ageSpecific;

    @Column(name = "age-group", columnDefinition = "text")
    private String ageGroup;

    @Transient
    private List<AgeGroupType> ageGroupList;

    @Column(name = "gender_specific")
    private Boolean genderSpecific;



    @Column(name = "gender", length = 50)
    private String gender;

    @Column(name = "special_population")
    private Boolean specialPopulation;
    @Column(name = "special_population_values", columnDefinition = "text")
    private String specialPopulationValuesRaw;

    @Transient
    private List<String> specialPopulationValues;

    @Column(name = "price", precision = 7, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", length = 10)
    private Currency currency;

    @Column(name = "special_notes", columnDefinition = "TEXT")
    private String specialNotes;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_profile")
    private Boolean isProfile;

    @Column(name = "appointable")
    private Boolean appointable;


    @PostLoad
    private void fillListFromRaw() {
        if (specialPopulationValues != null && !specialPopulationValuesRaw.isBlank()) {
            this.specialPopulationValues =
                    Arrays.asList(specialPopulationValuesRaw.split(","));
        } else {
            this.specialPopulationValues = new ArrayList<>();
        }
    }

    @PrePersist
    @PreUpdate
    private void fillRawFromList() {
        if (specialPopulationValues != null && !specialPopulationValues.isEmpty()) {
            this.specialPopulationValues =
                    this.specialPopulationValues.stream()
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .toList();
            this.specialPopulationValuesRaw =
                    String.join(",", this.specialPopulationValues);
        } else {
            this.specialPopulationValuesRaw = null;
        }
    }
}

