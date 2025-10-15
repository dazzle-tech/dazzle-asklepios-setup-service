package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.MedicalSheets;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(
        name = "department_medical_sheets_nurse_visibility",
        uniqueConstraints = @UniqueConstraint(columnNames = {"department_id", "medical_sheet"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class DepartmentMedicalSheetsNurseVisbility extends AbstractAuditingEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @EqualsAndHashCode.Include
    @Column(name = "department_id", nullable = false)
    private Long departmentId;

    @EqualsAndHashCode.Include
    @Enumerated(EnumType.STRING)
    @Column(name = "medical_sheet", nullable = false)
    private MedicalSheets medicalSheet;
}
