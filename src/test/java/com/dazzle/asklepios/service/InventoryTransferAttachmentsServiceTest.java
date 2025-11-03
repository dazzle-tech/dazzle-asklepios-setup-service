package com.dazzle.asklepios.service;

import com.dazzle.asklepios.attachments.AttachmentProperties;
import com.dazzle.asklepios.domain.InventoryTransferAttachments;
import com.dazzle.asklepios.repository.InventoryTransferAttachmentsRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.attachment.inventoryTransfer.UploadInventoryTransferAttachmentVM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InventoryTransferAttachmentsServiceTest {

    @Mock
    private InventoryTransferAttachmentsRepository repo;

    @Mock
    private AttachmentProperties props;

    @Mock
    private AttachmentStorageService storage;

    @InjectMocks
    private InventoryTransferAttachmentsService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void upload_Success() throws Exception {
        when(props.getAllowed()).thenReturn(Set.of("image/png"));
        when(props.getMaxBytes()).thenReturn(10_000L);

        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("image/png");
        when(file.getSize()).thenReturn(1024L);
        when(file.getOriginalFilename()).thenReturn("x y@#.png");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{1}));

        var head = mock(software.amazon.awssdk.services.s3.model.HeadObjectResponse.class);
        when(head.contentLength()).thenReturn(1024L);
        when(head.contentType()).thenReturn("image/png");
        when(storage.head(anyString())).thenReturn(head);

        UploadInventoryTransferAttachmentVM vm = mock(UploadInventoryTransferAttachmentVM.class);
        when(vm.file()).thenReturn(file);

        ArgumentCaptor<InventoryTransferAttachments> cap = ArgumentCaptor.forClass(InventoryTransferAttachments.class);
        when(repo.save(cap.capture())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.upload(55L, vm);

        InventoryTransferAttachments saved = cap.getValue();
        assertThat(saved.getTransactionId()).isEqualTo(55L);
        assertThat(saved.getFilename()).isEqualTo("x y__.png"); // sanitized
        assertThat(saved.getMimeType()).isEqualTo("image/png");
        assertThat(saved.getSizeBytes()).isEqualTo(1024L);

        verify(storage).put(
                startsWith("inventoryTransfer/55/"),
                eq("image/png"),
                eq(1024L),
                any(InputStream.class)
        );
    }

    @Test
    void upload_UnsupportedType() {
        when(props.getAllowed()).thenReturn(Set.of("application/pdf"));
        when(props.getMaxBytes()).thenReturn(10_000L);

        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("web");
        when(file.getSize()).thenReturn(100L);
        when(file.getOriginalFilename()).thenReturn("a.png");

        UploadInventoryTransferAttachmentVM vm = mock(UploadInventoryTransferAttachmentVM.class);
        when(vm.file()).thenReturn(file);
        assertThrows(BadRequestAlertException.class, () -> service.upload(1L, vm));
        verify(storage, never()).put(anyString(), anyString(), anyLong(), any());
        verify(repo, never()).save(any());
    }

    @Test
    void upload_TooLarge() {
        when(props.getAllowed()).thenReturn(Set.of("image/png"));
        when(props.getMaxBytes()).thenReturn(500L);

        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("image/png");
        when(file.getSize()).thenReturn(1_000000L);
        when(file.getOriginalFilename()).thenReturn("a.png");

        UploadInventoryTransferAttachmentVM vm = mock(UploadInventoryTransferAttachmentVM.class);
        when(vm.file()).thenReturn(file);

        assertThrows(BadRequestAlertException.class, () -> service.upload(1L, vm));
        verify(storage, never()).put(anyString(), anyString(), anyLong(), any());
        verify(repo, never()).save(any());
    }

    @Test
    void upload_UploadFailedOnPut() throws Exception {
        when(props.getAllowed()).thenReturn(Set.of("image/png"));
        when(props.getMaxBytes()).thenReturn(10_000L);

        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("image/png");
        when(file.getSize()).thenReturn(100L);
        when(file.getOriginalFilename()).thenReturn("a.png");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{1}));

        doThrow(new RuntimeException("io")).when(storage).put(anyString(), anyString(), anyLong(), any());

        UploadInventoryTransferAttachmentVM vm = mock(UploadInventoryTransferAttachmentVM.class);
        when(vm.file()).thenReturn(file);


        verify(repo, never()).save(any());
    }

    @Test
    void upload_SizeMismatch() throws Exception {
        when(props.getAllowed()).thenReturn(Set.of("image/png"));
        when(props.getMaxBytes()).thenReturn(10_000L);

        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("image/png");
        when(file.getSize()).thenReturn(100L);
        when(file.getOriginalFilename()).thenReturn("a.png");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{1}));

        var head = mock(software.amazon.awssdk.services.s3.model.HeadObjectResponse.class);
        when(head.contentLength()).thenReturn(50L);
        when(head.contentType()).thenReturn("image/png");
        when(storage.head(anyString())).thenReturn(head);

        UploadInventoryTransferAttachmentVM vm = mock(UploadInventoryTransferAttachmentVM.class);
        when(vm.file()).thenReturn(file);

    }

    @Test
    void upload_TypeMismatch() throws Exception {
        when(props.getAllowed()).thenReturn(Set.of("image/png"));
        when(props.getMaxBytes()).thenReturn(10_000L);

        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("image/png");
        when(file.getSize()).thenReturn(100L);
        when(file.getOriginalFilename()).thenReturn("a.png");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{1}));

        var head = mock(software.amazon.awssdk.services.s3.model.HeadObjectResponse.class);
        when(head.contentLength()).thenReturn(100L);
        when(head.contentType()).thenReturn("application/octet-stream");
        when(storage.head(anyString())).thenReturn(head);

        UploadInventoryTransferAttachmentVM vm = mock(UploadInventoryTransferAttachmentVM.class);
        when(vm.file()).thenReturn(file);

    }

    @Test
    void list_ReturnsFromRepo() {
        when(repo.findByTransactionIdAndDeletedAtIsNullOrderByCreatedDateDesc(77L))
                .thenReturn(List.of(new InventoryTransferAttachments()));

        var out = service.list(77L);

        assertThat(out).hasSize(1);
        verify(repo).findByTransactionIdAndDeletedAtIsNullOrderByCreatedDateDesc(77L);
    }


    @Test
    void downloadUrl_ReturnsTicket() throws Exception {
        InventoryTransferAttachments entity = InventoryTransferAttachments.builder()
                .id(10L).transactionId(1L).spaceKey("k").filename("f.txt")
                .mimeType("text/plain").sizeBytes(1L)
                .build();

        when(repo.findActiveById(10L)).thenReturn(Optional.of(entity));

        PresignedGetObjectRequest pre = mock(PresignedGetObjectRequest.class);
        when(pre.url()).thenReturn(new URL("https://files.example/k?sig=1"));
        when(storage.presignGet("k", "f.txt")).thenReturn(pre);
        when(props.getPresignExpirySeconds()).thenReturn(600);

        var ticket = service.downloadUrl(10L);

        assertThat(ticket.url()).isEqualTo("https://files.example/k?sig=1");
        assertThat(ticket.expiresInSeconds()).isEqualTo(600);
        verify(repo).findActiveById(10L);
        verify(storage).presignGet("k", "f.txt");
    }
    @Test
    void softDelete_SetsDeletedAtOnce() {
        InventoryTransferAttachments entity = new InventoryTransferAttachments();
        entity.setId(5L);
        entity.setDeletedAt(null);

        when(repo.findById(5L)).thenReturn(Optional.of(entity));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.softDelete(5L);

        assertThat(entity.getDeletedAt()).isNotNull();
        verify(repo).save(entity);
    }

    @Test
    void softDelete_NoOpIfAlreadyDeleted() {
        InventoryTransferAttachments entity = new InventoryTransferAttachments();
        entity.setId(6L);
        entity.setDeletedAt(Instant.now());
        when(repo.findById(6L)).thenReturn(Optional.of(entity));
        service.softDelete(6L);
        verify(repo, never()).save(any());
    }
}
