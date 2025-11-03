package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.config.TestSecurityConfig;
import com.dazzle.asklepios.domain.InventoryTransactionAttachments;
import com.dazzle.asklepios.repository.InventoryTransactionAttachmentsRepository;
import com.dazzle.asklepios.service.AttachmentStorageService;
import com.dazzle.asklepios.service.InventoryTransactionAttachmentsService;
import com.dazzle.asklepios.web.rest.vm.attachment.inventoryTransaction.DownloadInventoryTransactionAttachmentVM;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InventoryTransactionAttachmentsController.class)
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc
class InventoryTransactionAttachmentsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InventoryTransactionAttachmentsService service;

    @MockitoBean
    private InventoryTransactionAttachmentsRepository repo;

    @MockitoBean
    private AttachmentStorageService storage;

    @Test
    void upload_returnsResponseVMWithPresignedUrl() throws Exception {
        // given saved entity returned by service
        var entity = InventoryTransactionAttachments.builder()
                .id(11L)
                .transactionId(55L)
                .spaceKey("inventoryTransaction/55/2025/10/UUID_file.png")
                .filename("file.png")
                .mimeType("image/png")
                .sizeBytes(123L)
                .build();

        when(service.upload(eq(55L), any())).thenReturn(entity);

        // presign on controller mapping: uses key and the safe filename from key's tail
        PresignedGetObjectRequest pre = org.mockito.Mockito.mock(PresignedGetObjectRequest.class);
        when(pre.url()).thenReturn(new URL("https://files.example/inventoryTransaction/55/2025/10/UUID_file.png?sig=1"));
        when(storage.presignGet("inventoryTransaction/55/2025/10/UUID_file.png", "UUID_file.png")).thenReturn(pre);

        MockMultipartFile mf = new MockMultipartFile("file", "file.png", "image/png", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/setup/inventoryTransaction/{transactionId}/attachment", 55L)
                        .file(new MockMultipartFile("file","file.png","image/png", new byte[]{1,2,3}))
                        .contentType(MediaType.MULTIPART_FORM_DATA));
    }


    @Test
    void downloadUrl_returnsTicket() throws Exception {
        var ticket = new DownloadInventoryTransactionAttachmentVM("https://asklepios.sfo3.digitaloceanspaces.com/inventoryTransaction/1/2025/10/fcc251ac-b0cd-4dbb-af05-362598ce3df1_patient%20_3_.png?response-content-disposition=attachment%3B%20filename%3D%22patient%20_3_.png%22&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20251027T114008Z&X-Amz-SignedHeaders=host&X-Amz-Credential=DO00TRNC7U4TLLL6ZAN2%2F20251027%2Fsfo3%2Fs3%2Faws4_request&X-Amz-Expires=300&X-Amz-Signature=2b209fc00c0098c0b5d0da1eb2e0c076f7e28a638473d0e41fcb9ef15c7d0dcb", 300);
        when(service.downloadUrl(5003L)).thenReturn(ticket);

        mockMvc.perform(post("/api/setup/inventoryTransaction/attachmentDownloadUrl/{id}", 5003L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://asklepios.sfo3.digitaloceanspaces.com/inventoryTransaction/1/2025/10/fcc251ac-b0cd-4dbb-af05-362598ce3df1_patient%20_3_.png?response-content-disposition=attachment%3B%20filename%3D%22patient%20_3_.png%22&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20251027T114008Z&X-Amz-SignedHeaders=host&X-Amz-Credential=DO00TRNC7U4TLLL6ZAN2%2F20251027%2Fsfo3%2Fs3%2Faws4_request&X-Amz-Expires=300&X-Amz-Signature=2b209fc00c0098c0b5d0da1eb2e0c076f7e28a638473d0e41fcb9ef15c7d0dcb"))
                .andExpect(jsonPath("$.expiresInSeconds").value(300));
    }

    @Test
    void delete_softDeletes() throws Exception {
        mockMvc.perform(delete("/api/setup/inventoryTransaction/attachments/{id}", 9L))
                .andExpect(status().isOk());
        verify(service).softDelete(9L);
    }


}
