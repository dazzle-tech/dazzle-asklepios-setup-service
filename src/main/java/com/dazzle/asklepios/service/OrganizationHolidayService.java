package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.OrganizationDefinition;
import com.dazzle.asklepios.domain.OrganizationHoliday;
import com.dazzle.asklepios.domain.enumeration.HolidayType;
import com.dazzle.asklepios.repository.OrganizationDefinitionRepository;
import com.dazzle.asklepios.repository.OrganizationHolidayRepository;
import com.dazzle.asklepios.service.dto.organizationHoliday.OrganizationHolidayCreateDTO;
import com.dazzle.asklepios.service.dto.organizationHoliday.OrganizationHolidayUpdateDTO;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrganizationHolidayService {

    private static final Logger LOG = LoggerFactory.getLogger(OrganizationHolidayService.class);

    private final OrganizationHolidayRepository organizationHolidayRepository;
    private final OrganizationDefinitionRepository organizationDefinitionRepository;

    public OrganizationHolidayService(
            OrganizationHolidayRepository organizationHolidayRepository,
            OrganizationDefinitionRepository organizationDefinitionRepository
    ) {
        this.organizationHolidayRepository = organizationHolidayRepository;
        this.organizationDefinitionRepository = organizationDefinitionRepository;
    }

    public OrganizationHoliday create(OrganizationHolidayCreateDTO dto) {
        LOG.debug("Request to create OrganizationHoliday : {}", dto);

        OrganizationDefinition organizationDefinition = getOrganizationDefinition(dto.organizationDefinitionId());

        validateHoliday(
                dto.startDate(),
                dto.endDate(),
                dto.allFacilities(),
                dto.facilityIds()
        );

        OrganizationHoliday entity = OrganizationHoliday.builder()
                .organizationDefinition(organizationDefinition)
                .name(dto.name())
                .holidayType(dto.holidayType())
                .startDate(dto.startDate())
                .endDate(dto.endDate())
                .reason(dto.reason())
                .isActive(dto.isActive())
                .allFacilities(dto.allFacilities())
                .facilityIds(normalizeFacilityIds(dto.facilityIds()))
                .recurring(dto.recurring())
                .build();

        return organizationHolidayRepository.save(entity);
    }

    public Optional<OrganizationHoliday> update(OrganizationHolidayUpdateDTO dto) {
        LOG.debug("Request to update OrganizationHoliday : {}", dto);

        return organizationHolidayRepository.findById(dto.id())
                .map(existing -> {
                    if (dto.name() != null) {
                        existing.setName(dto.name());
                    }
                    if (dto.holidayType() != null) {
                        existing.setHolidayType(dto.holidayType());
                    }
                    if (dto.startDate() != null) {
                        existing.setStartDate(dto.startDate());
                    }
                    if (dto.endDate() != null) {
                        existing.setEndDate(dto.endDate());
                    }
                    if (dto.reason() != null) {
                        existing.setReason(dto.reason());
                    }
                    if (dto.isActive() != null) {
                        existing.setIsActive(dto.isActive());
                    }
                    if (dto.allFacilities() != null) {
                        existing.setAllFacilities(dto.allFacilities());
                    }
                    if (dto.recurring() != null) {
                        existing.setRecurring(dto.recurring());
                    }

                    if (dto.facilityIds() != null) {
                        existing.setFacilityIds(normalizeFacilityIds(dto.facilityIds()));
                    }

                    if (Boolean.TRUE.equals(existing.getAllFacilities())) {
                        existing.setFacilityIds(null);
                    }

                    validateHoliday(
                            existing.getStartDate(),
                            existing.getEndDate(),
                            existing.getAllFacilities(),
                            existing.getFacilityIds()
                    );

                    return organizationHolidayRepository.save(existing);
                });
    }

    @Transactional(readOnly = true)
    public List<OrganizationHoliday> findAll() {
        LOG.debug("Request to get all OrganizationHolidays");
        return organizationHolidayRepository.findAllByOrderByStartDateDesc();
    }

    @Transactional(readOnly = true)
    public List<OrganizationHoliday> findAllActiveDesc() {
        LOG.debug("Request to get all active OrganizationHolidays DESC");
        return organizationHolidayRepository.findAllByIsActiveTrueOrderByStartDateDesc();
    }

    @Transactional(readOnly = true)
    public Optional<OrganizationHoliday> findOne(Long id) {
        LOG.debug("Request to get OrganizationHoliday : {}", id);
        return organizationHolidayRepository.findById(id);
    }


    @Transactional(readOnly = true)
    public List<OrganizationHoliday> search(String name, HolidayType holidayType, LocalDate startDate, LocalDate endDate, Boolean recurring, Boolean allFacilities, Long facilityId ) {
        LOG.debug("Request to search OrganizationHolidays name={}, holidayType={}, startDate={}, endDate={}, recurring={}, allFacilities={}, facilityId={}",
                name, holidayType, startDate, endDate, recurring, allFacilities, facilityId);

        Specification<OrganizationHoliday> organizationHolidaySpecification = Specification.where(null);

        if (name != null && !name.isBlank()) {
            String value = name.trim().toLowerCase();
            organizationHolidaySpecification = organizationHolidaySpecification.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + value + "%"));
        }

        if (holidayType != null) {
            organizationHolidaySpecification = organizationHolidaySpecification.and((root, query, cb) ->
                    cb.equal(root.get("holidayType"), holidayType));
        }

        if (startDate != null) {
            organizationHolidaySpecification = organizationHolidaySpecification.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("startDate"), startDate));
        }

        if (endDate != null) {
            organizationHolidaySpecification = organizationHolidaySpecification.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("endDate"), endDate));
        }

        if (recurring != null) {
            organizationHolidaySpecification = organizationHolidaySpecification.and((root, query, cb) ->
                    cb.equal(root.get("recurring"), recurring));
        }

        if (allFacilities != null) {
            organizationHolidaySpecification = organizationHolidaySpecification.and((root, query, cb) ->
                    cb.equal(root.get("allFacilities"), allFacilities));
        }

        if (facilityId != null) {
            String idAsText = String.valueOf(facilityId);

            organizationHolidaySpecification = organizationHolidaySpecification.and((root, query, cb) -> cb.or(
                    cb.equal(root.get("facilityIds"), idAsText),
                    cb.like(root.get("facilityIds"), idAsText + ",%"),
                    cb.like(root.get("facilityIds"), "%," + idAsText),
                    cb.like(root.get("facilityIds"), "%," + idAsText + ",%")
            ));
        }

        return organizationHolidayRepository.findAll(organizationHolidaySpecification).stream()
                .sorted(java.util.Comparator.comparing(OrganizationHoliday::getStartDate))
                .toList();
    }

    public Optional<OrganizationHoliday> toggleActive(Long id) {
        LOG.debug("Request to toggle active OrganizationHoliday : {}", id);

        return organizationHolidayRepository.findById(id)
                .map(existing -> {
                    existing.setIsActive(!Boolean.TRUE.equals(existing.getIsActive()));
                    return organizationHolidayRepository.save(existing);
                });
    }

    private OrganizationDefinition getOrganizationDefinition(Long id) {
        LOG.debug("Request to get OrganizationDefinition : {}", id);

        return organizationDefinitionRepository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException(
                        "organizationDefinition not found: " + id,
                        "OrganizationDefinition",
                        "notfound"
                ));
    }

    private void validateHoliday(
            java.time.LocalDate startDate,
            java.time.LocalDate endDate,
            Boolean allFacilities,
            String facilityIds
    ) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BadRequestAlertException(
                    "startDate cannot be after endDate",
                    "organizationHoliday",
                    "invalid_date_range"
            );
        }

        if (allFacilities == null) {
            throw new BadRequestAlertException(
                    "allFacilities is required",
                    "organizationHoliday",
                    "all_facilities_required"
            );
        }

        if (Boolean.FALSE.equals(allFacilities)
                && (facilityIds == null || facilityIds.isBlank())) {
            throw new BadRequestAlertException(
                    "facilityIds is required when allFacilities is false",
                    "organizationHoliday",
                    "facility_ids_required"
            );
        }

        if (Boolean.TRUE.equals(allFacilities)
                && facilityIds != null
                && !facilityIds.isBlank()) {
            throw new BadRequestAlertException(
                    "facilityIds must be empty when allFacilities is true",
                    "organizationHoliday",
                    "facility_ids_not_allowed"
            );
        }
    }

    private String normalizeFacilityIds(String facilityIds) {
        if (facilityIds == null || facilityIds.isBlank()) {
            return null;
        }

        return Arrays.stream(facilityIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .collect(Collectors.joining(","));
    }
}