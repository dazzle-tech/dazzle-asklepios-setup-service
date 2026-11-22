package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.ChildGrowthSetup;
import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.childgrowth.ChildGrowthDocument;
import com.dazzle.asklepios.domain.enumeration.ChildGrowthIndicator;
import com.dazzle.asklepios.domain.enumeration.ConfigurationScope;
import com.dazzle.asklepios.repository.ChildGrowthSetupRepository;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.repository.UserDepartmentRepository;
import com.dazzle.asklepios.repository.UserRepository;
import com.dazzle.asklepios.repository.UserRoleRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.childgrowth.ChildGrowthSetupResponseVM;
import com.dazzle.asklepios.web.rest.vm.childgrowth.ChildGrowthSetupSaveVM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChildGrowthSetupServiceTest {

    private ChildGrowthSetupRepository repository;
    private FacilityRepository facilityRepository;
    private ChildGrowthSetupService service;

    @BeforeEach
    void setUp() {
        repository = mock(ChildGrowthSetupRepository.class);
        facilityRepository = mock(FacilityRepository.class);
        service = new ChildGrowthSetupService(
                repository,
                facilityRepository,
                mock(UserRepository.class),
                mock(UserRoleRepository.class),
                mock(UserDepartmentRepository.class)
        );
        when(repository.save(any(ChildGrowthSetup.class))).thenAnswer(invocation -> {
            ChildGrowthSetup entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(7L);
            }
            return entity;
        });
    }

    @Test
    void getReturnsExampleWhenNothingIsSaved() {
        when(repository.findByScopeTypeAndFacilityIsNullAndUserIsNull(ConfigurationScope.ORGANIZATION))
                .thenReturn(Optional.empty());

        ChildGrowthSetupResponseVM response = service.get(ConfigurationScope.ORGANIZATION, null, null);

        assertThat(response.saved()).isFalse();
        assertThat(response.id()).isNull();
        assertThat(response.charts()).hasSize(5);
        assertThat(response.charts().get(0).getId()).isEqualTo("who");
    }

    @Test
    void saveOrganizationCreatesThenUpdatesTheSameRow() {
        when(repository.findByScopeTypeAndFacilityIsNullAndUserIsNull(ConfigurationScope.ORGANIZATION))
                .thenReturn(Optional.empty());

        ChildGrowthSetupResponseVM created = service.save(saveVm(exampleDocument()));
        assertThat(created.saved()).isTrue();
        assertThat(created.id()).isEqualTo(7L);
        assertThat(created.facilityId()).isNull();

        ChildGrowthSetup existing = ChildGrowthSetup.builder()
                .id(7L)
                .scopeType(ConfigurationScope.ORGANIZATION)
                .document(ChildGrowthDocument.example())
                .build();
        when(repository.findByScopeTypeAndFacilityIsNullAndUserIsNull(ConfigurationScope.ORGANIZATION))
                .thenReturn(Optional.of(existing));

        ChildGrowthDocument changed = exampleDocument();
        changed.getRules().setCorrectedAgeUntilMonths(18);
        ChildGrowthSetupResponseVM updated = service.save(saveVm(changed));

        assertThat(updated.id()).isEqualTo(7L);
        assertThat(updated.rules().getCorrectedAgeUntilMonths()).isEqualTo(18);
        verify(repository, org.mockito.Mockito.times(2)).save(any(ChildGrowthSetup.class));
    }

    @Test
    void facilityScopeRequiresAFacility() {
        assertThrows(BadRequestAlertException.class,
                () -> service.save(new ChildGrowthSetupSaveVM(
                        ConfigurationScope.FACILITY, null, null,
                        exampleDocument().getCharts(),
                        exampleDocument().getRules(),
                        stringBands(exampleDocument()),
                        exampleDocument().getLimits(),
                        exampleDocument().getAlerts()
                )));
    }

    @Test
    void overlappingBandsAreRejected() {
        ChildGrowthDocument document = exampleDocument();
        document.getBandsByIndicator().get(ChildGrowthIndicator.WFA).stream()
                .filter(band -> "watch-low".equals(band.getId()))
                .findFirst()
                .orElseThrow()
                .setMin(new BigDecimal("-3"));

        assertThrows(BadRequestAlertException.class, () -> service.save(saveVm(document)));
    }

    @Test
    void enabledChartRequiresAgeToAfterAgeFrom() {
        ChildGrowthDocument document = exampleDocument();
        document.getCharts().get(0).setAgeTo(0);

        BadRequestAlertException error = assertThrows(BadRequestAlertException.class, () -> service.save(saveVm(document)));
        assertThat(error.getBody().getTitle()).contains("Age to");
    }

    @Test
    void disabledCustomChartMayOmitAges() {
        when(repository.findByScopeTypeAndFacilityIsNullAndUserIsNull(ConfigurationScope.ORGANIZATION))
                .thenReturn(Optional.empty());
        Facility facility = new Facility();
        facility.setId(4L);
        when(facilityRepository.findById(4L)).thenReturn(Optional.of(facility));
        when(repository.findByScopeTypeAndFacility_IdAndUserIsNull(ConfigurationScope.FACILITY, 4L))
                .thenReturn(Optional.empty());

        ChildGrowthDocument document = exampleDocument();
        document.getCharts().stream()
                .filter(chart -> "custom".equals(chart.getId()))
                .findFirst()
                .orElseThrow()
                .setEnabled(false);

        ChildGrowthSetupResponseVM saved = service.save(new ChildGrowthSetupSaveVM(
                ConfigurationScope.FACILITY,
                4L,
                null,
                document.getCharts(),
                document.getRules(),
                stringBands(document),
                document.getLimits(),
                document.getAlerts()
        ));

        assertThat(saved.facilityId()).isEqualTo(4L);
        assertThat(saved.saved()).isTrue();
    }

    private ChildGrowthSetupSaveVM saveVm(ChildGrowthDocument document) {
        return new ChildGrowthSetupSaveVM(
                ConfigurationScope.ORGANIZATION,
                null,
                null,
                document.getCharts(),
                document.getRules(),
                stringBands(document),
                document.getLimits(),
                document.getAlerts()
        );
    }

    private ChildGrowthDocument exampleDocument() {
        ChildGrowthDocument document = ChildGrowthDocument.example();
        Map<ChildGrowthIndicator, List<ChildGrowthDocument.Band>> copy = new EnumMap<>(ChildGrowthIndicator.class);
        document.getBandsByIndicator().forEach((indicator, bands) -> copy.put(indicator, bands.stream()
                .map(band -> ChildGrowthDocument.Band.builder()
                        .id(band.getId())
                        .label(band.getLabel())
                        .tone(band.getTone())
                        .min(band.getMin())
                        .max(band.getMax())
                        .percentile(band.getPercentile())
                        .alerted(band.getAlerted())
                        .build())
                .toList()));
        document.setBandsByIndicator(copy);
        return document;
    }

    private Map<String, List<ChildGrowthDocument.Band>> stringBands(ChildGrowthDocument document) {
        Map<String, List<ChildGrowthDocument.Band>> bands = new HashMap<>();
        document.getBandsByIndicator().forEach((indicator, value) -> bands.put(indicator.name(), value));
        return bands;
    }
}
