package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.biling.PayorPlanType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
@Table(name = "payor_plan")
public class PayorPlan extends AbstractAuditingEntity<Long> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK to payor (real FK in DB)
    @NotNull
    @Column(name = "payor_id", nullable = false)
    private Long payorId;

    @NotNull
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", nullable = false, length = 50)
    private PayorPlanType planType;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;


}
