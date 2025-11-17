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

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@Table(name = "active_ingredient_drug_interactions")
public class ActiveIngredientDrugInteractions extends AbstractAuditingEntity<Long> implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "active_ingredient_id", nullable = false, foreignKey = @ForeignKey(name = "fk_aid_active_ingredient"))
    private ActiveIngredients activeIngredient;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "interacted_active_ingredient_id", nullable = false, foreignKey = @ForeignKey(name = "fk_aid_interacted_active_ingredient"))
    private ActiveIngredients interactedActiveIngredient;

    @Column(nullable = false, length = 50)
    private String severity;

    @Column(columnDefinition = "text")
    private String description;

}
