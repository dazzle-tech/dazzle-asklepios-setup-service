package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Configuration;
import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.User;
import com.dazzle.asklepios.domain.enumeration.ConfigurationKeys;
import com.dazzle.asklepios.domain.enumeration.ConfigurationReferenceType;
import com.dazzle.asklepios.domain.enumeration.ConfigurationScope;
import com.dazzle.asklepios.domain.enumeration.ConfigurationValueType;
import com.dazzle.asklepios.repository.ConfigurationRepository;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.repository.UserDepartmentRepository;
import com.dazzle.asklepios.repository.UserRepository;
import com.dazzle.asklepios.repository.UserRoleRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.configuration.ConfigurationCreateVM;
import com.dazzle.asklepios.web.rest.vm.configuration.ConfigurationUpdateVM;
import com.dazzle.asklepios.web.rest.vm.configuration.ResolvedConfigurationVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class ConfigurationService {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationService.class);
    private static final long NO_ROW_TO_EXCLUDE = -1L;

    private final ConfigurationRepository configurationRepository;
    private final FacilityRepository facilityRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserDepartmentRepository userDepartmentRepository;

    public ConfigurationService(
            ConfigurationRepository configurationRepository,
            FacilityRepository facilityRepository,
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            UserDepartmentRepository userDepartmentRepository
    ) {
        this.configurationRepository = configurationRepository;
        this.facilityRepository = facilityRepository;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.userDepartmentRepository = userDepartmentRepository;
    }

    public Configuration create(ConfigurationCreateVM vm) {
        LOG.debug("Request to create Configuration : {}", vm);

        Facility facility = loadFacility(vm.facilityId());
        Set<User> users = loadUsers(vm.userIds());
        ConfigurationScope scope = scopeOf(facility, users);
        assertUnique(vm.key(), facility, users, null);

        Configuration configuration = Configuration.builder()
                .scopeType(scope)
                .facility(facility)
                .users(users)
                .key(vm.key())
                .value(vm.value())
                .valueType(vm.valueType())
                .referenceType(vm.referenceType())
                .description(vm.description())
                .isActive(Boolean.TRUE.equals(vm.isActive()))
                .build();

        return configurationRepository.save(configuration);
    }

    public Optional<Configuration> update(Long id, ConfigurationUpdateVM vm) {
        LOG.debug("Request to update Configuration id={} with {}", id, vm);

        Configuration configuration = configurationRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Configuration not found with id " + id,
                        "configuration",
                        "notfound"
                ));

        Facility facility = loadFacility(vm.facilityId());
        Set<User> users = loadUsers(vm.userIds());
        ConfigurationScope scope = scopeOf(facility, users);

        ConfigurationKeys key = vm.key() != null ? vm.key() : configuration.getKey();
        assertUnique(key, facility, users, id);

        configuration.setScopeType(scope);
        configuration.setFacility(facility);
        if (configuration.getUsers() == null) {
            configuration.setUsers(new HashSet<>());
        }
        configuration.getUsers().clear();
        configuration.getUsers().addAll(users);

        if (vm.key() != null) configuration.setKey(vm.key());
        if (vm.value() != null) configuration.setValue(vm.value());
        if (vm.description() != null) configuration.setDescription(vm.description());
        if (vm.referenceType() != null) configuration.setReferenceType(vm.referenceType());
        if (vm.valueType() != null) configuration.setValueType(vm.valueType());
        if (vm.isActive() != null) configuration.setIsActive(vm.isActive());

        Configuration updated = configurationRepository.save(configuration);
        LOG.debug("Updated configuration id={}", updated.getId());

        return Optional.of(updated);
    }

    /**
     * Feature check. Most specific active row wins:
     * user (facility + user) → facility → organization.
     * Empty when no stored row matches.
     */
    @Transactional(readOnly = true)
    public Optional<ResolvedConfigurationVM> getConfig(ConfigurationKeys key, Long userId, Long facilityId) {
        if (key == null) {
            throw new BadRequestAlertException("Configuration key is required", "configuration", "keyrequired");
        }
        LOG.debug("Resolve configuration key={} userId={} facilityId={}", key, userId, facilityId);

        return findWinningRow(key, userId, facilityId)
                .map(ResolvedConfigurationVM::fromRow);
    }

    /**
     * Same order as {@link #getConfig}.
     * Empty when no stored row matches.
     */
    @Transactional(readOnly = true)
    public Optional<Configuration> findEffective(ConfigurationKeys key, Long userId, Long facilityId) {
        LOG.debug("Request effective Configuration key={} userId={} facilityId={}", key, userId, facilityId);
        return findWinningRow(key, userId, facilityId);
    }

    private Optional<Configuration> findWinningRow(ConfigurationKeys key, Long userId, Long facilityId) {
        if (userId != null && facilityId != null) {
            List<Configuration> userLevel = configurationRepository
                    .findByKeyAndScopeTypeAndIsActiveTrueAndFacility_IdAndUsers_IdOrderByIdDesc(
                            key,
                            ConfigurationScope.USER,
                            facilityId,
                            userId
                    );
            if (!userLevel.isEmpty()) {
                if (userLevel.size() > 1) {
                    LOG.warn(
                            "Multiple active user configurations for key={} facilityId={} userId={}; using id={}",
                            key,
                            facilityId,
                            userId,
                            userLevel.get(0).getId()
                    );
                }
                return Optional.of(userLevel.get(0));
            }
        }

        if (facilityId != null) {
            List<Configuration> facilityLevel = configurationRepository
                    .findByKeyAndScopeTypeAndFacility_IdAndIsActiveTrue(key, ConfigurationScope.FACILITY, facilityId);
            if (!facilityLevel.isEmpty()) {
                return Optional.of(facilityLevel.get(0));
            }
        }

        List<Configuration> organizationLevel = configurationRepository
                .findByKeyAndScopeTypeAndFacilityIsNullAndIsActiveTrue(key, ConfigurationScope.ORGANIZATION);
        if (!organizationLevel.isEmpty()) {
            return Optional.of(organizationLevel.get(0));
        }

        return Optional.empty();
    }

    @Transactional(readOnly = true)
    public Page<User> searchUsers(String searchText, Long facilityId, Pageable pageable) {
        boolean blankSearch = searchText == null || searchText.isBlank();
        if (blankSearch && facilityId == null) {
            return Page.empty(pageable);
        }
        String query = blankSearch ? "" : searchText.trim();
        LOG.debug("Search configuration users text='{}' facilityId={}", query, facilityId);

        if (facilityId == null) {
            return userRepository.findByLoginContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                    query,
                    query,
                    query,
                    query,
                    pageable
            );
        }

        Map<Long, User> matches = new LinkedHashMap<>();
        if (blankSearch) {
            userRoleRepository.findByRole_FacilityId(facilityId)
                    .forEach(userRole -> addUser(matches, userRole.getUser()));
            userDepartmentRepository.findByDepartment_Facility_Id(facilityId)
                    .forEach(userDepartment -> addUser(matches, userDepartment.getUser()));
        } else {
            userRoleRepository
                    .findByRole_FacilityIdAndUser_LoginContainingIgnoreCaseOrRole_FacilityIdAndUser_FirstNameContainingIgnoreCaseOrRole_FacilityIdAndUser_LastNameContainingIgnoreCaseOrRole_FacilityIdAndUser_EmailContainingIgnoreCase(
                            facilityId, query, facilityId, query, facilityId, query, facilityId, query
                    )
                    .forEach(userRole -> addUser(matches, userRole.getUser()));
            userDepartmentRepository
                    .findByDepartment_Facility_IdAndUser_LoginContainingIgnoreCaseOrDepartment_Facility_IdAndUser_FirstNameContainingIgnoreCaseOrDepartment_Facility_IdAndUser_LastNameContainingIgnoreCaseOrDepartment_Facility_IdAndUser_EmailContainingIgnoreCase(
                            facilityId, query, facilityId, query, facilityId, query, facilityId, query
                    )
                    .forEach(userDepartment -> addUser(matches, userDepartment.getUser()));
        }

        List<User> distinct = List.copyOf(matches.values());
        int start = (int) Math.min(pageable.getOffset(), distinct.size());
        int end = Math.min(start + pageable.getPageSize(), distinct.size());
        return new PageImpl<>(distinct.subList(start, end), pageable, distinct.size());
    }

    private void addUser(Map<Long, User> matches, User user) {
        if (user == null || user.getId() == null) {
            return;
        }
        user.getLogin();
        matches.putIfAbsent(user.getId(), user);
    }

    @Transactional(readOnly = true)
    public Optional<Configuration> findOne(Long id) {
        LOG.debug("Request to get Configuration : {}", id);
        return configurationRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Page<Configuration> findAll(Pageable pageable) {
        LOG.debug("Request to get all Configuration ");
        return configurationRepository.findAll(pageable);
    }

    public Page<Configuration> quickSearch(String searchText, Pageable pageable) {
        if (searchText == null || searchText.isBlank()) {
            return Page.empty(pageable);
        }

        String text = searchText.trim();
        String normalizedKey = normalizeToEnumKey(text);

        ConfigurationKeys matchedKey = null;
        try {
            matchedKey = ConfigurationKeys.valueOf(normalizedKey);
        } catch (IllegalArgumentException ignored) {
            // not a valid enum key
        }

        if (matchedKey == null) {
            return configurationRepository.findByValueContainingIgnoreCaseOrDescriptionContainingIgnoreCase(text, text, pageable);
        }

        return configurationRepository.findByValueContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrKey(
                text,
                text,
                matchedKey,
                pageable
        );
    }

    private String normalizeToEnumKey(String input) {
        return input
                .trim()
                .toUpperCase()
                .replaceAll("\\s+", "_");
    }

    public Page<Configuration> filterByValueType(ConfigurationValueType valueType, Pageable pageable) {
        LOG.debug("Request to filterByValueType for Configuration ");
        return configurationRepository.findAllByValueType(valueType, pageable);
    }

    public Page<Configuration> filterByReferenceType(ConfigurationReferenceType referenceType, Pageable pageable) {
        LOG.debug("Request to filterByReferenceType for Configuration ");
        return configurationRepository.findAllByReferenceType(referenceType, pageable);
    }

    private Facility loadFacility(Long facilityId) {
        if (facilityId == null) {
            return null;
        }
        return facilityRepository.findById(facilityId)
                .orElseThrow(() -> new BadRequestAlertException(
                        "Facility not found with id " + facilityId,
                        "facility",
                        "notfound"
                ));
    }

    private Set<User> loadUsers(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new HashSet<>();
        }
        Set<Long> requested = userIds.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));
        if (requested.isEmpty()) {
            return new HashSet<>();
        }

        List<User> found = userRepository.findAllById(requested);
        if (found.size() != requested.size()) {
            Set<Long> foundIds = found.stream().map(User::getId).collect(Collectors.toSet());
            requested.removeAll(foundIds);
            throw new BadRequestAlertException(
                    "User not found with id " + requested,
                    "user",
                    "notfound"
            );
        }
        return new HashSet<>(found);
    }

    /**
     * Organization: no facility and no users.
     * Facility: facility, no users.
     * User: facility and at least one user.
     */
    private ConfigurationScope scopeOf(Facility facility, Set<User> users) {
        boolean hasUsers = users != null && !users.isEmpty();
        if (hasUsers && facility == null) {
            throw new BadRequestAlertException(
                    "User-level configuration requires a facility",
                    "configuration",
                    "facilityrequired"
            );
        }
        if (hasUsers) {
            return ConfigurationScope.USER;
        }
        if (facility != null) {
            return ConfigurationScope.FACILITY;
        }
        return ConfigurationScope.ORGANIZATION;
    }

    private void assertUnique(ConfigurationKeys key, Facility facility, Set<User> users, Long excludeId) {
        ConfigurationScope scope = scopeOf(facility, users);
        long excluded = excludeId == null ? NO_ROW_TO_EXCLUDE : excludeId;

        if (scope == ConfigurationScope.ORGANIZATION) {
            boolean duplicate = configurationRepository
                    .findByKeyAndScopeTypeAndFacilityIsNull(key, ConfigurationScope.ORGANIZATION)
                    .stream()
                    .anyMatch(row -> !Objects.equals(row.getId(), excludeId));
            if (duplicate) {
                throw duplicateKeyException(key, "organization");
            }
            return;
        }

        if (scope == ConfigurationScope.FACILITY) {
            boolean duplicate = configurationRepository
                    .findByKeyAndScopeTypeAndFacility_Id(key, ConfigurationScope.FACILITY, facility.getId())
                    .stream()
                    .anyMatch(row -> !Objects.equals(row.getId(), excludeId));
            if (duplicate) {
                throw duplicateKeyException(key, "facility " + facility.getId());
            }
            return;
        }

        Set<Long> userIds = users.stream().map(User::getId).collect(Collectors.toSet());
        Set<Long> clashes = configurationRepository
                .findByKeyAndScopeTypeAndFacility_IdAndIdNotAndUsers_IdIn(
                        key,
                        ConfigurationScope.USER,
                        facility.getId(),
                        excluded,
                        userIds
                )
                .stream()
                .filter(row -> row.getUsers() != null)
                .flatMap(row -> row.getUsers().stream())
                .map(User::getId)
                .filter(userIds::contains)
                .collect(Collectors.toSet());
        if (!clashes.isEmpty()) {
            throw duplicateKeyException(key, "user " + clashes + " at facility " + facility.getId());
        }
    }

    private BadRequestAlertException duplicateKeyException(ConfigurationKeys key, String scope) {
        return new BadRequestAlertException(
                "Duplicate configuration key '" + key + "' at " + scope + " level",
                "configuration",
                "duplicatekey"
        );
    }
}
