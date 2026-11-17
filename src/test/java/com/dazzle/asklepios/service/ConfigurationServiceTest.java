package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Configuration;
import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.User;
import com.dazzle.asklepios.domain.enumeration.ConfigurationKeys;
import com.dazzle.asklepios.domain.enumeration.ConfigurationReferenceType;
import com.dazzle.asklepios.domain.enumeration.ConfigurationScope;
import com.dazzle.asklepios.domain.enumeration.ConfigurationValueType;
import com.dazzle.asklepios.domain.enumeration.JobRole;
import com.dazzle.asklepios.repository.ConfigurationRepository;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.repository.UserRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.configuration.ConfigurationCreateVM;
import com.dazzle.asklepios.web.rest.vm.configuration.ConfigurationUpdateVM;
import com.dazzle.asklepios.web.rest.vm.configuration.ResolvedConfigurationVM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConfigurationServiceTest {

    private static final ConfigurationKeys KEY = ConfigurationKeys.ENABLE_APPOINTMENT_RECURRING;

    @Mock
    private ConfigurationRepository configurationRepository;

    @Mock
    private FacilityRepository facilityRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ConfigurationService configurationService;

    private Facility facility;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        facility = Facility.builder().id(10L).name("Main Clinic").build();
        user = User.builder()
                .id(20L)
                .login("ada")
                .firstName("Ada")
                .lastName("Lovelace")
                .email("ada@example.com")
                .jobRole(JobRole.PHYSICIAN)
                .build();
    }

    @Test
    void createOrganizationWhenFacilityAndUserAreEmpty() {
        when(configurationRepository.findByKeyAndScopeTypeAndFacilityIsNull(KEY, ConfigurationScope.ORGANIZATION))
                .thenReturn(List.of());
        when(configurationRepository.save(any(Configuration.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Configuration created = configurationService.create(createVm(null, null, "true"));

        assertThat(created.getScopeType()).isEqualTo(ConfigurationScope.ORGANIZATION);
        assertThat(created.getFacility()).isNull();
        assertThat(created.getUsers()).isEmpty();
    }

    @Test
    void createFacilityWhenOnlyFacilityIsSelected() {
        when(facilityRepository.findById(10L)).thenReturn(Optional.of(facility));
        when(configurationRepository.findByKeyAndScopeTypeAndFacility_Id(KEY, ConfigurationScope.FACILITY, 10L))
                .thenReturn(List.of());
        when(configurationRepository.save(any(Configuration.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Configuration created = configurationService.create(createVm(10L, null, "false"));

        assertThat(created.getScopeType()).isEqualTo(ConfigurationScope.FACILITY);
        assertThat(created.getFacility()).isEqualTo(facility);
        assertThat(created.getUsers()).isEmpty();
    }

    @Test
    void createUserWhenFacilityAndUserAreSelected() {
        when(facilityRepository.findById(10L)).thenReturn(Optional.of(facility));
        when(userRepository.findAllById(Set.of(20L))).thenReturn(List.of(user));
        when(configurationRepository.findByKeyAndScopeTypeAndFacility_IdAndIdNotAndUsers_IdIn(
                eq(KEY), eq(ConfigurationScope.USER), eq(10L), eq(-1L), eq(Set.of(20L))
        )).thenReturn(List.of());
        when(configurationRepository.save(any(Configuration.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Configuration created = configurationService.create(createVm(10L, Set.of(20L), "true"));

        assertThat(created.getScopeType()).isEqualTo(ConfigurationScope.USER);
        assertThat(created.getFacility()).isEqualTo(facility);
        assertThat(created.getUsers()).containsExactly(user);
    }

    @Test
    void createRejectsUserWithoutFacility() {
        when(userRepository.findAllById(Set.of(20L))).thenReturn(List.of(user));

        BadRequestAlertException ex = assertThrows(
                BadRequestAlertException.class,
                () -> configurationService.create(createVm(null, Set.of(20L), "true"))
        );

        assertThat(ex.getMessage()).contains("requires a facility");
        verify(configurationRepository, never()).save(any());
    }

    @Test
    void getConfigUsesUserThenFacilityThenOrganization() {
        Configuration userRow = row(3L, ConfigurationScope.USER, "user-value");
        Configuration facilityRow = row(2L, ConfigurationScope.FACILITY, "facility-value");
        Configuration organizationRow = row(1L, ConfigurationScope.ORGANIZATION, "organization-value");

        when(configurationRepository.findByKeyAndScopeTypeAndIsActiveTrueAndFacility_IdAndUsers_IdOrderByIdDesc(
                KEY, ConfigurationScope.USER, 10L, 20L
        )).thenReturn(List.of(userRow));
        ResolvedConfigurationVM userHit = configurationService.getConfig(KEY, 20L, 10L).orElseThrow();
        assertThat(userHit.value()).isEqualTo("user-value");
        assertThat(userHit.scope()).isEqualTo(ConfigurationScope.USER);

        when(configurationRepository.findByKeyAndScopeTypeAndIsActiveTrueAndFacility_IdAndUsers_IdOrderByIdDesc(
                KEY, ConfigurationScope.USER, 10L, 20L
        )).thenReturn(List.of());
        when(configurationRepository.findByKeyAndScopeTypeAndFacility_IdAndIsActiveTrue(KEY, ConfigurationScope.FACILITY, 10L))
                .thenReturn(List.of(facilityRow));
        ResolvedConfigurationVM facilityHit = configurationService.getConfig(KEY, 20L, 10L).orElseThrow();
        assertThat(facilityHit.value()).isEqualTo("facility-value");
        assertThat(facilityHit.scope()).isEqualTo(ConfigurationScope.FACILITY);

        when(configurationRepository.findByKeyAndScopeTypeAndFacility_IdAndIsActiveTrue(KEY, ConfigurationScope.FACILITY, 10L))
                .thenReturn(List.of());
        when(configurationRepository.findByKeyAndScopeTypeAndFacilityIsNullAndIsActiveTrue(KEY, ConfigurationScope.ORGANIZATION))
                .thenReturn(List.of(organizationRow));
        ResolvedConfigurationVM organizationHit = configurationService.getConfig(KEY, 20L, 10L).orElseThrow();
        assertThat(organizationHit.value()).isEqualTo("organization-value");
        assertThat(organizationHit.scope()).isEqualTo(ConfigurationScope.ORGANIZATION);

        when(configurationRepository.findByKeyAndScopeTypeAndFacilityIsNullAndIsActiveTrue(KEY, ConfigurationScope.ORGANIZATION))
                .thenReturn(List.of());
        assertThat(configurationService.getConfig(KEY, 20L, 10L)).isEmpty();
    }

    @Test
    void getConfigSkipsUserLevelWhenFacilityIsMissing() {
        Configuration organizationRow = row(1L, ConfigurationScope.ORGANIZATION, "organization-value");
        when(configurationRepository.findByKeyAndScopeTypeAndFacilityIsNullAndIsActiveTrue(KEY, ConfigurationScope.ORGANIZATION))
                .thenReturn(List.of(organizationRow));

        ResolvedConfigurationVM resolved = configurationService.getConfig(KEY, 20L, null).orElseThrow();

        assertThat(resolved.value()).isEqualTo("organization-value");
        verify(configurationRepository, never())
                .findByKeyAndScopeTypeAndIsActiveTrueAndFacility_IdAndUsers_IdOrderByIdDesc(any(), any(), any(), any());
    }

    @Test
    void createRejectsDuplicateUserOverride() {
        when(facilityRepository.findById(10L)).thenReturn(Optional.of(facility));
        when(userRepository.findAllById(Set.of(20L))).thenReturn(List.of(user));
        Configuration existing = row(9L, ConfigurationScope.USER, "true");
        existing.setUsers(Set.of(user));
        when(configurationRepository.findByKeyAndScopeTypeAndFacility_IdAndIdNotAndUsers_IdIn(
                eq(KEY), eq(ConfigurationScope.USER), eq(10L), eq(-1L), eq(Set.of(20L))
        )).thenReturn(List.of(existing));

        BadRequestAlertException ex = assertThrows(
                BadRequestAlertException.class,
                () -> configurationService.create(createVm(10L, Set.of(20L), "true"))
        );

        assertThat(ex.getMessage()).contains("Duplicate configuration key");
        verify(configurationRepository, never()).save(any());
    }

    @Test
    void updateChangesFacilityLevelRowToUserLevel() {
        Configuration existing = row(9L, ConfigurationScope.FACILITY, "false");
        existing.setFacility(facility);
        existing.setUsers(new HashSet<>());
        existing.getUsers().add(user);

        when(configurationRepository.findById(9L)).thenReturn(Optional.of(existing));
        when(facilityRepository.findById(10L)).thenReturn(Optional.of(facility));
        when(userRepository.findAllById(Set.of(20L))).thenReturn(List.of(user));
        when(configurationRepository.findByKeyAndScopeTypeAndFacility_IdAndIdNotAndUsers_IdIn(
                eq(KEY), eq(ConfigurationScope.USER), eq(10L), eq(9L), eq(Set.of(20L))
        )).thenReturn(List.of(existing));
        when(configurationRepository.save(any(Configuration.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConfigurationUpdateVM update = new ConfigurationUpdateVM(
                9L,
                10L,
                Set.of(20L),
                KEY,
                "false",
                ConfigurationValueType.BOOLEAN,
                ConfigurationReferenceType.UI_ACTION,
                "show appointment recurring",
                true
        );

        Configuration updated = configurationService.update(9L, update).orElseThrow();

        assertThat(updated.getScopeType()).isEqualTo(ConfigurationScope.USER);
        assertThat(updated.getFacility()).isEqualTo(facility);
        assertThat(updated.getUsers()).containsExactly(user);
    }

    @Test
    void updateChangesUserLevelRowToFacilityLevel() {
        Configuration existing = row(9L, ConfigurationScope.USER, "false");
        existing.setFacility(facility);
        existing.setUsers(new HashSet<>(Set.of(user)));

        when(configurationRepository.findById(9L)).thenReturn(Optional.of(existing));
        when(facilityRepository.findById(10L)).thenReturn(Optional.of(facility));
        when(configurationRepository.findByKeyAndScopeTypeAndFacility_Id(KEY, ConfigurationScope.FACILITY, 10L))
                .thenReturn(List.of(existing));
        when(configurationRepository.save(any(Configuration.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Configuration updated = configurationService.update(9L, updateVm(10L, Set.of(), "false")).orElseThrow();

        assertThat(updated.getScopeType()).isEqualTo(ConfigurationScope.FACILITY);
        assertThat(updated.getFacility()).isEqualTo(facility);
        assertThat(updated.getUsers()).isEmpty();
    }

    @Test
    void updateChangesFacilityLevelRowToOrganizationLevel() {
        Configuration existing = row(9L, ConfigurationScope.FACILITY, "false");
        existing.setFacility(facility);
        existing.setUsers(new HashSet<>());

        when(configurationRepository.findById(9L)).thenReturn(Optional.of(existing));
        when(configurationRepository.findByKeyAndScopeTypeAndFacilityIsNull(KEY, ConfigurationScope.ORGANIZATION))
                .thenReturn(List.of(existing));
        when(configurationRepository.save(any(Configuration.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Configuration updated = configurationService.update(9L, updateVm(null, null, "false")).orElseThrow();

        assertThat(updated.getScopeType()).isEqualTo(ConfigurationScope.ORGANIZATION);
        assertThat(updated.getFacility()).isNull();
        assertThat(updated.getUsers()).isEmpty();
    }

    @Test
    void updateChangesOrganizationLevelRowToUserLevel() {
        Configuration existing = row(9L, ConfigurationScope.ORGANIZATION, "true");
        existing.setUsers(new HashSet<>());

        when(configurationRepository.findById(9L)).thenReturn(Optional.of(existing));
        when(facilityRepository.findById(10L)).thenReturn(Optional.of(facility));
        when(userRepository.findAllById(Set.of(20L))).thenReturn(List.of(user));
        when(configurationRepository.findByKeyAndScopeTypeAndFacility_IdAndIdNotAndUsers_IdIn(
                eq(KEY), eq(ConfigurationScope.USER), eq(10L), eq(9L), eq(Set.of(20L))
        )).thenReturn(List.of(existing));
        when(configurationRepository.save(any(Configuration.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Configuration updated = configurationService.update(9L, updateVm(10L, Set.of(20L), "true")).orElseThrow();

        assertThat(updated.getScopeType()).isEqualTo(ConfigurationScope.USER);
        assertThat(updated.getUsers()).containsExactly(user);
        assertThat(updated.getFacility()).isEqualTo(facility);
    }

    @Test
    void createPersistsDerivedScope() {
        when(facilityRepository.findById(10L)).thenReturn(Optional.of(facility));
        when(userRepository.findAllById(Set.of(20L))).thenReturn(List.of(user));
        when(configurationRepository.findByKeyAndScopeTypeAndFacility_IdAndIdNotAndUsers_IdIn(any(), any(), any(), any(), any()))
                .thenReturn(List.of());
        when(configurationRepository.save(any(Configuration.class))).thenAnswer(invocation -> invocation.getArgument(0));

        configurationService.create(createVm(10L, Set.of(20L), "true"));

        ArgumentCaptor<Configuration> captor = ArgumentCaptor.forClass(Configuration.class);
        verify(configurationRepository).save(captor.capture());
        assertThat(captor.getValue().getScopeType()).isEqualTo(ConfigurationScope.USER);
    }

    private ConfigurationUpdateVM updateVm(Long facilityId, Set<Long> userIds, String value) {
        return new ConfigurationUpdateVM(
                9L,
                facilityId,
                userIds,
                KEY,
                value,
                ConfigurationValueType.BOOLEAN,
                ConfigurationReferenceType.UI_ACTION,
                "show appointment recurring",
                true
        );
    }

    private ConfigurationCreateVM createVm(Long facilityId, Set<Long> userIds, String value) {
        return new ConfigurationCreateVM(
                facilityId,
                userIds,
                KEY,
                value,
                ConfigurationValueType.BOOLEAN,
                ConfigurationReferenceType.OTHERS,
                "Enable appointment recurring",
                true
        );
    }

    private Configuration row(Long id, ConfigurationScope scope, String value) {
        return Configuration.builder()
                .id(id)
                .scopeType(scope)
                .key(KEY)
                .value(value)
                .valueType(ConfigurationValueType.BOOLEAN)
                .referenceType(ConfigurationReferenceType.OTHERS)
                .description("desc")
                .isActive(true)
                .build();
    }
}
