package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.CdtClass;
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
@Table(name = "cdt_code")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CdtCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "class", nullable = false, length = 100)
    private CdtClass cdtClass;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated = Instant.now();
}
