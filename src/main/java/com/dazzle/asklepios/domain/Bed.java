package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.BedStatus;
import com.dazzle.asklepios.domain.enumeration.BedType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "beds")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bed extends AbstractAuditingEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @NotNull
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "location_details", columnDefinition = "text")
    private String locationDetails;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private BedType type;

    @Builder.Default
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private BedStatus status = BedStatus.EMPTY;

    @Builder.Default
    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}