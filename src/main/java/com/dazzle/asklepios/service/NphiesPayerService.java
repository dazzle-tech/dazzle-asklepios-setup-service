package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Country;
import com.dazzle.asklepios.domain.CountryDistrict;
import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.NphiesPayer;
import com.dazzle.asklepios.repository.CountryDistrictRepository;
import com.dazzle.asklepios.repository.CountryRepository;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.repository.NphiesPayerRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.nphiespayer.NphiesPayerSaveVM;
import com.dazzle.asklepios.web.rest.vm.nphiespayer.NphiesPayerUpdateVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.regex.Pattern;

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

    public NphiesPayerService(
            NphiesPayerRepository nphiesPayerRepository,
            FacilityRepository facilityRepository,
            CountryRepository countryRepository,
            CountryDistrictRepository countryDistrictRepository
    ) {
        this.nphiesPayerRepository = nphiesPayerRepository;
        this.facilityRepository = facilityRepository;
        this.countryRepository = countryRepository;
        this.countryDistrictRepository = countryDistrictRepository;
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

        return nphiesPayerRepository.save(payer);
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

        return nphiesPayerRepository.save(existing);
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

        return nphiesPayerRepository.findByNphiesIdContainingIgnoreCase(
                nphiesId,
                pageable
        );
    }

    @Transactional(readOnly = true)
    public Page<NphiesPayer> findByNameEn(String nameEn, Pageable pageable) {
        LOG.debug(
                "[FIND BY NAME EN] Searching NPHIES payers by nameEn='{}' pageable={}",
                nameEn,
                pageable
        );

        return nphiesPayerRepository.findByNameEnContainingIgnoreCase(
                nameEn,
                pageable
        );
    }

    @Transactional(readOnly = true)
    public Page<NphiesPayer> findByNameAr(String nameAr, Pageable pageable) {
        LOG.debug(
                "[FIND BY NAME AR] Searching NPHIES payers by nameAr='{}' pageable={}",
                nameAr,
                pageable
        );

        return nphiesPayerRepository.findByNameArContainingIgnoreCase(
                nameAr,
                pageable
        );
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
