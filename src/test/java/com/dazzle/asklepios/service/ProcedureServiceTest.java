package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.Procedure;
import com.dazzle.asklepios.repository.ProcedureRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProcedureServiceTest {

    private static final Long FACILITY_ID = 1L;
    private static final Long PROCEDURE_ID = 10L;

    @Mock
    private ProcedureRepository procedureRepository;

    @Mock
    private ProcedureItemsService procedureItemsService;

    @Mock
    private EntityManager em;

    @Mock
    private PriceListCatalogSyncService priceListCatalogSyncService;

    @InjectMocks
    private ProcedureService procedureService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(em.getReference(Facility.class, FACILITY_ID)).thenReturn(new Facility());
        when(procedureRepository.saveAndFlush(any(Procedure.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createSavesSelectedRelatedSurfaces() {
        Procedure saved = procedureService.create(FACILITY_ID, incoming(List.of("MESIAL", "OCCLUSAL", "LABIAL")));

        assertThat(saved.getRelatedSurfaces()).containsExactly("MESIAL", "OCCLUSAL", "LABIAL");
    }

    @Test
    void createWithoutRelatedSurfacesLeavesProcedureUnconfigured() {
        Procedure saved = procedureService.create(FACILITY_ID, incoming(null));

        assertThat(saved.getRelatedSurfaces()).isNull();
    }

    @Test
    void createRejectsUnknownSurface() {
        Procedure incoming = incoming(List.of("MESIAL", "TOP"));

        assertThrows(BadRequestAlertException.class, () -> procedureService.create(FACILITY_ID, incoming));
        verify(procedureRepository, never()).saveAndFlush(any(Procedure.class));
    }

    @Test
    void updateReplacesRelatedSurfaces() {
        Procedure existing = existing(List.of("MESIAL"));
        when(procedureRepository.findById(PROCEDURE_ID)).thenReturn(Optional.of(existing));

        Procedure updated = procedureService
                .update(PROCEDURE_ID, FACILITY_ID, incoming(List.of("DISTAL", "PALATAL")))
                .orElseThrow();

        assertThat(updated.getRelatedSurfaces()).containsExactly("DISTAL", "PALATAL");
    }

    @Test
    void updateKeepsRelatedSurfacesWhenFieldIsOmitted() {
        Procedure existing = existing(List.of("MESIAL", "DISTAL"));
        when(procedureRepository.findById(PROCEDURE_ID)).thenReturn(Optional.of(existing));

        Procedure updated = procedureService
                .update(PROCEDURE_ID, FACILITY_ID, incoming(null))
                .orElseThrow();

        assertThat(updated.getRelatedSurfaces()).containsExactly("MESIAL", "DISTAL");
    }

    @Test
    void updateClearsRelatedSurfacesWhenEmptyListIsSent() {
        Procedure existing = existing(List.of("MESIAL", "DISTAL"));
        when(procedureRepository.findById(PROCEDURE_ID)).thenReturn(Optional.of(existing));

        Procedure updated = procedureService
                .update(PROCEDURE_ID, FACILITY_ID, incoming(List.of()))
                .orElseThrow();

        assertThat(updated.getRelatedSurfaces()).isEmpty();
    }

    @Test
    void updateRejectsUnknownSurface() {
        Procedure existing = existing(List.of("MESIAL"));
        when(procedureRepository.findById(PROCEDURE_ID)).thenReturn(Optional.of(existing));
        Procedure incoming = incoming(List.of("NOT_A_SURFACE"));

        assertThrows(BadRequestAlertException.class,
                () -> procedureService.update(PROCEDURE_ID, FACILITY_ID, incoming));
        assertThat(existing.getRelatedSurfaces()).containsExactly("MESIAL");
    }

    private Procedure incoming(List<String> relatedSurfaces) {
        return Procedure.builder()
                .name("Filling")
                .code("97531-00-00")
                .isAppointable(true)
                .isActive(true)
                .relatedSurfaces(relatedSurfaces)
                .build();
    }

    private Procedure existing(List<String> relatedSurfaces) {
        Facility facility = new Facility();
        facility.setId(FACILITY_ID);
        Procedure procedure = incoming(new ArrayList<>(relatedSurfaces));
        procedure.setId(PROCEDURE_ID);
        procedure.setFacility(facility);
        return procedure;
    }
}
