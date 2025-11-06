package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.LoincCategory;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.Instant;

@Entity
@Table(name = "loinc_code")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoincCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private LoincCategory category;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated = Instant.now();
}
