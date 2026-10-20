package com.dazzle.asklepios.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Pharmaceutical of an active ingredient, e.g. "LORNOXICAM - 8 MG VIAL".
 * This is what the doctor prescribes, and its code is sent to external pharmacies.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@Table(name = "pharmaceutical")
public class Pharmaceutical extends AbstractAuditingEntity<Long> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "active_ingredient_id", nullable = false, foreignKey = @ForeignKey(name = "fk_pharmaceutical_active_ingredient"))
    private ActiveIngredients activeIngredient;

    /**
     * Name shown to the doctor, e.g. "LORNOXICAM - 8 MG VIAL".
     */
    @Column(name = "pharmaceutical_name", nullable = false, length = 255)
    private String pharmaceuticalName;

    @Column(name = "code", length = 50)
    private String code;

    @Column(name = "billing_code", length = 50)
    private String billingCode;

    @Column(name = "item_description", length = 255)
    private String itemDescription;

    @Column(name = "second_description", length = 255)
    private String secondDescription;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
