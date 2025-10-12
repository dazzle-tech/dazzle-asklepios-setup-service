package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.config.TestSecurityConfig;
import com.dazzle.asklepios.domain.Service;
import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.ServiceCategory;
import com.dazzle.asklepios.service.ServiceService;
import com.dazzle.asklepios.web.rest.vm.ServiceResponseVM;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ServiceController.class)
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc
class ServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ServiceService serviceService;

    private Service sampleService() {
        return Service.builder()
                .id(100L)
                .name("MRI Scan")
                .abbreviation("MRI")
                .code("MRI-01")
                .category(ServiceCategory.CONSULTATION)
                .price(new BigDecimal("199.99"))
                .currency(Currency.USD)
                .isActive(true)
                .createdBy("tester")
                .build();
    }

    @Test
    void testGetAllServices_Paginated() throws Exception {
        var svc = sampleService();
        var pageable = PageRequest.of(0, 10);
        Page<ServiceResponseVM> page =
                new PageImpl<>(List.of(ServiceResponseVM.ofEntity(svc)), pageable, 1);

        when(serviceService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/setup/service")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$[0].name").value("MRI Scan"))
                .andExpect(jsonPath("$[0].code").value("MRI-01"));
    }

    @Test
    void testGetServiceById() throws Exception {
        var svc = sampleService();
        when(serviceService.findOne(100L)).thenReturn(Optional.of(svc));

        mockMvc.perform(get("/api/setup/service/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("MRI Scan"))
                .andExpect(jsonPath("$.code").value("MRI-01"));
    }

    @Test
    void testGetServiceById_NotFound() throws Exception {
        when(serviceService.findOne(9999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/setup/service/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateService_Success() throws Exception {
        var created = sampleService();
        when(serviceService.existsByNameIgnoreCase("MRI Scan")).thenReturn(false);
        when(serviceService.create(any())).thenReturn(created);

        mockMvc.perform(post("/api/setup/service")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "name": "MRI Scan",
                              "abbreviation": "MRI",
                              "code": "MRI-01",
                              "category": "CONSULTATION",
                              "price": 199.99,
                              "currency": "USD",
                              "isActive": true,
                              "createdBy": "tester"
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/setup/service/100"))
                .andExpect(jsonPath("$.name").value("MRI Scan"))
                .andExpect(jsonPath("$.code").value("MRI-01"));
    }

    @Test
    void testCreateService_ConflictOnName() throws Exception {
        when(serviceService.existsByNameIgnoreCase("MRI Scan")).thenReturn(true);

        mockMvc.perform(post("/api/setup/service")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "name": "MRI Scan",
                              "abbreviation": "MRI",
                              "code": "MRI-01",
                              "category": "CONSULTATION",
                              "price": 199.99,
                              "currency": "USD",
                              "isActive": true,
                              "createdBy": "tester"
                            }
                            """))
                .andExpect(status().isConflict());

        verify(serviceService, never()).create(any());
    }

    @Test
    void testUpdateService_Success() throws Exception {
        var svc = sampleService();
        when(serviceService.update(eq(100L), any())).thenReturn(Optional.of(svc));

        mockMvc.perform(put("/api/setup/service/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "id": 100,
                              "name": "MRI Scan",
                              "abbreviation": "MRI",
                              "code": "MRI-01",
                              "category": "CONSULTATION",
                              "price": 199.99,
                              "currency": "USD",
                              "isActive": true,
                              "lastModifiedBy": "admin"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("MRI Scan"))
                .andExpect(jsonPath("$.code").value("MRI-01"));
    }

    @Test
    void testUpdateService_NotFound() throws Exception {
        when(serviceService.update(eq(9999L), any())).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/setup/service/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "id": 9999,
                              "name": "Unknown",
                              "abbreviation": "UNK",
                              "code": "UNK-01",
                              "category": "CONSUMABLE",
                              "price": 10.00,
                              "currency": "USD",
                              "isActive": false,
                              "lastModifiedBy": "admin"
                            }
                            """))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetServicesByCategory_Paginated() throws Exception {
        var svc = sampleService();
        var pageable = PageRequest.of(0, 10);
        Page<ServiceResponseVM> page =
                new PageImpl<>(List.of(ServiceResponseVM.ofEntity(svc)), pageable, 1);

        when(serviceService.findByCategory(eq(ServiceCategory.CONSULTATION), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/setup/service/service-list-by-category")
                        .param("category", ServiceCategory.CONSULTATION.name())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$[0].category").value("CONSULTATION"));
    }

    @Test
    void testGetServicesByCode_Paginated() throws Exception {
        var svc = sampleService();
        var pageable = PageRequest.of(0, 10);
        Page<ServiceResponseVM> page =
                new PageImpl<>(List.of(ServiceResponseVM.ofEntity(svc)), pageable, 1);

        when(serviceService.findByCodeContainingIgnoreCase(eq("MRI-01"), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/setup/service/service-list-by-code")
                        .param("code", "MRI-01")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$[0].code").value("MRI-01"));
    }

    @Test
    void testGetServicesByName_Paginated() throws Exception {
        var svc = sampleService();
        var pageable = PageRequest.of(0, 10);
        Page<ServiceResponseVM> page =
                new PageImpl<>(List.of(ServiceResponseVM.ofEntity(svc)), pageable, 1);

        when(serviceService.findByNameContainingIgnoreCase(eq("MRI"), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/setup/service/service-list-by-name")
                        .param("name", "MRI")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$[0].name").value("MRI Scan"));
    }

    @Test
    void testToggleServiceActiveStatus() throws Exception {
        var svc = sampleService();
        when(serviceService.toggleIsActive(100L)).thenReturn(Optional.of(svc));

        mockMvc.perform(patch("/api/setup/service/100/toggle-active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("MRI Scan"))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(serviceService, times(1)).toggleIsActive(100L);
    }

    @Test
    void testToggleServiceActiveStatus_NotFound() throws Exception {
        when(serviceService.toggleIsActive(9999L)).thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/setup/service/9999/toggle-active"))
                .andExpect(status().isNotFound());
    }
}
