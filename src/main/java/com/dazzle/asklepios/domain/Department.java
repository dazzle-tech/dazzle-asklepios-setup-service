package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Department implements Serializable {

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
    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @NotNull
    @Column(name = "type", nullable = false, length = 50)
    @Convert(converter = DepartmentTypeConverter.class)
    private DepartmentType departmentType;

    @Column(name = "last_modified_by", length = 50)
    private String lastModifiedBy;

    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    @Column
    private Boolean appointable;

    @NotNull
    @Column(name = "department_code", nullable = false, length = 50)
    private String departmentCode;

    @Column(name = "phone_number", length = 50)
    private String phoneNumber;

    @Column(length = 100)
    private String email;

    @Column(name = "encounter_type", length = 50)
    @Convert(converter = EncounterTypeConverter.class)
    private EncounterType encounterType;

    @Column(name = "is_active")
    private Boolean isActive;
}
