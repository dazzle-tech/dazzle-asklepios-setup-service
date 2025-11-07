package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.AgeUnit;
import com.dazzle.asklepios.domain.enumeration.Condition;
import com.dazzle.asklepios.domain.enumeration.NormalRangeType;
import com.dazzle.asklepios.domain.enumeration.TestResultType;
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
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "diagnostic_test_normal_range")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticTestNormalRange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private DiagnosticTest test;

    private String gender;

    private Double ageFrom;

    @Enumerated(EnumType.STRING)
    private AgeUnit ageFromUnit;

    private Double ageTo;

    @Enumerated(EnumType.STRING)
    private AgeUnit ageToUnit;

    @Enumerated(EnumType.STRING)
    private Condition condition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TestResultType resultType;

    private String resultText;

    private String resultLov; // optional single LOV key

    @Enumerated(EnumType.STRING)
    private NormalRangeType normalRangeType;

    private Double rangeFrom;
    private Double rangeTo;

    private Boolean criticalValue = false;
    private Double criticalValueLessThan;
    private Double criticalValueMoreThan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_test_id")
    private DiagnosticTestProfile profileTest;

    private Boolean isProfile = false;

    // transient field - not persisted, comes from frontend
    @Transient
    private List<String> lovKeys;
}
