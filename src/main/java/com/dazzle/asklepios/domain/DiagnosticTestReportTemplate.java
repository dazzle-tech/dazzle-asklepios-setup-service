package com.dazzle.asklepios.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
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
@EqualsAndHashCode(callSuper = false)
@Table(
        name = "diagnostic_test_report_template"
)
public class DiagnosticTestReportTemplate extends AbstractAuditingEntity<Long> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Diagnostic test cannot be null")
    @ManyToOne(optional = false)
    @JoinColumn(name = "diagnostic_test_id")
    private DiagnosticTest diagnosticTest;

    @NotBlank(message = "Template name cannot be null")
    @Column(name = "name", length = 255)
    private String name;

    @NotBlank(message = "Template value cannot be null")
    @Lob
    @Column(name = "template_value", columnDefinition = "text")
    private String templateValue; // HTML

    @Column(name = "is_active")
    private Boolean isActive = true;
}
