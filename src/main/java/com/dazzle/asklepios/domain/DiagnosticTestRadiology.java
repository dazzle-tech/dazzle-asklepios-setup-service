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
@Table(name = "diagnostic_test_radiology")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticTestRadiology {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false, unique = true)
    private DiagnosticTest test;

    @Column(nullable = false)
    private String category;

    private Double imageDuration;
    @Column(length = 1000)
    private String testInstructions;
    @Column(length = 1000)
    private String medicalIndications;
    private String turnaroundTimeUnit;
    private Double turnaroundTime;
    @Column(length = 1000)
    private String associatedRisks;


}
