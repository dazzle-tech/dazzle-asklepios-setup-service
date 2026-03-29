package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.Bed;
import com.dazzle.asklepios.service.BedService;
import com.dazzle.asklepios.service.dto.bed.BedCreateDTO;
import com.dazzle.asklepios.service.dto.bed.BedUpdateDTO;
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
public class BedController {

    private static final Logger LOG = LoggerFactory.getLogger(BedController.class);

    private final BedService bedService;

    public BedController(BedService bedService) {
        this.bedService = bedService;
    }

    @PostMapping("/bed")
    public ResponseEntity<Bed> create(
            @Valid @RequestBody @NotNull BedCreateDTO bedCreateDTO
    ) {
        LOG.debug("REST create Bed payload={}", bedCreateDTO);

        if (bedCreateDTO.roomId() == null) {
            LOG.warn("[CREATE] Bed rejected: roomId is null payload={}", bedCreateDTO);
            throw new BadRequestAlertException("Room id is required", "bed", "room.required");
        }

        if (bedCreateDTO.type() == null) {
            LOG.warn("[CREATE] Bed rejected: bed type is null payload={}", bedCreateDTO);
            throw new BadRequestAlertException("Bed type is required", "bed", "type.required");
        }

        Bed createdBed = bedService.create(bedCreateDTO);

        return ResponseEntity
                .created(URI.create("/api/setup/bed/" + createdBed.getId()))
                .body(createdBed);
    }

    @PutMapping("/bed/{id}")
    public ResponseEntity<Bed> update(
            @PathVariable @NotNull Long id,
            @Valid @RequestBody @NotNull BedUpdateDTO bedUpdateDTO
    ) {
        LOG.debug("REST update Bed id={} payload={}", id, bedUpdateDTO);

        if (bedUpdateDTO.id() == null) {
            LOG.warn("[UPDATE] Bed rejected: dto id is null payload={}", bedUpdateDTO);
            throw new BadRequestAlertException("Bed id is required", "bed", "id.required");
        }

        if (!id.equals(bedUpdateDTO.id())) {
            LOG.warn("[UPDATE] Bed rejected: path id={} does not match dto id={}", id, bedUpdateDTO.id());
            throw new BadRequestAlertException("Invalid bed id", "bed", "id.invalid");
        }

        if (bedUpdateDTO.roomId() == null) {
            LOG.warn("[UPDATE] Bed rejected: roomId is null payload={}", bedUpdateDTO);
            throw new BadRequestAlertException("Room id is required", "bed", "room.required");
        }

        if (bedUpdateDTO.type() == null) {
            LOG.warn("[UPDATE] Bed rejected: bed type is null payload={}", bedUpdateDTO);
            throw new BadRequestAlertException("Bed type is required", "bed", "type.required");
        }

        Bed updatedBed = bedService.update(id, bedUpdateDTO);
        return ResponseEntity.ok(updatedBed);
    }

    @PostMapping("/bed/{id}/activate")
    public ResponseEntity<Bed> activate(
            @PathVariable("id") @NotNull Long bedId
    ) {
        LOG.debug("REST activate Bed id={}", bedId);

        Bed activatedBed = bedService.activate(bedId);
        return ResponseEntity.ok(activatedBed);
    }

    @PostMapping("/bed/{id}/deactivate")
    public ResponseEntity<Bed> deactivate(
            @PathVariable("id") @NotNull Long bedId
    ) {
        LOG.debug("REST deactivate Bed id={}", bedId);

        Bed deactivatedBed = bedService.deactivate(bedId);
        return ResponseEntity.ok(deactivatedBed);
    }

    @GetMapping("/bed/search/active/by-room/{roomId}")
    public ResponseEntity<List<Bed>> findActiveByRoomId(
            @PathVariable @NotNull Long roomId,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST find active Beds by roomId={} pageable={}", roomId, pageable);

        Page<Bed> page = bedService.findActiveByRoomId(roomId, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page

        );

        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
    @GetMapping("/bed/{id}")
    public ResponseEntity<Bed> findById(
            @PathVariable("id") @NotNull Long bedId
    ) {
        LOG.debug("REST find Bed by id={}", bedId);

        Bed bed = bedService.findById(bedId);
        return ResponseEntity.ok(bed);
    }


    @GetMapping("/bed/search/by-room/{roomId}")
    public ResponseEntity<List<Bed>> findByRoomId(
            @PathVariable @NotNull Long roomId,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST find Beds by roomId={} pageable={}", roomId, pageable);

        Page<Bed> page = bedService.findByRoomId(roomId, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
    @PostMapping("/bed/{id}/occupy")
    public ResponseEntity<Bed> markAsOccupied(
            @PathVariable("id") @NotNull Long bedId
    ) {
        LOG.debug("REST request to mark Bed as OCCUPIED id={}", bedId);

        Bed bed = bedService.markAsOccupied(bedId);

        return ResponseEntity.ok(bed);
    }
}