package com.dazzle.asklepios.service;

import com.dazzle.asklepios.attachments.AttachmentProperties;
import com.dazzle.asklepios.domain.EncounterAttachments;
import com.dazzle.asklepios.domain.enumeration.EncounterAttachmentSource;
import com.dazzle.asklepios.repository.EncounterAttachementsRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.attachment.encounter.UploadEncounterAttachmentVM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EncounterAttachmentsServiceTest {

    @Mock
    private EncounterAttachementsRepository repo;

    @Mock
    private AttachmentProperties props;

    @Mock
    private AttachmentStorageService storage;

    @InjectMocks
    private EncounterAttachmentsService service;

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

        UploadEncounterAttachmentVM vm = mock(UploadEncounterAttachmentVM.class);
        when(vm.files()).thenReturn(List.of(file));
        when(vm.type()).thenReturn("3154545");
        when(vm.details()).thenReturn("scan");
        when(vm.source()).thenReturn(EncounterAttachmentSource.CONSULTATION_ORDER_ATTACHMENT);

        ArgumentCaptor<EncounterAttachments> cap = ArgumentCaptor.forClass(EncounterAttachments.class);
        when(repo.save(cap.capture())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.upload(55L, vm);

        assertThat(result).hasSize(1);
        EncounterAttachments saved = cap.getValue();
        assertThat(saved.getEncounterId()).isEqualTo(55L);
        assertThat(saved.getFilename()).isEqualTo("x y__.png"); // sanitized
        assertThat(saved.getMimeType()).isEqualTo("image/png");
        assertThat(saved.getSizeBytes()).isEqualTo(1024L);
        assertThat(saved.getType()).isEqualTo("3154545");
        assertThat(saved.getDetails()).isEqualTo("scan");
        assertThat(saved.getSource()).isEqualTo(EncounterAttachmentSource.CONSULTATION_ORDER_ATTACHMENT);

        verify(storage).put(startsWith("encounters/55/"), eq("image/png"), eq(1024L), any());
        verify(storage, atLeastOnce()).head(anyString());
        verify(repo).save(any(EncounterAttachments.class));
    }

    @Test
    void upload_UnsupportedType() {
        when(props.getAllowed()).thenReturn(Set.of("application/pdf"));
        when(props.getMaxBytes()).thenReturn(10_000L);

        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("web");
        when(file.getSize()).thenReturn(100L);
        when(file.getOriginalFilename()).thenReturn("a.png");

        UploadEncounterAttachmentVM vm = mock(UploadEncounterAttachmentVM.class);
        when(vm.files()).thenReturn(List.of(file));
        when(vm.type()).thenReturn("3154545");
        when(vm.details()).thenReturn(null);
        when(vm.source()).thenReturn(EncounterAttachmentSource.CONSULTATION_ORDER_ATTACHMENT);

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

        UploadEncounterAttachmentVM vm = mock(UploadEncounterAttachmentVM.class);
        when(vm.files()).thenReturn(List.of(file));
        when(vm.type()).thenReturn("3154545");
        when(vm.details()).thenReturn(null);
        when(vm.source()).thenReturn(EncounterAttachmentSource.CONSULTATION_ORDER_ATTACHMENT);

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

        UploadEncounterAttachmentVM vm = mock(UploadEncounterAttachmentVM.class);
        when(vm.files()).thenReturn(List.of(file));
        when(vm.type()).thenReturn("3154545");
        when(vm.details()).thenReturn(null);
        when(vm.source()).thenReturn(EncounterAttachmentSource.CONSULTATION_ORDER_ATTACHMENT);

        assertThrows(BadRequestAlertException.class, () -> service.upload(1L, vm));
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

        UploadEncounterAttachmentVM vm = mock(UploadEncounterAttachmentVM.class);
        when(vm.files()).thenReturn(List.of(file));
        when(vm.type()).thenReturn("3154545");
        when(vm.details()).thenReturn(null);
        when(vm.source()).thenReturn(EncounterAttachmentSource.CONSULTATION_ORDER_ATTACHMENT);

        assertThrows(BadRequestAlertException.class, () -> service.upload(1L, vm));
        verify(repo, never()).save(any());
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

        UploadEncounterAttachmentVM vm = mock(UploadEncounterAttachmentVM.class);
        when(vm.files()).thenReturn(List.of(file));
        when(vm.type()).thenReturn("3154545");
        when(vm.details()).thenReturn(null);
        when(vm.source()).thenReturn(EncounterAttachmentSource.CONSULTATION_ORDER_ATTACHMENT);

        assertThrows(BadRequestAlertException.class, () -> service.upload(1L, vm));
        verify(repo, never()).save(any());
    }

    @Test
    void list_ReturnsFromRepo() {
        when(repo.findByEncounterIdInAndDeletedAtIsNullOrderByCreatedDateDesc(Collections.singletonList(77L)))
                .thenReturn(List.of(new EncounterAttachments()));

        var out = service.list(Collections.singletonList(77L));

        assertThat(out).hasSize(1);
        verify(repo).findByEncounterIdInAndDeletedAtIsNullOrderByCreatedDateDesc(Collections.singletonList(77L));
    }

    @Test
    void listByEncounterIdAndSource_ReturnsFromRepo() {
        when(repo.findByEncounterIdAndSourceAndDeletedAtIsNullOrderByCreatedDateDesc(77L, EncounterAttachmentSource.CONSULTATION_ORDER_ATTACHMENT))
                .thenReturn(List.of());

        var out = service.listByEncounterIdAndSource(77L, EncounterAttachmentSource.CONSULTATION_ORDER_ATTACHMENT);

        assertThat(out).isEmpty();
        verify(repo).findByEncounterIdAndSourceAndDeletedAtIsNullOrderByCreatedDateDesc(77L, EncounterAttachmentSource.CONSULTATION_ORDER_ATTACHMENT);
    }


    @Test
    void downloadUrl_ReturnsTicket() throws Exception {
        EncounterAttachments entity = EncounterAttachments.builder()
                .id(10L).encounterId(1L).spaceKey("k").filename("f.txt")
                .mimeType("text/plain").sizeBytes(1L)
                .source(EncounterAttachmentSource.CONSULTATION_ORDER_ATTACHMENT).build();

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
        EncounterAttachments entity = new EncounterAttachments();
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
        EncounterAttachments entity = new EncounterAttachments();
        entity.setId(6L);
        entity.setDeletedAt(Instant.now());
        when(repo.findById(6L)).thenReturn(Optional.of(entity));
        service.softDelete(6L);
        verify(repo, never()).save(any());
    }
}
