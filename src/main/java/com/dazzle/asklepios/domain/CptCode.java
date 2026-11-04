package com.dazzle.asklepios.domain;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
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

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "code_category", nullable = false, length = 50)
    private String codeCategory = "CPT";

    @Column(name = "service_category", nullable = false, length = 200)
    private String serviceCategory;

    @Column(name = "main_category", nullable = false, length = 200)
    private String mainCategory;

    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated = Instant.now();
}
