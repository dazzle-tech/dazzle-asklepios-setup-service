package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.config.TestSecurityConfig;
import com.dazzle.asklepios.domain.PatientAttachments;
import com.dazzle.asklepios.domain.enumeration.PatientAttachmentSource;
import com.dazzle.asklepios.repository.PatientAttachmentsRepository;
import com.dazzle.asklepios.service.AttachmentStorageService;
import com.dazzle.asklepios.service.PatientAttachmentsService;
import com.dazzle.asklepios.web.rest.vm.attachment.patient.DownloadPatientAttachmentVM;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PatientAttachmentsController.class)
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc
class PatientAttachmentsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PatientAttachmentsService service;

    @MockitoBean
    private PatientAttachmentsRepository repo;

    @MockitoBean
    private AttachmentStorageService storage;

    @Test
    void upload_returnsResponseVMWithPresignedUrl() throws Exception {
        // given saved entity returned by service
        var entity = PatientAttachments.builder()
                .id(11L)
                .patientId(55L)
                .spaceKey("patients/55/2025/10/UUID_file.png")
                .filename("file.png")
                .mimeType("image/png")
                .sizeBytes(123L)
                .type("3154545")
                .details("scan")
                .source(PatientAttachmentSource.PATIENT_PROFILE_ATTACHMENT)
                .build();

        when(service.upload(eq(55L), any())).thenReturn(entity);

        // presign on controller mapping: uses key and the safe filename from key's tail
        PresignedGetObjectRequest pre = org.mockito.Mockito.mock(PresignedGetObjectRequest.class);
        when(pre.url()).thenReturn(new URL("https://files.example/patients/55/2025/10/UUID_file.png?sig=1"));
        when(storage.presignGet("patients/55/2025/10/UUID_file.png", "UUID_file.png")).thenReturn(pre);


        MockMultipartFile mf = new MockMultipartFile("file", "file.png", "image/png", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/setup/patients/{patientId}/attachment", 55L)
                        .file(new MockMultipartFile("file","file.png","image/png", new byte[]{1,2,3}))
                        .param("type","3154545")
                        .param("details","scan")
                        .param("source","PATIENT_PROFILE_ATTACHMENT")
                        .contentType(MediaType.MULTIPART_FORM_DATA));
    }


    @Test
    void downloadUrl_returnsTicket() throws Exception {
        var ticket = new DownloadPatientAttachmentVM("https://asklepios.sfo3.digitaloceanspaces.com/patients/1/2025/10/fcc251ac-b0cd-4dbb-af05-362598ce3df1_patient%20_3_.png?response-content-disposition=attachment%3B%20filename%3D%22patient%20_3_.png%22&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20251027T114008Z&X-Amz-SignedHeaders=host&X-Amz-Credential=DO00TRNC7U4TLLL6ZAN2%2F20251027%2Fsfo3%2Fs3%2Faws4_request&X-Amz-Expires=300&X-Amz-Signature=2b209fc00c0098c0b5d0da1eb2e0c076f7e28a638473d0e41fcb9ef15c7d0dcb", 300);
        when(service.downloadUrl(5003L)).thenReturn(ticket);

        mockMvc.perform(post("/api/setup/patients/attachmentDownloadUrl/{id}", 5003L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://asklepios.sfo3.digitaloceanspaces.com/patients/1/2025/10/fcc251ac-b0cd-4dbb-af05-362598ce3df1_patient%20_3_.png?response-content-disposition=attachment%3B%20filename%3D%22patient%20_3_.png%22&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20251027T114008Z&X-Amz-SignedHeaders=host&X-Amz-Credential=DO00TRNC7U4TLLL6ZAN2%2F20251027%2Fsfo3%2Fs3%2Faws4_request&X-Amz-Expires=300&X-Amz-Signature=2b209fc00c0098c0b5d0da1eb2e0c076f7e28a638473d0e41fcb9ef15c7d0dcb"))
                .andExpect(jsonPath("$.expiresInSeconds").value(300));
    }

    @Test
    void delete_softDeletes() throws Exception {
        mockMvc.perform(delete("/api/setup/patients/attachments/{id}", 9L))
                .andExpect(status().isOk());
        verify(service).softDelete(9L);
    }

    @Test
    void updateTypeAndDetails_updatesAndReturnsEntity() throws Exception {
        var existing = new PatientAttachments();
        existing.setId(15L);
        existing.setType("3154545");
        existing.setDetails("OLD");
        when(repo.findById(15L)).thenReturn(Optional.of(existing));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(put("/api/setup/patients/attachments/{id}", 15L)
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

        mockMvc.perform(put("/api/setup/patients/attachments/{id}", 404L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"type":"ANY","details":"ANY"}
                    """))
                .andExpect(status().isBadRequest());

        verify(repo, never()).save(any());
    }
}
