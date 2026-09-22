package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Country;
import com.dazzle.asklepios.domain.CountryDistrict;
import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.NphiesPayer;
import com.dazzle.asklepios.domain.TpaDefinition;
import com.dazzle.asklepios.repository.CountryDistrictRepository;
import com.dazzle.asklepios.repository.CountryRepository;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.repository.NphiesPayerRepository;
import com.dazzle.asklepios.repository.TpaDefinitionRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.nphiespayer.NphiesPayerSaveVM;
import com.dazzle.asklepios.web.rest.vm.nphiespayer.NphiesPayerUpdateVM;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional
public class NphiesPayerService {

    private static final Logger LOG = LoggerFactory.getLogger(NphiesPayerService.class);
    private static final String ENTITY_NAME = "nphiesPayer";
    private static final Pattern WEBSITE_PATTERN = Pattern.compile("(?i)^https?://.+$");

    private final NphiesPayerRepository nphiesPayerRepository;
    private final FacilityRepository facilityRepository;
    private final CountryRepository countryRepository;
    private final CountryDistrictRepository countryDistrictRepository;
    private final TpaDefinitionRepository tpaDefinitionRepository;

    public NphiesPayerService(
            NphiesPayerRepository nphiesPayerRepository,
            FacilityRepository facilityRepository,
            CountryRepository countryRepository,
            CountryDistrictRepository countryDistrictRepository,
            TpaDefinitionRepository tpaDefinitionRepository
    ) {
        this.nphiesPayerRepository = nphiesPayerRepository;
        this.facilityRepository = facilityRepository;
        this.countryRepository = countryRepository;
        this.countryDistrictRepository = countryDistrictRepository;
        this.tpaDefinitionRepository = tpaDefinitionRepository;
    }

    public NphiesPayer create(NphiesPayerSaveVM vm) {
        LOG.debug("Create NPHIES Payer payload={}", vm);

        if (nphiesPayerRepository.existsByNphiesIdIgnoreCase(vm.nphiesId())) {
            throw new BadRequestAlertException(
                    "nphiesIdExists",
                    ENTITY_NAME,
                    "NPHIES ID already exists."
            );
        }

        validateWebsite(vm.website());
        validateCityRequiresCountry(vm.countryId(), vm.cityId());

        Facility facility = requireActiveFacility(vm.facilityId());
        Country country = resolveCountry(vm.countryId());
        CountryDistrict city = resolveCity(vm.cityId(), country);

        NphiesPayer payer = NphiesPayer.builder()
                .nphiesId(trim(vm.nphiesId()))
                .nameEn(trim(vm.nameEn()))
                .nameAr(blankToNull(vm.nameAr()))
                .shortName(blankToNull(vm.shortName()))
                .facility(facility)
                .insuranceAuthorityLicenseNo(trim(vm.insuranceAuthorityLicenseNo()))
                .commercialRegistrationNo(trim(vm.commercialRegistrationNo()))
                .vatRegistrationNo(trim(vm.vatRegistrationNo()))
                .unifiedNationalNo(blankToNull(vm.unifiedNationalNo()))
                .headOfficeAddress(blankToNull(vm.headOfficeAddress()))
                .country(country)
                .city(city)
                .postalCode(blankToNull(vm.postalCode()))
                .contactPerson(blankToNull(vm.contactPerson()))
                .phone(trim(vm.phone()))
                .mobile(blankToNull(vm.mobile()))
                .email(trim(vm.email()))
                .website(blankToNull(vm.website()))
                .isActive(vm.isActive() != null ? vm.isActive() : Boolean.TRUE)
                .approvalCoverageCompany(vm.approvalCoverageCompany())
                .build();

        NphiesPayer saved = nphiesPayerRepository.save(payer);
        syncTpas(saved, vm.tpaIds());
        syncChildCompanies(saved, vm.childCompanyIds());
        return reloadWithLinks(saved.getId(), saved);
    }

