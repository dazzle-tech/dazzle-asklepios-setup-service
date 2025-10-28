package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.TestType;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class DiagnosticTest extends AbstractAuditingEntity<Long> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "test_type", length = 255)
    private TestType testType;

    @Column(name = "test_name", length = 255)
    private String testName;

    @Column(name = "internal_code", length = 255)
    private String internalCode;

    @Column(name = "age_specific")
    private Boolean ageSpecific;

    @Column(name = "gender_specific")
    private Boolean genderSpecific;

    @Column(name = "gender", length = 50)
    private String gender;

    @Column(name = "special_population")
    private Boolean specialPopulation;

    @Column(name = "price", precision = 7, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", length = 10)
    private Currency currency;

    @Column(name = "special_notes", columnDefinition = "TEXT")
    private String specialNotes;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "appointable")
    private Boolean appointable;

    @Column(name = "created_date")
    private Instant createdDate;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "last_modified_by", length = 50)
    private String lastModifiedBy;

    @Column(name = "last_modified_date")
    private Instant lastModifiedDate;
}
