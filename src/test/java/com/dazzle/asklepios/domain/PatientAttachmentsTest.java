package com.dazzle.asklepios.domain;

import com.dazzle.asklepios.domain.enumeration.PatientAttachmentSource;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class PatientAttachmentsTest {

    @Test
    void testBuilderAndGetters() {
        Instant now = Instant.now();

        PatientAttachments att = PatientAttachments.builder()
                .id(9001L)
                .patientId(123L)
                .spaceKey("space-abc")
                .filename("report.pdf")
                .mimeType("application/pdf")
                .sizeBytes(42_000L)
                .deletedAt(now)
                .type("3154545")
                .details("{\"note\":\"ok\"}")
                .source(PatientAttachmentSource.values()[0])
                .build();

        assertThat(att.getId()).isEqualTo(9001L);
        assertThat(att.getPatientId()).isEqualTo(123L);
        assertThat(att.getSpaceKey()).isEqualTo("space-abc");
        assertThat(att.getFilename()).isEqualTo("report.pdf");
        assertThat(att.getMimeType()).isEqualTo("application/pdf");
        assertThat(att.getSizeBytes()).isEqualTo(42_000L);
        assertThat(att.getDeletedAt()).isEqualTo(now);
        assertThat(att.getType()).isEqualTo("3154545");
        assertThat(att.getDetails()).isEqualTo("{\"note\":\"ok\"}");
        assertThat(att.getSource()).isNotNull();
    }

    @Test
    void testEqualsAndHashCode() {
        PatientAttachments a1 = PatientAttachments.builder()
                .id(7001L)
                .patientId(1L)
                .spaceKey("k")
                .filename("a.txt")
                .mimeType("text/plain")
                .sizeBytes(1L)
                .source(PatientAttachmentSource.values()[0])
                .build();

        PatientAttachments a2 = PatientAttachments.builder()
                .id(7001L) // same id → should be equal
                .patientId(1L)
                .spaceKey("k")
                .filename("a.txt")
                .mimeType("text/plain")
                .sizeBytes(1L)
                .source(PatientAttachmentSource.values()[0])
                .build();

        assertThat(a1).isEqualTo(a2);
        assertThat(a1.hashCode()).isEqualTo(a2.hashCode());
    }

    @Test
    void testSerialization() throws Exception {
        PatientAttachments att = PatientAttachments.builder()
                .id(8002L)
                .patientId(55L)
                .spaceKey("sp")
                .filename("img.png")
                .mimeType("image/png")
                .sizeBytes(2048L)
                .type("3154545")
                .details("scan")
                .source(PatientAttachmentSource.values()[0])
                .build();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(att);

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bis);
        PatientAttachments copy = (PatientAttachments) in.readObject();

        assertThat(copy.getFilename()).isEqualTo("img.png");
        assertThat(copy.getMimeType()).isEqualTo("image/png");
        assertThat(copy.getSizeBytes()).isEqualTo(2048L);
        assertThat(copy.getType()).isEqualTo("3154545");
        assertThat(copy.getDetails()).isEqualTo("scan");
        assertThat(copy.getSource()).isNotNull();
    }
}
