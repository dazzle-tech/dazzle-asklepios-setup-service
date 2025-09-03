package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PrescriptionInstruction implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_lkey")
    private AgeGroups category;

    @Column(name = "dose", precision = 7, scale = 2)
    private BigDecimal dose;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit")
    private Uom unit;

    @Enumerated(EnumType.STRING)
    @Column(name = "rout")
    private MedRoa rout;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency")
    private MedFrequency frequency;

}
