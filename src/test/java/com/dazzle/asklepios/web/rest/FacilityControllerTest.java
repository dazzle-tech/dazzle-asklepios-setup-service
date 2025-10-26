package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.config.TestSecurityConfig;
import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.FacilityType;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.service.FacilityService;
import com.dazzle.asklepios.web.rest.vm.FacilityCreateVM;
import com.dazzle.asklepios.web.rest.vm.FacilityResponseVM;
import com.dazzle.asklepios.web.rest.vm.FacilityUpdateVM;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

import static org.hamcrest.Matchers.hasItems;

@WebMvcTest(controllers = FacilityController.class)
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc
class FacilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FacilityService facilityService;

    @MockitoBean
    private FacilityRepository facilityRepository;

    @Test
    void testGetAllFacilities() throws Exception {
        Facility facility = new Facility();
        facility.setId(1000L);
        facility.setName("General Hospital");
        facility.setCode("GH001");

        when(facilityService.findAll()).thenReturn(List.of(FacilityResponseVM.ofEntity(facility)));

        mockMvc.perform(get("/api/setup/facility"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("General Hospital"))
                .andExpect(jsonPath("$[0].code").value("GH001"));
    }

    @Test
    void testGetFacilityById() throws Exception {
        Facility facility = new Facility();
        facility.setId(1000L);
        facility.setName("General Hospital");
        facility.setCode("GH001");

        when(facilityService.findOne(1000L)).thenReturn(Optional.of(FacilityResponseVM.ofEntity(facility)));

        mockMvc.perform(get("/api/setup/facility/1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("General Hospital"))
                .andExpect(jsonPath("$.code").value("GH001"));
    }

    @Test
    void testGetFacilityById_NotFound() throws Exception {
        when(facilityService.findOne(9999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/setup/facility/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateFacility() throws Exception {
        Facility facility = new Facility();
        facility.setId(1001L);
        facility.setName("New Hospital");
        facility.setCode("NH001");

        FacilityCreateVM createVM = new FacilityCreateVM(
                "New Hospital",
                "NH001",
                FacilityType.HOSPITAL,
                "contact@newhospital.com",
                "123456789",
                "987654321",
                "111222333",
                "ADDR01",
                Currency.USD,
                true,
                LocalDate.of(2025, 10, 1)
        );

        when(facilityService.create(createVM)).thenReturn(FacilityResponseVM.ofEntity(facility));
        when(facilityRepository.existsByNameIgnoreCase("New Hospital")).thenReturn(false);

        mockMvc.perform(post("/api/setup/facility")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "New Hospital",
                                    "code": "NH001",
                                    "type": "HOSPITAL",
                                    "emailAddress": "contact@newhospital.com",
                                    "phone1": "123456789",
                                    "phone2": "987654321",
                                    "fax": "111222333",
                                    "addressId": "ADDR01",
                                    "defaultCurrency": "USD",
                                    "isActive": true,
                                    "registrationDate": "2025-10-01"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/setup/facility/1001"))
                .andExpect(jsonPath("$.name").value("New Hospital"))
                .andExpect(jsonPath("$.code").value("NH001"));
    }

    @Test
    void testUpdateFacility() throws Exception {
        Facility facility = new Facility();
        facility.setId(1000L);
        facility.setName("Updated Hospital");
        facility.setCode("UH001");

        FacilityUpdateVM updateVM = new FacilityUpdateVM(
                1000L,
                "Updated Hospital",
                "UH001",
                FacilityType.HOSPITAL,
                "contact@hospital.com",
                "123456789",
                "987654321",
                "111222333",
                "ADDR01",
                Currency.USD,
                true
        );

        when(facilityService.update(1000L, updateVM)).thenReturn(Optional.of(facility));

        mockMvc.perform(put("/api/setup/facility/1000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "id": 1000,
                                    "name": "Updated Hospital",
                                    "code": "UH001",
                                    "type": "HOSPITAL",
                                    "emailAddress": "contact@hospital.com",
                                    "phone1": "123456789",
                                    "phone2": "987654321",
                                    "fax": "111222333",
                                    "addressId": "ADDR01",
                                    "defaultCurrency": "USD",
                                    "isActive": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Hospital"))
                .andExpect(jsonPath("$.code").value("UH001"));
    }

    @Test
    void testUpdateFacility_NotFound() throws Exception {
        FacilityUpdateVM updateVM = new FacilityUpdateVM(
                9999L,
                "Unknown",
                "UN001",
                FacilityType.CLINIC,
                "unknown@hospital.com",
                "000",
                "000",
                "000",
                "ADDR99",
                Currency.EUR,
                false
        );

        when(facilityService.update(9999L, updateVM)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/setup/facility/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "id": 9999,
                                    "name": "Unknown",
                                    "code": "UN001",
                                    "type": "CLINIC",
                                    "emailAddress": "unknown@hospital.com",
                                    "phone1": "000",
                                    "phone2": "000",
                                    "fax": "000",
                                    "addressId": "ADDR99",
                                    "defaultCurrency": "EUR",
                                    "isActive": false
                                }
                                """))
                .andExpect(status().isNotFound());
    }
}
