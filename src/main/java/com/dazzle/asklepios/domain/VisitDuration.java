package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.VisitType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "visit_duration")
@EqualsAndHashCode(callSuper = false)
public class VisitDuration extends AbstractAuditingEntity<Long> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "visit_type", nullable = false, length = 255)
    private VisitType visitType;

    @NotNull
    @Column(name = "duration_in_minutes", nullable = false)
    private Integer durationInMinutes;

    @NotNull
    @Column(name = "resource_specific", nullable = false)
    private Boolean resourceSpecific = false;
}
