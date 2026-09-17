package com.dazzle.asklepios.web.rest.vm.practitioner;

import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.Practitioner;
import com.dazzle.asklepios.domain.enumeration.FacilityType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PractitionerResponseVMTest {

    @Test
    void ofEntity_mapsFacilityToFacilityDto() {
        Facility facility = new Facility();
        facility.setId(7L);
        facility.setName("Main Facility");
        facility.setCode("FAC-001");
        facility.setType(FacilityType.HOSPITAL);
        facility.setEmailAddress("facility@example.com");
        facility.setPhone1("1111111");
        facility.setPhone2("2222222");
        facility.setIsActive(true);
        facility.setTimeZone("Asia/Amman");

        Practitioner practitioner = new Practitioner();
        practitioner.setId(15L);
        practitioner.setFirstName("Jane");
        practitioner.setLastName("Doe");
        practitioner.setFacility(facility);

        PractitionerResponseVM response = PractitionerResponseVM.ofEntity(practitioner);

        assertNotNull(response.facility());
        assertEquals(7L, response.facility().id());
        assertEquals("Main Facility", response.facility().name());
        assertEquals("FAC-001", response.facility().code());
        assertEquals(FacilityType.HOSPITAL, response.facility().type());
        assertEquals("facility@example.com", response.facility().emailAddress());
        assertEquals("1111111", response.facility().phone1());
        assertEquals("2222222", response.facility().phone2());
        assertEquals(true, response.facility().isActive());
        assertEquals("Asia/Amman", response.facility().timeZone());
    }

    @Test
    void serialization_doesNotExposeFacilityRelationships() throws Exception {
        Facility facility = new Facility();
        facility.setId(7L);
        facility.setName("Main Facility");
        facility.setCode("FAC-001");
        facility.setType(FacilityType.HOSPITAL);
        facility.setIsActive(true);
        facility.setTimeZone("Asia/Amman");

        Department labDepartment = new Department();
        labDepartment.setId(99L);
        labDepartment.setName("Lab");
        labDepartment.setFacility(facility);
        facility.setDefaultLabDepartment(labDepartment);
        facility.setDefaultRadDepartment(labDepartment);

        Practitioner practitioner = new Practitioner();
        practitioner.setId(15L);
        practitioner.setFirstName("Jane");
        practitioner.setLastName("Doe");
        practitioner.setFacility(facility);

        PractitionerResponseVM response = PractitionerResponseVM.ofEntity(practitioner);
        String json = new ObjectMapper().writeValueAsString(response);

        assertTrue(json.contains("\"facility\":{\"id\":7"));
        assertTrue(json.contains("\"timeZone\":\"Asia/Amman\""));
        assertFalse(json.contains("defaultLabDepartment"));
        assertFalse(json.contains("defaultRadDepartment"));
    }
}


