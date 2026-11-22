package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.ChildGrowthSetup;
import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.User;
import com.dazzle.asklepios.domain.UserDepartment;
import com.dazzle.asklepios.domain.UserRole;
import com.dazzle.asklepios.domain.childgrowth.ChildGrowthDocument;
import com.dazzle.asklepios.domain.enumeration.ChildGrowthAlertRecipient;
import com.dazzle.asklepios.domain.enumeration.ChildGrowthChartKind;
import com.dazzle.asklepios.domain.enumeration.ChildGrowthIndicator;
import com.dazzle.asklepios.domain.enumeration.ConfigurationScope;
import com.dazzle.asklepios.repository.ChildGrowthSetupRepository;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.repository.UserDepartmentRepository;
import com.dazzle.asklepios.repository.UserRepository;
import com.dazzle.asklepios.repository.UserRoleRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import com.dazzle.asklepios.web.rest.vm.childgrowth.ChildGrowthSetupResponseVM;
import com.dazzle.asklepios.web.rest.vm.childgrowth.ChildGrowthSetupSaveVM;
import com.dazzle.asklepios.web.rest.vm.childgrowth.ChildGrowthUserOptionVM;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class ChildGrowthSetupService {

    private static final String ENTITY_NAME = "childGrowthSetup";
    private static final Logger LOG = LoggerFactory.getLogger(ChildGrowthSetupService.class);
    private static final List<String> BAND_ORDER = List.of(
            "action-low", "watch-low", "normal", "watch-high", "action-high"
    );
    private static final Map<String, ChildGrowthChartKind> CHART_KINDS = Map.of(
            "who", ChildGrowthChartKind.DEFAULT,
            "cdc", ChildGrowthChartKind.DEFAULT,
            "preterm", ChildGrowthChartKind.PRETERM,
            "down", ChildGrowthChartKind.DIAGNOSIS,
            "custom", ChildGrowthChartKind.CUSTOM
    );

    private static final ObjectMapper DOCUMENTS = new ObjectMapper();

    private final ChildGrowthSetupRepository repository;
    private final FacilityRepository facilityRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserDepartmentRepository userDepartmentRepository;

    public ChildGrowthSetupService(
            ChildGrowthSetupRepository repository,
            FacilityRepository facilityRepository,
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            UserDepartmentRepository userDepartmentRepository
    ) {
        this.repository = repository;
        this.facilityRepository = facilityRepository;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.userDepartmentRepository = userDepartmentRepository;
    }

    @Transactional(readOnly = true)
    public List<ChildGrowthUserOptionVM> activeUsers(Long facilityId) {
        Facility facility = facility(facilityId);
        Map<Long, User> matches = new LinkedHashMap<>();
        for (UserRole userRole : userRoleRepository.findByRole_FacilityId(facility.getId())) {
            addActiveUser(matches, userRole.getUser());
        }
        for (UserDepartment link : userDepartmentRepository.findByDepartment_Facility_Id(facility.getId())) {
            if (Boolean.TRUE.equals(link.getIsActive())) {
                addActiveUser(matches, link.getUser());
            }
        }
        return matches.values().stream()
                .sorted(Comparator.comparing(this::displayName, String.CASE_INSENSITIVE_ORDER))
                .map(ChildGrowthUserOptionVM::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ChildGrowthSetupResponseVM get(ConfigurationScope scope, Long facilityId, List<Long> userIds) {
        List<Long> ids = userIdsOf(scope, userIds);
        if (scope != ConfigurationScope.USER) {
            ScopeTarget target = resolveShared(scope, facilityId, ids);
            return find(target)
                    .map(ChildGrowthSetupResponseVM::fromEntity)
                    .orElseGet(() -> ChildGrowthSetupResponseVM.unsaved(scope, facilityId, ids));
        }
        Facility facility = facility(facilityId);
        List<User> users = usersAtFacility(facility, ids);
        List<ChildGrowthSetup> rows = repository.findByScopeTypeAndFacility_IdAndUser_IdIn(
                ConfigurationScope.USER, facility.getId(), ids);
        if (rows.size() == users.size() && sameDocument(rows)) {
            Long id = users.size() == 1 ? rows.get(0).getId() : null;
            return ChildGrowthSetupResponseVM.saved(id, scope, facility.getId(), ids, rows.get(0).getDocument());
        }
        return ChildGrowthSetupResponseVM.unsaved(scope, facility.getId(), ids);
    }

    public ChildGrowthSetupResponseVM save(ChildGrowthSetupSaveVM vm) {
        List<Long> ids = userIdsOf(vm.scope(), vm.userIds());
        ChildGrowthDocument document = toDocument(vm);
        validate(document);
        if (vm.scope() != ConfigurationScope.USER) {
            ScopeTarget target = resolveShared(vm.scope(), vm.facilityId(), ids);
            ChildGrowthSetup entity = find(target).orElseGet(() -> ChildGrowthSetup.builder()
                    .scopeType(target.scope())
                    .facility(target.facility())
                    .build());
            entity.setDocument(document);
            ChildGrowthSetup saved = repository.save(entity);
            LOG.info("[SAVE] ChildGrowthSetup id={} scope={} facilityId={}",
                    saved.getId(), target.scope(), idOf(target.facility()));
            return ChildGrowthSetupResponseVM.fromEntity(saved);
        }
        Facility facility = facility(vm.facilityId());
        List<User> users = usersAtFacility(facility, ids);
        Long singleId = null;
        for (User user : users) {
            ChildGrowthSetup entity = repository
                    .findByScopeTypeAndFacility_IdAndUser_Id(ConfigurationScope.USER, facility.getId(), user.getId())
                    .orElseGet(() -> ChildGrowthSetup.builder()
                            .scopeType(ConfigurationScope.USER)
                            .facility(facility)
                            .user(user)
                            .build());
            entity.setDocument(document);
            ChildGrowthSetup saved = repository.save(entity);
            singleId = saved.getId();
        }
        LOG.info("[SAVE] ChildGrowthSetup scope=USER facilityId={} userIds={}", facility.getId(), ids);
        return ChildGrowthSetupResponseVM.saved(
                users.size() == 1 ? singleId : null,
                ConfigurationScope.USER,
                facility.getId(),
                ids,
                document
        );
    }

    private ChildGrowthDocument toDocument(ChildGrowthSetupSaveVM vm) {
        Map<ChildGrowthIndicator, List<ChildGrowthDocument.Band>> bands = new EnumMap<>(ChildGrowthIndicator.class);
        if (vm.bandsByIndicator() != null) {
            vm.bandsByIndicator().forEach((key, value) -> {
                ChildGrowthIndicator indicator;
                try {
                    indicator = ChildGrowthIndicator.valueOf(key);
                } catch (IllegalArgumentException ex) {
                    throw bad("Unknown chart indicator: " + key, "indicator.invalid");
                }
                bands.put(indicator, value);
            });
        }
        return ChildGrowthDocument.builder()
                .charts(vm.charts())
                .rules(vm.rules())
                .bandsByIndicator(bands)
                .limits(vm.limits())
                .alerts(vm.alerts())
                .build();
    }

    private void validate(ChildGrowthDocument document) {
        if (document.getCharts() == null || document.getCharts().size() != CHART_KINDS.size()) {
            throw bad("The chart set must include WHO, CDC, preterm, Down syndrome, and custom", "charts.required");
        }
        Set<String> seen = new HashSet<>();
        for (ChildGrowthDocument.Chart chart : document.getCharts()) {
            if (chart == null || chart.getId() == null || !CHART_KINDS.containsKey(chart.getId())) {
                throw bad("Unknown chart", "chart.invalid");
            }
            if (!seen.add(chart.getId())) {
                throw bad("Chart " + chart.getId() + " is listed more than once", "chart.duplicate");
            }
            if (chart.getName() == null || chart.getName().isBlank()
                    || chart.getSex() == null || chart.getUnit() == null || chart.getEnabled() == null) {
                throw bad("Chart " + chart.getId() + " is incomplete", "chart.invalid");
            }
            if (chart.getKind() != CHART_KINDS.get(chart.getId())) {
                throw bad(chart.getName() + " has the wrong chart kind", "chart.kind");
            }
            if (Boolean.TRUE.equals(chart.getEnabled())) {
                validateAge(chart);
            }
        }
        if (seen.size() != CHART_KINDS.size()) {
            throw bad("The chart set must include WHO, CDC, preterm, Down syndrome, and custom", "charts.required");
        }
        if (document.getRules() == null
                || document.getRules().getMissingSex() == null
                || document.getRules().getAllowOverride() == null
                || document.getRules().getShowInParentPortal() == null) {
            throw bad("Selection rules are incomplete", "rules.invalid");
        }
        if (document.getRules().getCorrectedAgeUntilMonths() == null
                || document.getRules().getCorrectedAgeUntilMonths() < 0) {
            throw bad("Corrected age must be a whole number of months, 0 or more", "correctedAge.invalid");
        }
        validateBands(document.getBandsByIndicator());
        validateLimits(document.getLimits());
        validateAlerts(document.getAlerts());
    }

    private void validateAge(ChildGrowthDocument.Chart chart) {
        Integer from = chart.getAgeFrom();
        Integer to = chart.getAgeTo();
        if (from == null || from < 0) {
            throw bad(chart.getName() + ": Age from must be a whole number, 0 or more", "ageFrom.invalid");
        }
        if (to == null || to < 0) {
            throw bad(chart.getName() + ": Age to must be a whole number, 0 or more", "ageTo.invalid");
        }
        if (to <= from) {
            throw bad(chart.getName() + ": Age to must be greater than Age from", "ageTo.invalid");
        }
    }

    private void validateBands(Map<ChildGrowthIndicator, List<ChildGrowthDocument.Band>> bandsByIndicator) {
        if (bandsByIndicator == null) {
            throw bad("Status bands are required", "bands.required");
        }
        for (ChildGrowthIndicator indicator : ChildGrowthIndicator.values()) {
            List<ChildGrowthDocument.Band> bands = bandsByIndicator.get(indicator);
            if (bands == null || bands.size() != BAND_ORDER.size()) {
                throw bad(indicator.getLabel() + ": the five status bands are required", "bands.required");
            }
            Map<String, ChildGrowthDocument.Band> byId = new java.util.LinkedHashMap<>();
            for (ChildGrowthDocument.Band band : bands) {
                if (band == null || !BAND_ORDER.contains(band.getId())) {
                    throw bad(indicator.getLabel() + ": unknown status band", "band.invalid");
                }
                if (byId.put(band.getId(), band) != null) {
                    throw bad(indicator.getLabel() + ": " + band.getLabel() + " is listed more than once", "band.duplicate");
                }
                if (band.getMin() == null || band.getMax() == null || band.getMin().compareTo(band.getMax()) >= 0) {
                    throw bad(indicator.getLabel() + ": " + band.getLabel() + " Min must be less than Max", "band.range");
                }
                validateAlerted(indicator, band);
            }
            if (byId.size() != BAND_ORDER.size()) {
                throw bad(indicator.getLabel() + ": the five status bands are required", "bands.required");
            }
            List<ChildGrowthDocument.Band> sorted = byId.values().stream()
                    .sorted((left, right) -> left.getMin().compareTo(right.getMin()))
                    .toList();
            for (int i = 1; i < sorted.size(); i++) {
                ChildGrowthDocument.Band previous = sorted.get(i - 1);
                ChildGrowthDocument.Band current = sorted.get(i);
                if (current.getMin().compareTo(previous.getMax()) < 0) {
                    throw bad(
                            indicator.getLabel() + ": " + current.getLabel() + " overlaps " + previous.getLabel(),
                            "band.overlap"
                    );
                }
            }
        }
    }

    private void validateAlerted(ChildGrowthIndicator indicator, ChildGrowthDocument.Band band) {
        List<ChildGrowthAlertRecipient> alerted = band.getAlerted();
        if (alerted == null) {
            throw bad(indicator.getLabel() + ": " + band.getLabel() + " who is alerted is required", "alerted.required");
        }
        Set<ChildGrowthAlertRecipient> seen = new HashSet<>();
        for (ChildGrowthAlertRecipient recipient : alerted) {
            if (recipient == null || !seen.add(recipient)) {
                throw bad(
                        indicator.getLabel() + ": " + band.getLabel() + " lists the same recipient more than once",
                        "alerted.duplicate"
                );
            }
        }
    }

    private void validateLimits(ChildGrowthDocument.Limits limits) {
        if (limits == null) {
            throw bad("Entry limits are required", "limits.required");
        }
        checkPair("Weight", limits.getWeightMinKg(), limits.getWeightMaxKg());
        checkPair("Length", limits.getLengthMinCm(), limits.getLengthMaxCm());
        checkPair("Head circumference", limits.getHeadMinCm(), limits.getHeadMaxCm());
    }

    private void checkPair(String label, BigDecimal min, BigDecimal max) {
        if (min == null || max == null) {
            throw bad(label + ": min and max are required", "limits.required");
        }
        if (min.signum() < 0 || max.signum() < 0) {
            throw bad(label + ": limits must be 0 or more", "limits.invalid");
        }
        if (min.compareTo(max) >= 0) {
            throw bad(label + ": min must be less than max", "limits.invalid");
        }
    }

    private void validateAlerts(ChildGrowthDocument.Alerts alerts) {
        if (alerts == null || alerts.getConsecutiveVisits() == null || alerts.getConsecutiveVisits() < 1) {
            throw bad("Consecutive visits must be a whole number, 1 or more", "alerts.invalid");
        }
        if (alerts.getChannelsCrossed() == null || alerts.getChannelsCrossed() < 1) {
            throw bad("Percentile channels crossed must be a whole number, 1 or more", "alerts.invalid");
        }
    }

    private List<Long> userIdsOf(ConfigurationScope scope, List<Long> userIds) {
        if (scope == null) {
            throw bad("Scope is required", "scope.required");
        }
        List<Long> ids = new ArrayList<>();
        if (userIds != null) {
            for (Long userId : userIds) {
                if (userId == null) {
                    throw bad("Select at least one active user", "user.required");
                }
                if (!ids.contains(userId)) {
                    ids.add(userId);
                }
            }
        }
        if (scope != ConfigurationScope.USER) {
            if (!ids.isEmpty()) {
                throw bad(
                        scope == ConfigurationScope.ORGANIZATION
                                ? "Organization setup does not take a facility or user"
                                : "Facility setup does not take a user",
                        scope == ConfigurationScope.ORGANIZATION ? "scope.organization" : "scope.facility"
                );
            }
            return List.of();
        }
        if (ids.isEmpty()) {
            throw bad("Select at least one active user", "user.required");
        }
        return ids;
    }

    private ScopeTarget resolveShared(ConfigurationScope scope, Long facilityId, List<Long> userIds) {
        if (!userIds.isEmpty()) {
            throw bad("Facility setup does not take a user", "scope.facility");
        }
        return switch (scope) {
            case ORGANIZATION -> {
                if (facilityId != null) {
                    throw bad("Organization setup does not take a facility or user", "scope.organization");
                }
                yield new ScopeTarget(scope, null);
            }
            case FACILITY -> {
                if (facilityId == null) {
                    throw bad("Facility is required", "facility.required");
                }
                yield new ScopeTarget(scope, facility(facilityId));
            }
            case USER -> throw bad("Select at least one active user", "user.required");
        };
    }

    private List<User> usersAtFacility(Facility facility, List<Long> userIds) {
        List<User> users = new ArrayList<>();
        for (Long userId : userIds) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundAlertException(
                            "User not found: " + userId, ENTITY_NAME, "user.notfound"));
            if (!user.isActivated() || !assignedToFacility(facility.getId(), userId)) {
                throw bad(displayName(user) + " is not an active user at this facility", "user.inactive");
            }
            users.add(user);
        }
        return users;
    }

    private boolean assignedToFacility(Long facilityId, Long userId) {
        return userRoleRepository.existsByRole_FacilityIdAndUser_Id(facilityId, userId)
                || userDepartmentRepository.existsByDepartment_Facility_IdAndUser_IdAndIsActiveTrue(facilityId, userId);
    }

    private void addActiveUser(Map<Long, User> matches, User user) {
        if (user == null || user.getId() == null || !user.isActivated()) {
            return;
        }
        user.getLogin();
        matches.putIfAbsent(user.getId(), user);
    }

    private String displayName(User user) {
        String name = ((user.getFirstName() == null ? "" : user.getFirstName()) + " "
                + (user.getLastName() == null ? "" : user.getLastName())).trim();
        if (!name.isEmpty()) {
            return name;
        }
        return user.getLogin() == null ? "User " + user.getId() : user.getLogin();
    }

    private boolean sameDocument(List<ChildGrowthSetup> rows) {
        if (rows.isEmpty() || rows.get(0).getDocument() == null) {
            return false;
        }
        String first = json(rows.get(0).getDocument());
        for (int i = 1; i < rows.size(); i++) {
            if (!first.equals(json(rows.get(i).getDocument()))) {
                return false;
            }
        }
        return true;
    }

    private String json(ChildGrowthDocument document) {
        try {
            return DOCUMENTS.writeValueAsString(document);
        } catch (JsonProcessingException ex) {
            return "";
        }
    }

    private Facility facility(Long facilityId) {
        if (facilityId == null) {
            throw bad("Facility is required", "facility.required");
        }
        return facilityRepository.findById(facilityId)
                .orElseThrow(() -> new NotFoundAlertException(
                        "Facility not found: " + facilityId, ENTITY_NAME, "facility.notfound"));
    }

    private java.util.Optional<ChildGrowthSetup> find(ScopeTarget target) {
        return switch (target.scope()) {
            case ORGANIZATION -> repository.findByScopeTypeAndFacilityIsNullAndUserIsNull(target.scope());
            case FACILITY -> repository.findByScopeTypeAndFacility_IdAndUserIsNull(
                    target.scope(), target.facility().getId());
            case USER -> java.util.Optional.empty();
        };
    }

    private static Long idOf(Facility facility) {
        return facility == null ? null : facility.getId();
    }

    private static BadRequestAlertException bad(String message, String key) {
        return new BadRequestAlertException(message, ENTITY_NAME, key);
    }

    private record ScopeTarget(ConfigurationScope scope, Facility facility) {
    }
}
