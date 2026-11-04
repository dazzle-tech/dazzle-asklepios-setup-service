package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.TestType;
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

import java.io.Serial;
import java.io.Serializable;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class Catalog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private TestType type;

    @ManyToOne(optional = true)
    @JoinColumn(name = "department_id", foreignKey = @ForeignKey(name = "fk_catalog_department"))
    private Department department;

    @ManyToOne(optional = true)
    @JoinColumn(name = "facility_id", foreignKey = @ForeignKey(name = "fk_catalog_facility"))
    private Facility facility;


    @Column(name = "appointable", nullable = false)
    private Boolean appointable = false;

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