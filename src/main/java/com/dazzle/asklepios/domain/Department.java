package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.DepartmentType;
import com.dazzle.asklepios.domain.enumeration.EncounterType;
import com.dazzle.asklepios.service.dto.workingDay.WorkingDayJson;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class Department extends AbstractAuditingEntity<Long> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "facility_id", nullable = false, foreignKey = @ForeignKey(name = "fk_department_facility"))
    private Facility facility;

    @NotNull
    @Column(nullable = false, length = 100)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private DepartmentType type;

    @Column
    private Boolean appointable;

    @NotNull
    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "phone_number", length = 50)
    private String phoneNumber;

    @Column(length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "encounter_type", length = 50)
    private EncounterType encounterType;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "has_medical_sheets")
    private Boolean hasMedicalSheets = false;

    @Column(name = "has_nurse_medical_sheets")
    private Boolean hasNurseMedicalSheets = false;

    @NotNull
    @Column(name = "parallel_capacity_enabled", nullable = false)
    private Boolean parallelCapacityEnabled = false;

    @Column(name = "parallel_capacity_value")
    private Integer parallelCapacityValue = 1;

    @Column(name = "default_duration_minutes")
    private Integer defaultDurationMinutes;

    @Column(name = "default_buffer_before_minutes")
    private Integer defaultBufferBeforeMinutes = 0;

    @Column(name = "default_buffer_after_minutes")
    private Integer defaultBufferAfterMinutes = 0;

    @Column(name = "require_practitioner")
    private Boolean requirePractitioner = false;

    @Column(name = "require_billing")
    private Boolean requireBilling = false;

    @Column(name = "require_pre_assessment")
    private Boolean requirePreAssessment = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "working_days", columnDefinition = "json", nullable = false)
    @Builder.Default
    private List<WorkingDayJson> workingDays = new ArrayList<>();
}
