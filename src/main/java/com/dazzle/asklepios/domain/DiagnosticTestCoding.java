package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.MedicalCodeType;
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
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "diagnostic_test_coding")
public class DiagnosticTestCoding extends AbstractAuditingEntity<Long> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnostic_test_id", nullable = false)
    private DiagnosticTest diagnosticTest;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "code_type", nullable = false, length = 100)
    private MedicalCodeType codeType;

    @NotNull
    @Column(name = "code_id", nullable = false, length = 100)
    private String codeId;
}