    public NphiesPayer update(NphiesPayerUpdateVM vm) {
        LOG.debug("Update NPHIES Payer payload={}", vm);

        NphiesPayer existing = nphiesPayerRepository.findById(vm.id())
                .orElseThrow(() -> new BadRequestAlertException(
                        "notFound",
                        ENTITY_NAME,
                        "NPHIES payer not found."
                ));

        if (!existing.getNphiesId().equalsIgnoreCase(vm.nphiesId())
                && nphiesPayerRepository.existsByNphiesIdIgnoreCaseAndIdNot(vm.nphiesId(), vm.id())) {
            throw new BadRequestAlertException(
                    "nphiesIdExists",
                    ENTITY_NAME,
                    "NPHIES ID already exists."
            );
        }

        validateWebsite(vm.website());
        validateCityRequiresCountry(vm.countryId(), vm.cityId());

        Long currentFacilityId = existing.getFacility() != null ? existing.getFacility().getId() : null;
        Facility facility = requireFacility(vm.facilityId(), currentFacilityId);
        Country country = resolveCountry(vm.countryId());
        CountryDistrict city = resolveCity(vm.cityId(), country);

        existing.setNphiesId(trim(vm.nphiesId()));
        existing.setNameEn(trim(vm.nameEn()));
        existing.setNameAr(blankToNull(vm.nameAr()));
        existing.setShortName(blankToNull(vm.shortName()));
        existing.setFacility(facility);
        existing.setInsuranceAuthorityLicenseNo(trim(vm.insuranceAuthorityLicenseNo()));
        existing.setCommercialRegistrationNo(trim(vm.commercialRegistrationNo()));
        existing.setVatRegistrationNo(trim(vm.vatRegistrationNo()));
        existing.setUnifiedNationalNo(blankToNull(vm.unifiedNationalNo()));
        existing.setHeadOfficeAddress(blankToNull(vm.headOfficeAddress()));
        existing.setCountry(country);
        existing.setCity(city);
        existing.setPostalCode(blankToNull(vm.postalCode()));
        existing.setContactPerson(blankToNull(vm.contactPerson()));
        existing.setPhone(trim(vm.phone()));
        existing.setMobile(blankToNull(vm.mobile()));
        existing.setEmail(trim(vm.email()));
        existing.setWebsite(blankToNull(vm.website()));
        existing.setIsActive(vm.isActive() != null ? vm.isActive() : existing.getIsActive());
        existing.setApprovalCoverageCompany(vm.approvalCoverageCompany());

        NphiesPayer saved = nphiesPayerRepository.save(existing);
        syncTpas(saved, vm.tpaIds());
        if (vm.childCompanyIds() != null) {
            syncChildCompanies(saved, vm.childCompanyIds());
        }
        return reloadWithLinks(saved.getId(), saved);
    }

