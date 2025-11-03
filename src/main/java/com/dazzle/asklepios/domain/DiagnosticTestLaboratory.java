package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.Method;
import com.dazzle.asklepios.domain.enumeration.Property;
import com.dazzle.asklepios.domain.enumeration.Scale;
import com.dazzle.asklepios.domain.enumeration.System;
import com.dazzle.asklepios.domain.enumeration.Timing;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "diagnostic_test_laboratory")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticTestLaboratory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false, unique = true)
    private DiagnosticTest test;

    @Enumerated(EnumType.STRING)
    @Column(length = 200)
    private Property property;

    @Enumerated(EnumType.STRING)
    @Column(length = 200)
    private System system;

    @Enumerated(EnumType.STRING)
    @Column(length = 200)
    private Scale scale;

    @Column(length = 100)
    private String reagents;

    @Enumerated(EnumType.STRING)
    @Column(length = 200)
    private Method method;

    @Enumerated(EnumType.STRING)
    @Column(length = 200)
    private Timing timing;


    @Column(name = "test_duration_time")
    private Double testDurationTime;

    @Column(length = 200)
    private String timeUnit;

    @Column(length = 100, nullable = false)
    private String resultUnit;

    @Column(nullable = false)
    private Boolean isProfile = false;

    @Column(length = 200)
    private String sampleContainer;

    private Double sampleVolume;

    @Column(length = 200)
    private String sampleVolumeUnit;

    @Column(length = 200)
    private String tubeColor;

    @Column(length = 1000)
    private String testDescription;

    @Column(length = 1000)
    private String sampleHandling;

    private Double turnaroundTime;

    @Column(length = 200)
    private String turnaroundTimeUnit;

    @Column(length = 1000)
    private String preparationRequirements;

    @Column(length = 1000)
    private String medicalIndications;

    @Column(length = 1000)
    private String associatedRisks;

    @Column(length = 1000)
    private String testInstructions;

    @Column(length = 200, nullable = false)
    private String category;

    @Column(length = 200)
    private String tubeType;
}
