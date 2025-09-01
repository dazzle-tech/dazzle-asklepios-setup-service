package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.Uom;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "uom_groups_unit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UomGroupUnit implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", nullable = false)
    @EqualsAndHashCode.Include
    private Long id;

    @Enumerated(EnumType.STRING)
    private Uom uom;

    @Column(name = "uom_order")
    private Integer uomOrder;

    @Column(name = "uom_group_key")
    private String uomGroupKey;

}
