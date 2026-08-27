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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
                .build();

        NphiesPayer saved = nphiesPayerRepository.save(payer);
        syncTpas(saved, vm.tpaIds());
        return nphiesPayerRepository.findById(saved.getId()).orElse(saved);
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

        NphiesPayer saved = nphiesPayerRepository.save(existing);
        syncTpas(saved, vm.tpaIds());
        return nphiesPayerRepository.findById(saved.getId()).orElse(saved);
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
        return nphiesPayerRepository.findById(existing.getId()).orElse(existing);
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
        return nphiesPayerRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Page<NphiesPayer> findAll(Pageable pageable) {
        LOG.debug("[FIND ALL NPHIES PAYERS] Fetching all NPHIES payers pageable={}", pageable);

        Page<NphiesPayer> payersPage = nphiesPayerRepository.findAll(pageable);
        attachTpas(payersPage.getContent());

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
        attachTpas(page.getContent());
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
        attachTpas(page.getContent());
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
        attachTpas(page.getContent());
        return page;
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
                .filter(java.util.Objects::nonNull)
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
