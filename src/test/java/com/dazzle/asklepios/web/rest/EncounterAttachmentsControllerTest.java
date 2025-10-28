package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.config.TestSecurityConfig;
import com.dazzle.asklepios.domain.EncounterAttachments;
import com.dazzle.asklepios.domain.enumeration.EncounterAttachmentSource;
import com.dazzle.asklepios.repository.EncounterAttachementsRepository;
import com.dazzle.asklepios.service.AttachmentStorageService;
import com.dazzle.asklepios.service.EncounterAttachmentsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URL;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EncounterAttachmentsController.class)
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc
class EncounterAttachmentsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EncounterAttachmentsService service;

    @MockitoBean
    private EncounterAttachementsRepository repo;

    @MockitoBean
    private AttachmentStorageService storage;

    @Test
    void upload_returnsResponseVMsWithPresignedUrls() throws Exception {
        // given saved entity returned by service
        var entity = EncounterAttachments.builder()
                .id(11L)
                .encounterId(55L)
                .spaceKey("encounters/55/2025/10/UUID_file.png")
                .filename("file.png")
                .mimeType("image/png")
                .sizeBytes(123L)
                .type("3154545")
                .details("scan")
                .source(EncounterAttachmentSource.NURSE_STATION_ATTACHMENT)
                .build();

        when(service.upload(eq(55L), any())).thenReturn(List.of(entity));

        // presign on controller mapping: uses key and the safe filename from key's tail
        PresignedGetObjectRequest pre = org.mockito.Mockito.mock(PresignedGetObjectRequest.class);
        when(pre.url()).thenReturn(new URL("https://files.example/encounters/55/2025/10/UUID_file.png?sig=1"));
        when(storage.presignGet("encounters/55/2025/10/UUID_file.png", "UUID_file.png")).thenReturn(pre);

        MockMultipartFile mf = new MockMultipartFile("files", "file.png", "image/png", new byte[]{1,2,3});

        mockMvc.perform(multipart("/api/setup/encounters/{encounterId}/attachments", 55L)
                        .file(mf)
                        .param("type", "3154545")
                        .param("details", "scan")
                        .param("source", "NURSE_STATION_ATTACHMENT")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].filename").value("file.png"))
                .andExpect(jsonPath("$[0].downloadUrl").value("https://files.example/encounters/55/2025/10/UUID_file.png?sig=1"));
    }

    @Test
    void list_byEncounterAndSource_returnsEntities() throws Exception {
        var e = new EncounterAttachments();
        e.setId(5L);
        e.setEncounterId(77L);
        e.setFilename("a.pdf");
        when(service.listByEncounterIdAndSource(77L, EncounterAttachmentSource.NURSE_STATION_ATTACHMENT))
                .thenReturn(List.of(e));

        mockMvc.perform(get("/api/setup/encounters/attachments/by-encounterIdAndSource/{encounterId}/{source}", 77L, "NURSE_STATION_ATTACHMENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5L))
                .andExpect(jsonPath("$[0].filename").value("a.pdf"));
    }

    @Test
    void downloadUrl_returnsTicket() throws Exception {
        var ticket = new EncounterAttachmentsService.DownloadTicket("https://asklepios.sfo3.digitaloceanspaces.com/encounters/1/2025/10/fcc251ac-b0cd-4dbb-af05-362598ce3df1_patient%20_3_.png?response-content-disposition=attachment%3B%20filename%3D%22patient%20_3_.png%22&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20251027T114008Z&X-Amz-SignedHeaders=host&X-Amz-Credential=DO00TRNC7U4TLLL6ZAN2%2F20251027%2Fsfo3%2Fs3%2Faws4_request&X-Amz-Expires=300&X-Amz-Signature=2b209fc00c0098c0b5d0da1eb2e0c076f7e28a638473d0e41fcb9ef15c7d0dcb", 300);
        when(service.downloadUrl(5003L)).thenReturn(ticket);

        mockMvc.perform(post("/api/setup/encounters/attachmentDownloadUrl/{id}", 5003L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://asklepios.sfo3.digitaloceanspaces.com/encounters/1/2025/10/fcc251ac-b0cd-4dbb-af05-362598ce3df1_patient%20_3_.png?response-content-disposition=attachment%3B%20filename%3D%22patient%20_3_.png%22&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20251027T114008Z&X-Amz-SignedHeaders=host&X-Amz-Credential=DO00TRNC7U4TLLL6ZAN2%2F20251027%2Fsfo3%2Fs3%2Faws4_request&X-Amz-Expires=300&X-Amz-Signature=2b209fc00c0098c0b5d0da1eb2e0c076f7e28a638473d0e41fcb9ef15c7d0dcb"))
                .andExpect(jsonPath("$.expiresInSeconds").value(300));
    }

    @Test
    void delete_softDeletes() throws Exception {
        mockMvc.perform(delete("/api/setup/encounters/attachments/{id}", 9L))
                .andExpect(status().isOk());
        verify(service).softDelete(9L);
    }

    @Test
    void updateTypeAndDetails_updatesAndReturnsEntity() throws Exception {
        var existing = new EncounterAttachments();
        existing.setId(15L);
        existing.setType("3154545");
        existing.setDetails("OLD");
        when(repo.findById(15L)).thenReturn(Optional.of(existing));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(put("/api/setup/encounters/attachments/{id}", 15L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"type":"6541561","details":"some details"}
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("6541561"))
                .andExpect(jsonPath("$.details").value("some details"));
    }

    @Test
    void updateTypeAndDetails_notFound_returnsBadRequest() throws Exception {
        when(repo.findById(404L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/setup/encounters/attachments/{id}", 404L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"type":"ANY","details":"ANY"}
                    """))
                .andExpect(status().isBadRequest());

        verify(repo, never()).save(any());
    }
}
