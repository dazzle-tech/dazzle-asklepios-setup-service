package com.dazzle.asklepios.domain.childgrowth;

import com.dazzle.asklepios.domain.enumeration.ChildGrowthAgeUnit;
import com.dazzle.asklepios.domain.enumeration.ChildGrowthAlertRecipient;
import com.dazzle.asklepios.domain.enumeration.ChildGrowthBandTone;
import com.dazzle.asklepios.domain.enumeration.ChildGrowthChartKind;
import com.dazzle.asklepios.domain.enumeration.ChildGrowthIndicator;
import com.dazzle.asklepios.domain.enumeration.ChildGrowthMissingSex;
import com.dazzle.asklepios.domain.enumeration.ChildGrowthSex;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The child growth configuration edited as one document: chart sets, selection rules,
 * z-score bands, entry limits, and alert rules.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChildGrowthDocument {

    @NotNull
    @Builder.Default
    private List<Chart> charts = new ArrayList<>();

    @NotNull
    private Rules rules;

    @NotNull
    @Builder.Default
    private Map<ChildGrowthIndicator, List<Band>> bandsByIndicator = new EnumMap<>(ChildGrowthIndicator.class);

    @NotNull
    private Limits limits;

    @NotNull
    private Alerts alerts;

    public static ChildGrowthDocument example() {
        return ChildGrowthDocument.builder()
                .charts(List.of(
                        chart("who", "WHO Child Growth Standards", "Default for age",
                                ChildGrowthChartKind.DEFAULT, 0, 24, true),
                        chart("cdc", "CDC Growth Charts", "Default for age",
                                ChildGrowthChartKind.DEFAULT, 24, 240, true),
                        chart("preterm", "Preterm chart", "By gestational age, born before 37 weeks",
                                ChildGrowthChartKind.PRETERM, 22, 50, true),
                        chart("down", "Down syndrome chart", "By diagnosis on the problem list",
                                ChildGrowthChartKind.DIAGNOSIS, 0, 240, true),
                        chart("custom", "Custom or national chart", "Upload an LMS table (age, sex, L, M, S)",
                                ChildGrowthChartKind.CUSTOM, null, null, false)
                ))
                .rules(Rules.builder()
                        .missingSex(ChildGrowthMissingSex.ASK)
                        .correctedAgeUntilMonths(24)
                        .allowOverride(true)
                        .showInParentPortal(true)
                        .build())
                .bandsByIndicator(exampleBands())
                .limits(Limits.builder()
                        .weightMinKg(new BigDecimal("0.3"))
                        .weightMaxKg(new BigDecimal("150"))
                        .lengthMinCm(new BigDecimal("25"))
                        .lengthMaxCm(new BigDecimal("220"))
                        .headMinCm(new BigDecimal("20"))
                        .headMaxCm(new BigDecimal("65"))
                        .build())
                .alerts(Alerts.builder().consecutiveVisits(2).channelsCrossed(2).build())
                .build();
    }

    private static Map<ChildGrowthIndicator, List<Band>> exampleBands() {
        Map<ChildGrowthIndicator, List<Band>> bands = new EnumMap<>(ChildGrowthIndicator.class);
        for (ChildGrowthIndicator indicator : ChildGrowthIndicator.values()) {
            bands.put(indicator, List.of(
                    band("action-low", "Action · low", ChildGrowthBandTone.ACTION, "-5", "-2", "below 2nd",
                            List.of(ChildGrowthAlertRecipient.PHYSICIAN, ChildGrowthAlertRecipient.TASK)),
                    band("watch-low", "Watch · low", ChildGrowthBandTone.WATCH, "-2", "-1", "2nd to 16th",
                            List.of(ChildGrowthAlertRecipient.NURSE)),
                    band("normal", "Normal", ChildGrowthBandTone.NORMAL, "-1", "1", "16th to 84th", List.of()),
                    band("watch-high", "Watch · high", ChildGrowthBandTone.WATCH, "1", "2", "84th to 98th",
                            List.of(ChildGrowthAlertRecipient.NURSE)),
                    band("action-high", "Action · high", ChildGrowthBandTone.ACTION, "2", "5", "above 98th",
                            List.of(ChildGrowthAlertRecipient.PHYSICIAN, ChildGrowthAlertRecipient.TASK))
            ));
        }
        return bands;
    }

    private static Chart chart(
            String id,
            String name,
            String description,
            ChildGrowthChartKind kind,
            Integer ageFrom,
            Integer ageTo,
            boolean enabled
    ) {
        return Chart.builder()
                .id(id)
                .name(name)
                .description(description)
                .sex(ChildGrowthSex.BOTH)
                .ageFrom(ageFrom)
                .ageTo(ageTo)
                .unit(ChildGrowthAgeUnit.MONTHS)
                .enabled(enabled)
                .kind(kind)
                .build();
    }

    private static Band band(
            String id,
            String label,
            ChildGrowthBandTone tone,
            String min,
            String max,
            String percentile,
            List<ChildGrowthAlertRecipient> alerted
    ) {
        return Band.builder()
                .id(id)
                .label(label)
                .tone(tone)
                .min(new BigDecimal(min))
                .max(new BigDecimal(max))
                .percentile(percentile)
                .alerted(ChildGrowthAlertRecipient.copyOf(alerted))
                .build();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Chart {
        @NotBlank
        private String id;
        @NotBlank
        private String name;
        private String description;
        @NotNull
        private ChildGrowthSex sex;
        private Integer ageFrom;
        private Integer ageTo;
        @NotNull
        private ChildGrowthAgeUnit unit;
        @NotNull
        private Boolean enabled;
        @NotNull
        private ChildGrowthChartKind kind;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Rules {
        @NotNull
        private ChildGrowthMissingSex missingSex;
        @NotNull
        @Min(0)
        private Integer correctedAgeUntilMonths;
        @NotNull
        private Boolean allowOverride;
        @NotNull
        private Boolean showInParentPortal;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Band {
        @NotBlank
        private String id;
        @NotBlank
        private String label;
        @NotNull
        private ChildGrowthBandTone tone;
        @NotNull
        private BigDecimal min;
        @NotNull
        private BigDecimal max;
        @NotBlank
        private String percentile;

        @NotNull
        @Builder.Default
        @Setter(AccessLevel.NONE)
        @JsonDeserialize(using = ChildGrowthAlertedDeserializer.class)
        @JsonAlias("alert")
        private List<ChildGrowthAlertRecipient> alerted = new ArrayList<>();

        @JsonDeserialize(using = ChildGrowthAlertedDeserializer.class)
        @JsonAlias("alert")
        public void setAlerted(List<ChildGrowthAlertRecipient> alerted) {
            this.alerted = ChildGrowthAlertRecipient.copyOf(alerted);
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Limits {
        @NotNull
        @DecimalMin("0")
        private BigDecimal weightMinKg;
        @NotNull
        @DecimalMin("0")
        private BigDecimal weightMaxKg;
        @NotNull
        @DecimalMin("0")
        private BigDecimal lengthMinCm;
        @NotNull
        @DecimalMin("0")
        private BigDecimal lengthMaxCm;
        @NotNull
        @DecimalMin("0")
        private BigDecimal headMinCm;
        @NotNull
        @DecimalMin("0")
        private BigDecimal headMaxCm;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Alerts {
        @NotNull
        @Min(1)
        private Integer consecutiveVisits;
        @NotNull
        @Min(1)
        private Integer channelsCrossed;
    }
}
