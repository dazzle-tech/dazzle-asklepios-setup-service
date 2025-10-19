package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.config.TestSecurityConfig;
import com.dazzle.asklepios.domain.Service;
import com.dazzle.asklepios.domain.ServiceItems;
import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.ServiceCategory;
import com.dazzle.asklepios.domain.enumeration.ServiceItemsType;
import com.dazzle.asklepios.service.ServiceItemsService;
import com.dazzle.asklepios.web.rest.vm.DepartmentResponseVM;
import com.dazzle.asklepios.web.rest.vm.ServiceItemsResponseVM;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web layer tests for ServiceItemsController.
 */
@WebMvcTest(controllers = ServiceItemsController.class)
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc
class ServiceItemsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ServiceItemsService serviceItemsService;

    // --------- Helpers ---------

    private Service sampleService() {
        return Service.builder()
                .id(10L)
                .name("MRI")
                .code("MRI-01")
                .category(ServiceCategory.CONSULTATION)
                .currency(Currency.USD)
                .isActive(true)
                .createdBy("tester")
                .build();
    }

    private ServiceItems sampleServiceItems() {
        return ServiceItems.builder()
                .id(100L)
                .type(ServiceItemsType.DEPARTMENTS)
                .sourceId(55L)
                .service(sampleService())
                .createdBy("tester")
                .createdDate(Instant.parse("2025-01-01T00:00:00Z"))
                .lastModifiedBy("tester")
                .lastModifiedDate(Instant.parse("2025-01-02T00:00:00Z"))
                .isActive(true)
                .build();
    }

    // --------- Tests ---------

    @Test
    void testList_Paginated() throws Exception {
        var entity = sampleServiceItems();
        Pageable pageable = PageRequest.of(0, 10);
        Page<ServiceItemsResponseVM> page =
                new PageImpl<>(List.of(ServiceItemsResponseVM.ofEntity(entity)), pageable, 1);

        when(serviceItemsService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/setup/service-items")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].serviceId").value(10))
                .andExpect(jsonPath("$[0].type").value("DEPARTMENTS"));
    }

    @Test
    void testGet_ById_Found() throws Exception {
        var entity = sampleServiceItems();
        when(serviceItemsService.findOne(100L)).thenReturn(Optional.of(entity));

        mockMvc.perform(get("/api/setup/service-items/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.serviceId").value(10))
                .andExpect(jsonPath("$.type").value("DEPARTMENTS"));
    }

    @Test
    void testGet_ById_NotFound() throws Exception {
        when(serviceItemsService.findOne(9999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/setup/service-items/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreate_Success() throws Exception {
        var created = sampleServiceItems();
        when(serviceItemsService.create(any())).thenReturn(created);

        mockMvc.perform(post("/api/setup/service-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "type": "DEPARTMENTS",
                              "sourceId": 55,
                              "serviceId": 10,
                              "createdBy": "tester",
                              "isActive": true
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/setup/service-items/100"))
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.serviceId").value(10))
                .andExpect(jsonPath("$.type").value("DEPARTMENTS"));

        verify(serviceItemsService, times(1)).create(any());
    }

    @Test
    void testUpdate_Success() throws Exception {
        var updatedEntity = sampleServiceItems(); // you can tweak fields if you want to assert changed values
        when(serviceItemsService.update(eq(100L), any())).thenReturn(Optional.of(updatedEntity));

        mockMvc.perform(put("/api/setup/service-items/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "id": 100,
                              "type": "DEPARTMENTS",
                              "sourceId": 77,
                              "serviceId": 10,
                              "isActive": false,
                              "lastModifiedBy": "admin"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.serviceId").value(10))
                .andExpect(jsonPath("$.type").value("DEPARTMENTS"));
    }

    @Test
    void testUpdate_NotFound() throws Exception {
        when(serviceItemsService.update(eq(9999L), any())).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/setup/service-items/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "id": 9999,
                              "type": "DEPARTMENTS",
                              "sourceId": 1,
                              "serviceId": 10,
                              "isActive": true,
                              "lastModifiedBy": "admin"
                            }
                            """))
                .andExpect(status().isNotFound());
    }

    @Test
    void testListByService_Paginated() throws Exception {
        var entity = sampleServiceItems();
        Pageable pageable = PageRequest.of(0, 5);
        Page<ServiceItemsResponseVM> page =
                new PageImpl<>(List.of(ServiceItemsResponseVM.ofEntity(entity)), pageable, 1);

        when(serviceItemsService.findByServiceId(eq(10L), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/setup/service-items/by-service/10")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].serviceId").value(10));
    }

    @Test
    void testToggleActive_Found() throws Exception {
        var entity = sampleServiceItems();
        when(serviceItemsService.toggleIsActive(100L)).thenReturn(Optional.of(entity));

        mockMvc.perform(patch("/api/setup/service-items/100/toggle-active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.serviceId").value(10));

        verify(serviceItemsService, times(1)).toggleIsActive(100L);
    }

    @Test
    void testToggleActive_NotFound() throws Exception {
        when(serviceItemsService.toggleIsActive(9999L)).thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/setup/service-items/9999/toggle-active"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDelete() throws Exception {
        doNothing().when(serviceItemsService).delete(100L);

        mockMvc.perform(delete("/api/setup/service-items/100"))
                .andExpect(status().isNoContent());

        verify(serviceItemsService, times(1)).delete(100L);
    }

    @Test
    void testListSourcesByTypeAndFacility_NoPagination() throws Exception {
        // Return an empty list (we only verify delegation & 200 OK here)
        when(serviceItemsService.findSourcesByTypeAndFacility(eq(ServiceItemsType.DEPARTMENTS), eq(5L)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/setup/service-items/sources/by-facility")
                        .param("type", "DEPARTMENTS")
                        .param("facilityId", "5"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        verify(serviceItemsService).findSourcesByTypeAndFacility(ServiceItemsType.DEPARTMENTS, 5L);
    }
}
