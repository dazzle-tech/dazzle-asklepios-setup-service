package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.MedicalSheets;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "department_medical_sheets_visibility")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class DepartmentMedicalSheetsVisibility extends AbstractAuditingEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "department_id", nullable = false)
    private Long departmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "medical_sheet", nullable = false)
    private MedicalSheets medicalSheet;

    @Column(name = "is_visible", nullable = false)
    private Boolean isVisible = true;
}
