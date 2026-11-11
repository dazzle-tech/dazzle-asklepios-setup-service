package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.DentalChartType;
import com.dazzle.asklepios.domain.enumeration.ToothNumberingSystem;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Configurable age range → dental chart type mapping.
 * Ages are completed years: ageFrom inclusive, ageTo exclusive (null = open-ended).
 * Tooth inventory uses stable internal ids (Tooth1–Tooth32 / ToothA–ToothT);
 * numberingSystem only controls Setup display labels.
 */
@Entity
@Table(name = "dental_chart_age_rule")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DentalChartAgeRule extends AbstractAuditingEntity<Long> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Min(0)
    @Column(name = "age_from", nullable = false)
    private Integer ageFrom;

    /** Exclusive upper bound in completed years; null means open-ended. */
    @Min(0)
    @Column(name = "age_to")
    private Integer ageTo;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "chart_type", nullable = false, length = 50)
    private DentalChartType chartType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "numbering_system", nullable = false, length = 50)
    @Builder.Default
    private ToothNumberingSystem numberingSystem = ToothNumberingSystem.UNIVERSAL;

    /**
     * Comma-separated stable tooth identifiers (e.g. Tooth1,ToothA).
     * Prefer {@link #getToothNumberList()} / {@link #setToothNumberList(List)}.
     */
    @Column(name = "tooth_numbers", columnDefinition = "text")
    private String toothNumbers;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Transient
    public List<String> getToothNumberList() {
        if (toothNumbers == null || toothNumbers.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(toothNumbers.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    @Transient
    public void setToothNumberList(List<String> values) {
        if (values == null || values.isEmpty()) {
            this.toothNumbers = null;
            return;
        }
        this.toothNumbers = values.stream()
                .filter(v -> v != null && !v.isBlank())
                .map(String::trim)
                .distinct()
                .collect(Collectors.joining(","));
    }
}
