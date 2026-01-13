package com.dazzle.asklepios.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "favorite_diagnostic_test"
)
@EqualsAndHashCode(callSuper = false)
public class FavoriteDiagnosticTest extends AbstractAuditingEntity<Long>
        implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotNull
    @Column(name = "test_id", nullable = false)
    private Long testId;
}
