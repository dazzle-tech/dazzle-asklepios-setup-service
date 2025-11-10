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
    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private AgeGroupType category;

    @NotNull
    @Column(name = "dose")
    private BigDecimal dose;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "unit")
    private UOM unit;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "rout")
    private MedRoa rout;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "frequency")
    private MedFrequency frequency;

}
