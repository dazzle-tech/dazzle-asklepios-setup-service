package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;
import com.dazzle.asklepios.domain.enumeration.biling.BillingTrigger;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "billing_rule")
public class BillingRule extends AbstractAuditingEntity<Long>
        implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull
    @Size(max = 150)
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "billing_item_type", nullable = false, length = 50)
    private BillingItemTypes billingItemType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "billing_trigger", nullable = false, length = 50)
    private BillingTrigger billingTrigger;
}