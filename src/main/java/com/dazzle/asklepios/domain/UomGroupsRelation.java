package com.dazzle.asklepios.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "uom_groups_relation")
public class UomGroupsRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // numeric → BigDecimal for precise conversion ratio
    @Column(precision = 19, scale = 6)
    private BigDecimal relation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "uom_group_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_uom_group_relation_uom")
    )
    @JsonBackReference("group-relations")
    private UomGroup group;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "uom_unit_from_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_uom_unit_from")
    )
    private UomGroupUnit fromUnit;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "uom_unit_to_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_uom_unit_to")
    )
    private UomGroupUnit toUnit;

}
