package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.BedRoomService;
import com.dazzle.asklepios.service.BedRoomServiceService;
import com.dazzle.asklepios.service.dto.bedRoomService.BedRoomServiceCreateDTO;
import com.dazzle.asklepios.service.dto.bedRoomService.BedRoomServiceUpdateDTO;
import com.dazzle.asklepios.web.rest.Helper.PaginationUtil;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/setup")
public class BedRoomServiceController {

    private static final Logger LOG = LoggerFactory.getLogger(BedRoomServiceController.class);

    private final BedRoomServiceService bedRoomServiceService;

    public BedRoomServiceController(BedRoomServiceService bedRoomServiceService) {
        this.bedRoomServiceService = bedRoomServiceService;
    }

    @PostMapping("/bed-room-service")
    public ResponseEntity<BedRoomService> create(
            @Valid @RequestBody @NotNull BedRoomServiceCreateDTO bedRoomServiceCreateDTO
    ) {
        LOG.debug("REST create BedRoomService payload={}", bedRoomServiceCreateDTO);

        if (bedRoomServiceCreateDTO.roomId() == null) {
            LOG.warn("[CREATE] BedRoomService rejected: roomId is null payload={}", bedRoomServiceCreateDTO);
            throw new BadRequestAlertException("Room id is required", "bedRoomService", "room.required");
        }

        if (bedRoomServiceCreateDTO.serviceId() == null) {
            LOG.warn("[CREATE] BedRoomService rejected: serviceId is null payload={}", bedRoomServiceCreateDTO);
            throw new BadRequestAlertException("Service id is required", "bedRoomService", "service.required");
        }

        if (bedRoomServiceCreateDTO.bedSpecific() == null) {
            LOG.warn("[CREATE] BedRoomService rejected: bedSpecific is null payload={}", bedRoomServiceCreateDTO);
            throw new BadRequestAlertException("Bed specific flag is required", "bedRoomService", "bedSpecific.required");
        }

        if (Boolean.TRUE.equals(bedRoomServiceCreateDTO.bedSpecific()) && bedRoomServiceCreateDTO.bedId() == null) {
            LOG.warn("[CREATE] BedRoomService rejected: bedId is null while bedSpecific=true payload={}", bedRoomServiceCreateDTO);
            throw new BadRequestAlertException("Bed id is required when bedSpecific is true", "bedRoomService", "bed.required");
        }

        BedRoomService createdBedRoomService = bedRoomServiceService.create(bedRoomServiceCreateDTO);

        return ResponseEntity
                .created(URI.create("/api/setup/bed-room-service/" + createdBedRoomService.getId()))
                .body(createdBedRoomService);
    }

    @PutMapping("/bed-room-service/{id}")
    public ResponseEntity<BedRoomService> update(
            @PathVariable @NotNull Long id,
            @Valid @RequestBody @NotNull BedRoomServiceUpdateDTO bedRoomServiceUpdateDTO
    ) {
        LOG.debug("REST update BedRoomService id={} payload={}", id, bedRoomServiceUpdateDTO);

        if (bedRoomServiceUpdateDTO.id() == null) {
            LOG.warn("[UPDATE] BedRoomService rejected: dto id is null payload={}", bedRoomServiceUpdateDTO);
            throw new BadRequestAlertException("Bed room service id is required", "bedRoomService", "id.required");
        }

        if (!id.equals(bedRoomServiceUpdateDTO.id())) {
            LOG.warn("[UPDATE] BedRoomService rejected: path id={} does not match dto id={}", id, bedRoomServiceUpdateDTO.id());
            throw new BadRequestAlertException("Invalid bed room service id", "bedRoomService", "id.invalid");
        }

        if (bedRoomServiceUpdateDTO.roomId() == null) {
            LOG.warn("[UPDATE] BedRoomService rejected: roomId is null payload={}", bedRoomServiceUpdateDTO);
            throw new BadRequestAlertException("Room id is required", "bedRoomService", "room.required");
        }

        if (bedRoomServiceUpdateDTO.serviceId() == null) {
            LOG.warn("[UPDATE] BedRoomService rejected: serviceId is null payload={}", bedRoomServiceUpdateDTO);
            throw new BadRequestAlertException("Service id is required", "bedRoomService", "service.required");
        }

        if (bedRoomServiceUpdateDTO.bedSpecific() == null) {
            LOG.warn("[UPDATE] BedRoomService rejected: bedSpecific is null payload={}", bedRoomServiceUpdateDTO);
            throw new BadRequestAlertException("Bed specific flag is required", "bedRoomService", "bedSpecific.required");
        }

        if (Boolean.TRUE.equals(bedRoomServiceUpdateDTO.bedSpecific()) && bedRoomServiceUpdateDTO.bedId() == null) {
            LOG.warn("[UPDATE] BedRoomService rejected: bedId is null while bedSpecific=true payload={}", bedRoomServiceUpdateDTO);
            throw new BadRequestAlertException("Bed id is required when bedSpecific is true", "bedRoomService", "bed.required");
        }

        BedRoomService updatedBedRoomService = bedRoomServiceService.update(id, bedRoomServiceUpdateDTO);
        return ResponseEntity.ok(updatedBedRoomService);
    }

    @PutMapping("/bed-room-service/{id}/activation-status/{active}")
    public ResponseEntity<BedRoomService> changeActivationStatus(
            @PathVariable("id") @NotNull Long bedRoomServiceId,
            @PathVariable("active") @NotNull Boolean active
    ) {
        LOG.debug("REST change activation status for BedRoomService id={} active={}", bedRoomServiceId, active);

        BedRoomService updatedBedRoomService =
                bedRoomServiceService.changeActivationStatus(bedRoomServiceId, active);

        return ResponseEntity.ok(updatedBedRoomService);
    }

    @GetMapping("/bed-room-service/room/{roomId}")
    public ResponseEntity<List<BedRoomService>> findAllByRoom(
            @PathVariable Long roomId,
            @ParameterObject Pageable pageable
    ) {
        LOG.info("[REST] Fetch BedRoomServices for roomId={} pageable={}", roomId, pageable);

        Page<BedRoomService> page =
                bedRoomServiceService.findAllByRoom(roomId, pageable);

        LOG.info("[REST] Retrieved {} records for roomId={}",
                page.getTotalElements(),
                roomId);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/bed-room-service/{id}")
    public ResponseEntity<BedRoomService> findById(
            @PathVariable("id") @NotNull Long bedRoomServiceId
    ) {
        LOG.debug("REST find BedRoomService by id={}", bedRoomServiceId);

        BedRoomService bedRoomService = bedRoomServiceService.findById(bedRoomServiceId);
        return ResponseEntity.ok(bedRoomService);
    }
}