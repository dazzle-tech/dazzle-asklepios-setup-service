package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.OrganizationHoliday;
import com.dazzle.asklepios.domain.enumeration.HolidayType;
import com.dazzle.asklepios.security.AuthoritiesConstants;
import com.dazzle.asklepios.service.OrganizationHolidayService;
import com.dazzle.asklepios.service.dto.organizationHoliday.OrganizationHolidayCreateDTO;
import com.dazzle.asklepios.service.dto.organizationHoliday.OrganizationHolidayUpdateDTO;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.OrganizationHolidayResponseVM;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
public class OrganizationHolidayController {

    private static final Logger LOG = LoggerFactory.getLogger(OrganizationHolidayController.class);

    private final OrganizationHolidayService organizationHolidayService;

    public OrganizationHolidayController(OrganizationHolidayService organizationHolidayService) {
        this.organizationHolidayService = organizationHolidayService;
    }

    /**
     * {@code POST /organization-holiday} : Create a new OrganizationHoliday.
     */
    @PostMapping("/organization-holiday")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<OrganizationHolidayResponseVM> createOrganizationHoliday(
            @Valid @RequestBody OrganizationHolidayCreateDTO dto
    ) {
        LOG.debug("REST request to create OrganizationHoliday : {}", dto);

        OrganizationHoliday created = organizationHolidayService.create(dto);

        return ResponseEntity
                .created(URI.create("/api/setup/organization-holiday/" + created.getId()))
                .body(OrganizationHolidayResponseVM.ofEntity(created));
    }

    /**
     * {@code PUT /organization-holiday} : Update an existing OrganizationHoliday.
     */
    @PutMapping("/organization-holiday")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<OrganizationHolidayResponseVM> updateOrganizationHoliday(
            @Valid @RequestBody OrganizationHolidayUpdateDTO dto
    ) {
        LOG.debug("REST request to update OrganizationHoliday : {}", dto);

        return organizationHolidayService.update(dto)
                .map(updated -> ResponseEntity.ok(OrganizationHolidayResponseVM.ofEntity(updated)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * {@code GET /organization-holiday} : Get all OrganizationHolidays.
     */
    @GetMapping("/organization-holiday")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<List<OrganizationHolidayResponseVM>> getAllOrganizationHolidays() {
        LOG.debug("REST request to get all OrganizationHolidays");

        List<OrganizationHolidayResponseVM> result = organizationHolidayService.findAll()
                .stream()
                .map(OrganizationHolidayResponseVM::ofEntity)
                .toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/organization-holiday/by-date-range")
    public ResponseEntity<List<OrganizationHolidayResponseVM>> getActiveHolidaysInRange(
            @RequestParam("fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam("toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam Long facilityId
    ) {
        LOG.debug("REST request to get active holidays between {} and {}", fromDate, toDate);

        if (facilityId == null) {
            throw new BadRequestAlertException(
                    "Facility are required",
                    "organizationHoliday",
                    "datenull"
            );
        }

        if (fromDate == null || toDate == null) {
            throw new BadRequestAlertException(
                    "From date and to date are required",
                    "organizationHoliday",
                    "datenull"
            );
        }

        if (toDate.isBefore(fromDate)) {
            throw new BadRequestAlertException(
                    "To date cannot be before from date",
                    "organizationHoliday",
                    "dateinvalid"
            );
        }


        List<OrganizationHoliday> result =
                organizationHolidayService.getActiveHolidaysInRange(facilityId, fromDate, toDate);

        return ResponseEntity.ok(result.stream()
                .map(OrganizationHolidayResponseVM::ofEntity)
                .toList());
    }

    /**
     * {@code GET /organization-holiday/{id}} : Get one OrganizationHoliday by id.
     */
    @GetMapping("/organization-holiday/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<OrganizationHolidayResponseVM> getOrganizationHoliday(@PathVariable Long id) {
        LOG.debug("REST request to get OrganizationHoliday : {}", id);

        return organizationHolidayService.findOne(id)
                .map(OrganizationHolidayResponseVM::ofEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * {@code PATCH /organization-holiday/{id}/toggle-active} : Toggle active flag.
     */
    @PatchMapping("/organization-holiday/{id}/toggle-active")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<OrganizationHolidayResponseVM> toggleOrganizationHolidayActive(@PathVariable Long id) {
        LOG.debug("REST request to toggle active OrganizationHoliday : {}", id);

        return organizationHolidayService.toggleActive(id)
                .map(updated -> ResponseEntity.ok(OrganizationHolidayResponseVM.ofEntity(updated)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * {@code GET /organization-holiday/search} : Search OrganizationHolidays by optional filters.
     */
    @GetMapping("/organization-holiday/search")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<List<OrganizationHolidayResponseVM>> searchOrganizationHolidays(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) HolidayType holidayType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Boolean recurring,
            @RequestParam(required = false) Boolean allFacilities,
            @RequestParam(required = false) Long facilityId
    ) {
        LOG.debug(
                "REST request to search OrganizationHolidays name={}, holidayType={}, startDate={}, endDate={}, recurring={}, allFacilities={}, facilityId={}",
                name, holidayType, startDate, endDate, recurring, allFacilities, facilityId
        );

        List<OrganizationHolidayResponseVM> result = organizationHolidayService.search(name, holidayType, startDate, endDate, recurring, allFacilities, facilityId)
                .stream()
                .map(OrganizationHolidayResponseVM::ofEntity)
                .toList();

        return ResponseEntity.ok(result);
    }

    /**
     * {@code GET /organization-holiday} : Get all active OrganizationHolidays.
     */
    @GetMapping("/organization-holiday/active-desc")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<List<OrganizationHolidayResponseVM>> getActiveHolidaysDesc() {
        LOG.debug("REST request to get active OrganizationHolidays DESC");

        List<OrganizationHolidayResponseVM> result = organizationHolidayService.findAllActiveDesc()
                .stream()
                .map(OrganizationHolidayResponseVM::ofEntity)
                .toList();

        return ResponseEntity.ok(result);
    }
}