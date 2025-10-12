package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.ServiceItemsType;
import com.dazzle.asklepios.domain.enumeration.ServiceItemsType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "service_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ServiceItems implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ServiceItemsType type;   // enum for type field

    @NotNull
    @Column(name = "source_id", nullable = false)
    private Long sourceId;

    @Column(name = "service_id")
    private Long serviceId;

    @NotNull
    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;

    @Column(name = "created_date")
    private Instant createdDate;

    @Column(name = "last_modified_by", length = 50)
    private String lastModifiedBy;

    @Column(name = "last_modified_date")
    private Instant lastModifiedDate;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
