package com.dazzle.asklepios.domain;

import jakarta.persistence.*;
        import jakarta.validation.constraints.NotNull;
import lombok.*;

        import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "uom_group")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UomGroup implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", nullable = false)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "description")
    private String description;

    @Column(name = "name")
    private String name;

    @Column(name = "code")
    private String code;

}