    public NphiesPayer updateTpas(Long id, List<Long> tpaIds) {
        LOG.debug("Update NPHIES Payer TPA links id={} tpaIds={}", id, tpaIds);

        NphiesPayer existing = nphiesPayerRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(
                        "notFound",
                        ENTITY_NAME,
                        "NPHIES payer not found."
                ));
        syncTpas(existing, tpaIds);
        return reloadWithLinks(existing.getId(), existing);
    }

    public NphiesPayer updateChildCompanies(Long id, List<Long> childCompanyIds) {
        LOG.debug("Update NPHIES Payer child insurance links id={} childCompanyIds={}", id, childCompanyIds);

        NphiesPayer existing = nphiesPayerRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(
                        "notFound",
                        ENTITY_NAME,
                        "NPHIES payer not found."
                ));
        syncChildCompanies(existing, childCompanyIds);
        return reloadWithLinks(existing.getId(), existing);
    }

    public Optional<NphiesPayer> toggleIsActive(Long id) {
        LOG.debug("Toggle NPHIES Payer isActive id={}", id);
        return nphiesPayerRepository.findById(id).map(payer -> {
            payer.setIsActive(!Boolean.TRUE.equals(payer.getIsActive()));
            return nphiesPayerRepository.save(payer);
        });
    }

    @Transactional(readOnly = true)
    public Optional<NphiesPayer> findOne(Long id) {
        Optional<NphiesPayer> payer = nphiesPayerRepository.findById(id);
        payer.ifPresent(found -> attachLinks(List.of(found)));
        return payer;
    }

    @Transactional(readOnly = true)
    public List<NphiesPayer> findActive() {
        LOG.debug("Find active NPHIES Payers");
        return nphiesPayerRepository.findByIsActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<TpaDefinition> findAvailableTpas(Long payerId) {
        LOG.debug("Find active TPAs not linked to NPHIES Payer id={}", payerId);
        if (!nphiesPayerRepository.existsById(payerId)) {
            throw new BadRequestAlertException(
                    "notFound",
                    ENTITY_NAME,
                    "NPHIES payer not found."
            );
        }
        Set<Long> linkedIds = tpaDefinitionRepository.findByInsuranceCompanies_Id(payerId).stream()
                .map(TpaDefinition::getId)
                .collect(Collectors.toSet());
        return tpaDefinitionRepository.findByIsActiveTrue().stream()
                .filter(tpa -> !linkedIds.contains(tpa.getId()))
                .sorted(Comparator.comparing(TpaDefinition::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NphiesPayer> findAvailableChildCompanies(Long payerId) {
        LOG.debug("Find active insurance companies not linked under NPHIES Payer id={}", payerId);
        if (!nphiesPayerRepository.existsById(payerId)) {
            throw new BadRequestAlertException(
                    "notFound",
                    ENTITY_NAME,
                    "NPHIES payer not found."
            );
        }

        Set<Long> excludedIds = new HashSet<>();
        excludedIds.add(payerId);
        nphiesPayerRepository.findDistinctByParentCompanies_IdNotNull().stream()
                .map(NphiesPayer::getId)
                .forEach(excludedIds::add);

        Long current = payerId;
        Set<Long> visited = new HashSet<>();
        while (current != null && visited.add(current)) {
            List<NphiesPayer> parents = nphiesPayerRepository.findByChildCompanies_Id(current);
            if (parents.isEmpty()) {
                break;
            }
            current = parents.get(0).getId();
            excludedIds.add(current);
        }

        return nphiesPayerRepository.findByIsActiveTrue().stream()
                .filter(payer -> payer.getId() != null && !excludedIds.contains(payer.getId()))
                .sorted(Comparator.comparing(NphiesPayer::getNameEn, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<NphiesPayer> findAll(Pageable pageable) {
        LOG.debug("[FIND ALL NPHIES PAYERS] Fetching all NPHIES payers pageable={}", pageable);

        Page<NphiesPayer> payersPage = nphiesPayerRepository.findAll(pageable);
        attachLinks(payersPage.getContent());

        LOG.debug(
                "[FIND ALL NPHIES PAYERS] Retrieved count={} pageNumber={} pageSize={} totalElements={} totalPages={}",
                payersPage.getNumberOfElements(),
                payersPage.getNumber(),
                payersPage.getSize(),
                payersPage.getTotalElements(),
                payersPage.getTotalPages()
        );

        return payersPage;
    }

    @Transactional(readOnly = true)
    public Page<NphiesPayer> findByNphiesId(String nphiesId, Pageable pageable) {
        LOG.debug(
                "[FIND BY NPHIES ID] Searching NPHIES payers by nphiesId='{}' pageable={}",
                nphiesId,
                pageable
        );

        Page<NphiesPayer> page = nphiesPayerRepository.findByNphiesIdContainingIgnoreCase(
                nphiesId,
                pageable
        );
        attachLinks(page.getContent());
        return page;
    }

    @Transactional(readOnly = true)
    public Page<NphiesPayer> findByNameEn(String nameEn, Pageable pageable) {
        LOG.debug(
                "[FIND BY NAME EN] Searching NPHIES payers by nameEn='{}' pageable={}",
                nameEn,
                pageable
        );

        Page<NphiesPayer> page = nphiesPayerRepository.findByNameEnContainingIgnoreCase(
                nameEn,
                pageable
        );
        attachLinks(page.getContent());
        return page;
    }

    @Transactional(readOnly = true)
    public Page<NphiesPayer> findByNameAr(String nameAr, Pageable pageable) {
        LOG.debug(
                "[FIND BY NAME AR] Searching NPHIES payers by nameAr='{}' pageable={}",
                nameAr,
                pageable
        );

        Page<NphiesPayer> page = nphiesPayerRepository.findByNameArContainingIgnoreCase(
                nameAr,
                pageable
        );
        attachLinks(page.getContent());
        return page;
    }

    private NphiesPayer reloadWithLinks(Long id, NphiesPayer fallback) {
        NphiesPayer loaded = nphiesPayerRepository.findById(id).orElse(fallback);
        attachLinks(List.of(loaded));
        return loaded;
    }

    private void attachLinks(List<NphiesPayer> payers) {
        attachTpas(payers);
        attachChildCompanies(payers);
        attachParentCompanies(payers);
    }

    private void attachTpas(List<NphiesPayer> payers) {
        if (payers == null || payers.isEmpty()) {
            return;
        }

        Set<Long> payerIds = payers.stream()
                .map(NphiesPayer::getId)
                .collect(Collectors.toCollection(HashSet::new));
        List<TpaDefinition> linkedTpas = tpaDefinitionRepository.findByInsuranceCompanies_IdIn(payerIds);
        Map<Long, Set<TpaDefinition>> tpasByPayerId = new HashMap<>();

        for (TpaDefinition tpa : linkedTpas) {
            if (tpa.getInsuranceCompanies() == null) {
                continue;
            }
            for (NphiesPayer linkedPayer : tpa.getInsuranceCompanies()) {
                if (linkedPayer.getId() != null && payerIds.contains(linkedPayer.getId())) {
                    tpasByPayerId
                            .computeIfAbsent(linkedPayer.getId(), ignored -> new HashSet<>())
                            .add(tpa);
                }
            }
        }

        for (NphiesPayer payer : payers) {
            payer.setTpas(tpasByPayerId.getOrDefault(payer.getId(), new HashSet<>()));
        }
    }

    private void syncTpas(NphiesPayer payer, List<Long> tpaIds) {
        Set<Long> desiredIds = uniqueIds(tpaIds);

        List<TpaDefinition> currentlyLinked = payer.getId() == null
                ? List.of()
                : tpaDefinitionRepository.findByInsuranceCompanies_Id(payer.getId());
        Set<Long> currentIds = currentlyLinked.stream()
                .map(TpaDefinition::getId)
                .collect(Collectors.toSet());

        List<TpaDefinition> selected = desiredIds.isEmpty()
                ? List.of()
                : tpaDefinitionRepository.findByIdIn(desiredIds);
        if (selected.size() != desiredIds.size()) {
            throw new BadRequestAlertException(
                    "tpaNotFound",
                    ENTITY_NAME,
                    "One or more TPAs were not found."
            );
        }

        for (TpaDefinition tpa : currentlyLinked) {
            if (!desiredIds.contains(tpa.getId()) && tpa.getInsuranceCompanies() != null) {
                tpa.getInsuranceCompanies().removeIf(linked -> linked.getId().equals(payer.getId()));
            }
        }

        for (TpaDefinition tpa : selected) {
            if (!currentIds.contains(tpa.getId())) {
                if (!Boolean.TRUE.equals(tpa.getIsActive())) {
                    throw new BadRequestAlertException(
                            "inactiveTpaCannotLink",
                            ENTITY_NAME,
                            "Inactive TPA cannot be linked to insurance companies."
                    );
                }
                if (tpa.getInsuranceCompanies() == null) {
                    tpa.setInsuranceCompanies(new HashSet<>());
                }
                boolean alreadyLinked = tpa.getInsuranceCompanies().stream()
                        .anyMatch(linked -> payer.getId().equals(linked.getId()));
                if (!alreadyLinked) {
                    tpa.getInsuranceCompanies().add(payer);
                }
            }
        }

        tpaDefinitionRepository.saveAll(currentlyLinked);
        tpaDefinitionRepository.saveAll(selected);
        payer.setTpas(new HashSet<>(selected));
    }

    private void attachChildCompanies(List<NphiesPayer> payers) {
        if (payers == null || payers.isEmpty()) {
            return;
        }

        Set<Long> payerIds = payers.stream()
                .map(NphiesPayer::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));
        if (payerIds.isEmpty()) {
            return;
        }

        Map<Long, Set<NphiesPayer>> childrenByParentId = new HashMap<>();
        for (NphiesPayer loaded : nphiesPayerRepository.findDistinctByIdIn(payerIds)) {
            Set<NphiesPayer> children = loaded.getChildCompanies() == null
                    ? new HashSet<>()
                    : new HashSet<>(loaded.getChildCompanies());
            childrenByParentId.put(loaded.getId(), children);
        }

        for (NphiesPayer payer : payers) {
            payer.setChildCompanies(childrenByParentId.getOrDefault(payer.getId(), new HashSet<>()));
        }
    }

    private void attachParentCompanies(List<NphiesPayer> payers) {
        if (payers == null || payers.isEmpty()) {
            return;
        }

        Set<Long> payerIds = payers.stream()
                .map(NphiesPayer::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));
        if (payerIds.isEmpty()) {
            return;
        }

        Map<Long, NphiesPayer> parentByChildId = new HashMap<>();
        for (NphiesPayer parent : nphiesPayerRepository.findByChildCompanies_IdIn(payerIds)) {
            if (parent.getChildCompanies() == null) {
                continue;
            }
            Hibernate.initialize(parent.getChildCompanies());
            for (NphiesPayer child : parent.getChildCompanies()) {
                if (child.getId() != null && payerIds.contains(child.getId())) {
                    parentByChildId.put(child.getId(), parent);
                }
            }
        }

        for (NphiesPayer payer : payers) {
            NphiesPayer parent = parentByChildId.get(payer.getId());
            payer.setParentCompanies(parent == null ? new HashSet<>() : new HashSet<>(Set.of(parent)));
        }
    }

    private void syncChildCompanies(NphiesPayer payer, List<Long> childCompanyIds) {
        Set<Long> desiredIds = uniqueIds(childCompanyIds);
        if (payer.getId() != null && desiredIds.contains(payer.getId())) {
            throw new BadRequestAlertException(
                    "cannotLinkSelf",
                    ENTITY_NAME,
                    "An insurance company cannot be linked to itself."
            );
        }

        List<NphiesPayer> currentlyLinked = payer.getId() == null
                ? List.of()
                : nphiesPayerRepository.findByParentCompanies_Id(payer.getId());
        Set<Long> currentIds = currentlyLinked.stream()
                .map(NphiesPayer::getId)
                .collect(Collectors.toSet());

        if (desiredIds.equals(currentIds)) {
            payer.setChildCompanies(new HashSet<>(currentlyLinked));
            return;
        }

        List<NphiesPayer> selected = desiredIds.isEmpty()
                ? List.of()
                : nphiesPayerRepository.findByIdIn(desiredIds);
        if (selected.size() != desiredIds.size()) {
            throw new BadRequestAlertException(
                    "childCompanyNotFound",
                    ENTITY_NAME,
                    "One or more insurance companies were not found."
            );
        }

        for (NphiesPayer child : selected) {
            if (currentIds.contains(child.getId())) {
                continue;
            }
            if (!Boolean.TRUE.equals(child.getIsActive())) {
                throw new BadRequestAlertException(
                        "inactiveChildCompanyCannotLink",
                        ENTITY_NAME,
                        "Inactive insurance companies cannot be linked as children."
                );
            }
            List<NphiesPayer> existingParents = nphiesPayerRepository.findByChildCompanies_Id(child.getId());
            boolean hasOtherParent = existingParents.stream()
                    .anyMatch(parent -> parent.getId() != null && !parent.getId().equals(payer.getId()));
            if (hasOtherParent) {
                throw new BadRequestAlertException(
                        "childCompanyAlreadyLinked",
                        ENTITY_NAME,
                        "Insurance company is already linked under another parent."
                );
            }
            if (isAncestor(child.getId(), payer.getId())) {
                throw new BadRequestAlertException(
                        "childCompanyCycle",
                        ENTITY_NAME,
                        "An insurance company cannot be linked under one of its children."
                );
            }
        }

        if (payer.getChildCompanies() == null) {
            payer.setChildCompanies(new HashSet<>());
        } else {
            Hibernate.initialize(payer.getChildCompanies());
            payer.getChildCompanies().clear();
        }
        payer.getChildCompanies().addAll(selected);
        nphiesPayerRepository.save(payer);
        payer.setChildCompanies(new HashSet<>(selected));
    }

    private boolean isAncestor(Long possibleAncestorId, Long payerId) {
        if (possibleAncestorId == null || payerId == null) {
            return false;
        }
        Long current = payerId;
        Set<Long> visited = new HashSet<>();
        while (current != null && visited.add(current)) {
            List<NphiesPayer> parents = nphiesPayerRepository.findByChildCompanies_Id(current);
            if (parents.isEmpty()) {
                return false;
            }
            Long parentId = parents.get(0).getId();
            if (possibleAncestorId.equals(parentId)) {
                return true;
            }
            current = parentId;
        }
        return false;
    }

    private Facility requireActiveFacility(Long facilityId) {
        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new BadRequestAlertException(
                        "facilityNotFound",
                        ENTITY_NAME,
                        "Facility not found."
                ));
        if (!Boolean.TRUE.equals(facility.getIsActive())) {
            throw new BadRequestAlertException(
                    "inactiveFacility",
                    ENTITY_NAME,
                    "Facility must be active."
            );
        }
        return facility;
    }

    private Facility requireFacility(Long facilityId, Long currentFacilityId) {
        if (currentFacilityId != null && currentFacilityId.equals(facilityId)) {
            return facilityRepository.findById(facilityId)
                    .orElseThrow(() -> new BadRequestAlertException(
                            "facilityNotFound",
                            ENTITY_NAME,
                            "Facility not found."
                    ));
        }
        return requireActiveFacility(facilityId);
    }

    private Country resolveCountry(Long countryId) {
        if (countryId == null) {
            return null;
        }
        return countryRepository.findById(countryId)
                .orElseThrow(() -> new BadRequestAlertException(
                        "countryNotFound",
                        ENTITY_NAME,
                        "Country not found."
                ));
    }

    private CountryDistrict resolveCity(Long cityId, Country country) {
        if (cityId == null) {
            return null;
        }
        if (country == null) {
            throw new BadRequestAlertException(
                    "countryRequiredForCity",
                    ENTITY_NAME,
                    "Country is required when city is selected."
            );
        }
        CountryDistrict city = countryDistrictRepository.findById(cityId)
                .orElseThrow(() -> new BadRequestAlertException(
                        "cityNotFound",
                        ENTITY_NAME,
                        "City not found."
                ));
        Long cityCountryId = city.getCountry() != null ? city.getCountry().getId() : null;
        if (cityCountryId == null || !cityCountryId.equals(country.getId())) {
            throw new BadRequestAlertException(
                    "cityCountryMismatch",
                    ENTITY_NAME,
                    "City does not belong to the selected country."
            );
        }
        return city;
    }

    private void validateCityRequiresCountry(Long countryId, Long cityId) {
        if (cityId != null && countryId == null) {
            throw new BadRequestAlertException(
                    "countryRequiredForCity",
                    ENTITY_NAME,
                    "Country is required when city is selected."
            );
        }
    }

    private void validateWebsite(String website) {
        if (website == null || website.isBlank()) {
            return;
        }
        if (!WEBSITE_PATTERN.matcher(website.trim()).matches()) {
            throw new BadRequestAlertException(
                    "invalidWebsite",
                    ENTITY_NAME,
                    "Website must be a valid URL."
            );
        }
    }

    private Set<Long> uniqueIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Set.of();
        }
        return ids.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
