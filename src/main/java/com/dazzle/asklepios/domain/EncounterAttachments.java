package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.EncounterAttachmentSource;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "encounter_attachments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class EncounterAttachments extends AbstractAuditingEntity<Long> implements Serializable {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "encounter_id", nullable = false)
    private Long encounterId;

    @Column(name = "space_key", nullable = false)
    private String spaceKey;

    @Column(name = "filename", nullable = false, columnDefinition = "text")
    private String filename;

    @Column(name = "mime_type", nullable = false, columnDefinition = "text")
    private String mimeType;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "type", length = 100)
    private String type;

    @Column(name = "details", columnDefinition = "text")
    private String details;

    @Column(name = "source", length = 50)
    @Enumerated(EnumType.STRING)
    private EncounterAttachmentSource source;

}
