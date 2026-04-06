package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.DepartmentType;
import com.dazzle.asklepios.domain.enumeration.Gender;
import com.dazzle.asklepios.domain.enumeration.RoomType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

@Entity
@Table(name = "rooms")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Room extends AbstractAuditingEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "department_type", nullable = false, length = 50)
    private DepartmentType departmentType;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @NotNull
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private RoomType type;

    @Column(name = "floor", length = 20)
    private String floor;

    @NotNull
    @Column(name = "is_specific_gender", nullable = false)
    private Boolean isSpecificGender = false;

    @Builder.Default
    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender;

    @NotNull
    @Column(name = "appointable", nullable = false)
    private Boolean appointable = false;;

    @NotNull
    @Column(name = "parallel_capacity_value", nullable = false)
    private Integer parallelCapacityValue = 1;

    @Column(name = "default_duration_minutes")
    private Integer defaultDurationMinutes;

    @Column(name = "default_buffer_before_minutes")
    private Integer defaultBufferBeforeMinutes = 0;

    @Column(name = "default_buffer_after_minutes")
    private Integer defaultBufferAfterMinutes = 0;
}