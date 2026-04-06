package com.dazzle.asklepios.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "bed_room_services")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BedRoomService extends AbstractAuditingEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceSetup service;

    @NotNull
    @Column(name = "bed_specific", nullable = false)
    private Boolean bedSpecific = false;

    @ManyToOne
    @JoinColumn(name = "bed_id")
    private Bed bed;

    @Column(name = "rule", columnDefinition = "text")
    private String rule;

    @Builder.Default
    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}