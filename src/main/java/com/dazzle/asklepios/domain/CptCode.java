package com.dazzle.asklepios.domain;
import com.dazzle.asklepios.domain.enumeration.CptCategory;
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
@Table(name = "cpt_code")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CptCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CptCategory category;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated = Instant.now();
}
