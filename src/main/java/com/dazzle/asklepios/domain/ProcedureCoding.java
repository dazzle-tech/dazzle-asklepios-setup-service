package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.MedicalCodeType;
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
@Table(name = "procedure_coding")
public class ProcedureCoding extends AbstractAuditingEntity<Long> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Owning side FK -> procedure(id) */
    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "procedure_id", nullable = false)
    private Procedure procedure;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "code_type", nullable = false, length = 100)
    private MedicalCodeType codeType;

    @NotNull
    @Column(name = "code_id", nullable = false, length = 100)
    private String codeId;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
