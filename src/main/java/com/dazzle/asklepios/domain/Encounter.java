package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.Resource;
import com.dazzle.asklepios.domain.enumeration.Status;
import com.dazzle.asklepios.domain.enumeration.Visit;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Encounter extends AbstractAuditingEntity<Long> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;


    @EqualsAndHashCode.Include
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "patient_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_patient_encounter")
    )
    private Patient patient;


    @Column(name = "age", length = 50)
    private String age;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", length = 80)
    private Resource resource;

    @Enumerated(EnumType.STRING)
    @Column(name = "visit_type", length = 80)
    private Visit visit;
}
