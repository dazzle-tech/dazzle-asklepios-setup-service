package com.dazzle.asklepios.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "patient_attachments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_pa_space_key", columnNames = {"space_key"}),
                @UniqueConstraint(name = "uq_pa_patient_space", columnNames = {"patient_id", "space_key"})
        },
        indexes = {
                @Index(name = "idx_pa_patient", columnList = "patient_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientAttachments {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                       // app-assigned

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;

    @Column(name = "space_key", nullable = false, columnDefinition = "text")
    private String spaceKey;

    @Column(name = "filename", nullable = false, columnDefinition = "text")
    private String filename;

    @Column(name = "mime_type", nullable = false, columnDefinition = "text")
    private String mimeType;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "type", length = 100)
    private String type;

    @Column(name = "details", columnDefinition = "text")
    private String details;
    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }
}

