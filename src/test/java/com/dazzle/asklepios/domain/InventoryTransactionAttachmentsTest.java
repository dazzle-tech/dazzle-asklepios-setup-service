package com.dazzle.asklepios.domain;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class InventoryTransactionAttachmentsTest {

    @Test
    void testBuilderAndGetters() {
        Instant now = Instant.now();

        InventoryTransactionAttachments att = InventoryTransactionAttachments.builder()
                .id(9001L)
                .transactionId(123L)
                .spaceKey("space-abc")
                .filename("report.pdf")
                .mimeType("application/pdf")
                .sizeBytes(42_000L)
                .deletedAt(now)
                .build();

        assertThat(att.getId()).isEqualTo(9001L);
        assertThat(att.getTransactionId()).isEqualTo(123L);
        assertThat(att.getSpaceKey()).isEqualTo("space-abc");
        assertThat(att.getFilename()).isEqualTo("report.pdf");
        assertThat(att.getMimeType()).isEqualTo("application/pdf");
        assertThat(att.getSizeBytes()).isEqualTo(42_000L);
        assertThat(att.getDeletedAt()).isEqualTo(now);
    }

    @Test
    void testEqualsAndHashCode() {
        InventoryTransactionAttachments a1 = InventoryTransactionAttachments.builder()
                .id(7001L)
                .transactionId(1L)
                .spaceKey("k")
                .filename("a.txt")
                .mimeType("text/plain")
                .sizeBytes(1L)
                .build();

        InventoryTransactionAttachments a2 = InventoryTransactionAttachments.builder()
                .id(7001L) // same id → should be equal
                .transactionId(1L)
                .spaceKey("k")
                .filename("a.txt")
                .mimeType("text/plain")
                .sizeBytes(1L)
                .build();

        assertThat(a1).isEqualTo(a2);
        assertThat(a1.hashCode()).isEqualTo(a2.hashCode());
    }

    @Test
    void testSerialization() throws Exception {
        InventoryTransactionAttachments att = InventoryTransactionAttachments.builder()
                .id(8002L)
                .transactionId(55L)
                .spaceKey("sp")
                .filename("img.png")
                .mimeType("image/png")
                .sizeBytes(2048L)
                .build();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(att);

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bis);
        InventoryTransactionAttachments copy = (InventoryTransactionAttachments) in.readObject();

        assertThat(copy.getFilename()).isEqualTo("img.png");
        assertThat(copy.getMimeType()).isEqualTo("image/png");
        assertThat(copy.getSizeBytes()).isEqualTo(2048L);
    }
}
