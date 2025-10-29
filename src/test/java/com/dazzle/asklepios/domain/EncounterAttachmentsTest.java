package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.EncounterAttachmentSource;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class EncounterAttachmentsTest {

    @Test
    void testBuilderAndGetters() {
        Instant now = Instant.now();

        EncounterAttachments att = EncounterAttachments.builder()
                .id(9001L)
                .encounterId(123L)
                .spaceKey("space-abc")
                .filename("report.pdf")
                .mimeType("application/pdf")
                .sizeBytes(42_000L)
                .deletedAt(now)
                .type("3154545")
                .details("{\"note\":\"ok\"}")
                .source(EncounterAttachmentSource.values()[0])
                .sourceId(734845)
                .build();

        assertThat(att.getId()).isEqualTo(9001L);
        assertThat(att.getEncounterId()).isEqualTo(123L);
        assertThat(att.getSpaceKey()).isEqualTo("space-abc");
        assertThat(att.getFilename()).isEqualTo("report.pdf");
        assertThat(att.getMimeType()).isEqualTo("application/pdf");
        assertThat(att.getSizeBytes()).isEqualTo(42_000L);
        assertThat(att.getDeletedAt()).isEqualTo(now);
        assertThat(att.getType()).isEqualTo("3154545");
        assertThat(att.getDetails()).isEqualTo("{\"note\":\"ok\"}");
        assertThat(att.getSource()).isNotNull();
        assertThat(att.getSourceId()).isEqualTo(734845L);
    }

    @Test
    void testEqualsAndHashCode() {
        EncounterAttachments a1 = EncounterAttachments.builder()
                .id(7001L)
                .encounterId(1L)
                .spaceKey("k")
                .filename("a.txt")
                .mimeType("text/plain")
                .sizeBytes(1L)
                .source(EncounterAttachmentSource.values()[0])
                .sourceId(734845)
                .build();

        EncounterAttachments a2 = EncounterAttachments.builder()
                .id(7001L) // same id → should be equal
                .encounterId(1L)
                .spaceKey("k")
                .filename("a.txt")
                .mimeType("text/plain")
                .sizeBytes(1L)
                .source(EncounterAttachmentSource.values()[0])
                .sourceId(734845)
                .build();

        assertThat(a1).isEqualTo(a2);
        assertThat(a1.hashCode()).isEqualTo(a2.hashCode());
    }

    @Test
    void testSerialization() throws Exception {
        EncounterAttachments att = EncounterAttachments.builder()
                .id(8002L)
                .encounterId(55L)
                .spaceKey("sp")
                .filename("img.png")
                .mimeType("image/png")
                .sizeBytes(2048L)
                .type("3154545")
                .details("scan")
                .source(EncounterAttachmentSource.values()[0])
                .sourceId(734845)
                .build();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(att);

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bis);
        EncounterAttachments copy = (EncounterAttachments) in.readObject();

        assertThat(copy.getFilename()).isEqualTo("img.png");
        assertThat(copy.getMimeType()).isEqualTo("image/png");
        assertThat(copy.getSizeBytes()).isEqualTo(2048L);
        assertThat(copy.getType()).isEqualTo("3154545");
        assertThat(copy.getDetails()).isEqualTo("scan");
        assertThat(copy.getSource()).isNotNull();
        assertThat(copy.getSourceId()).isEqualTo(734845L);
    }
}
