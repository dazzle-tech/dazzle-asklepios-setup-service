package com.dazzle.asklepios.domain;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Entity
@Table(name = "cdt_dental_action")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CdtDentalAction implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dental_action_id", foreignKey = @ForeignKey(name = "fk_cdt_dental_action"))
    private DentalAction dentalAction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cdt_code", referencedColumnName = "code", foreignKey = @ForeignKey(name = "fk_cdt_dental_action_code"))
    private CdtCode cdtCode;
}
