package com.dazzle.asklepios.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "icd10_code")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Icd10Code {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String description;

    @Column
    private String version;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;


    @Column(name = "last_updated")
    private Instant lastUpdated = Instant.now();
}
