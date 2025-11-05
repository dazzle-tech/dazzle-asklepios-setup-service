package com.dazzle.asklepios.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "diagnostic_test_pathology")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticTestPathology {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false, unique = true)
    private DiagnosticTest test;

    @Column(nullable = false)
    private String category;

    private String specimenType;
    private String analysisProcedure;
    private Double turnaroundTime;
    private String timeUnit;

    @Column(length = 1000)
    private String testDescription;
    @Column(length = 1000)
    private String sampleHandling;
    @Column(length = 1000)
    private String medicalIndications;
    @Column(length = 1000)
    private String criticalValues;
    @Column(length = 1000)
    private String preparationRequirements;
    @Column(length = 1000)
    private String associatedRisks;
}
