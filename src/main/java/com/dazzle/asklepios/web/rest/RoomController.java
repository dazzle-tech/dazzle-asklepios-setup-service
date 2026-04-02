package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.Room;
import com.dazzle.asklepios.domain.enumeration.Gender;
import com.dazzle.asklepios.service.RoomService;
import com.dazzle.asklepios.service.dto.room.RoomCreateDTO;
import com.dazzle.asklepios.service.dto.room.RoomUpdateDTO;
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
public class RoomController {

    private static final Logger LOG = LoggerFactory.getLogger(RoomController.class);

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping("/room")
    public ResponseEntity<Room> create(
            @Valid @RequestBody @NotNull RoomCreateDTO roomCreateDTO
    ) {
        LOG.debug("REST create Room payload={}", roomCreateDTO);

        if (roomCreateDTO.facilityId() == null) {
            LOG.warn("[CREATE] Room rejected: facilityId is null payload={}", roomCreateDTO);
            throw new BadRequestAlertException("Facility id is required", "room", "facility.required");
        }

        if (roomCreateDTO.departmentId() == null) {
            LOG.warn("[CREATE] Room rejected: departmentId is null payload={}", roomCreateDTO);
            throw new BadRequestAlertException("Department id is required", "room", "department.required");
        }

        if (Boolean.TRUE.equals(roomCreateDTO.appointable())) {

            if (roomCreateDTO.defaultDurationMinutes() == null || roomCreateDTO.defaultDurationMinutes() <= 0) {
                LOG.warn("[CREATE] Invalid duration for appointable room payload={}", roomCreateDTO);
                throw new BadRequestAlertException(
                        "Default duration minutes must be greater than zero when room is appointable.",
                        "room",
                        "defaultDurationMinutes.invalid"
                );
            }

            if (roomCreateDTO.defaultBufferBeforeMinutes() == null || roomCreateDTO.defaultBufferBeforeMinutes() < 0) {
                LOG.warn("[CREATE] Invalid bufferBefore for appointable room payload={}", roomCreateDTO);
                throw new BadRequestAlertException(
                        "Default buffer before minutes must be zero or greater when room is appointable.",
                        "room",
                        "defaultBufferBeforeMinutes.invalid"
                );
            }

            if (roomCreateDTO.defaultBufferAfterMinutes() == null || roomCreateDTO.defaultBufferAfterMinutes() < 0) {
                LOG.warn("[CREATE] Invalid bufferAfter for appointable room payload={}", roomCreateDTO);
                throw new BadRequestAlertException(
                        "Default buffer after minutes must be zero or greater when room is appointable.",
                        "room",
                        "defaultBufferAfterMinutes.invalid"
                );
            }
        }

        Room createdRoom = roomService.create(roomCreateDTO);

        return ResponseEntity
                .created(URI.create("/api/setup/room/" + createdRoom.getId()))
                .body(createdRoom);
    }

    @PutMapping("/room/{id}")
    public ResponseEntity<Room> update(
            @PathVariable @NotNull Long id,
            @Valid @RequestBody @NotNull RoomUpdateDTO roomUpdateDTO
    ) {
        LOG.debug("REST update Room id={} payload={}", id, roomUpdateDTO);

        if (roomUpdateDTO.id() == null) {
            LOG.warn("[UPDATE] Room rejected: dto id is null payload={}", roomUpdateDTO);
            throw new BadRequestAlertException("Room id is required", "room", "id.required");
        }

        if (!id.equals(roomUpdateDTO.id())) {
            LOG.warn("[UPDATE] Room rejected: path id={} does not match dto id={}", id, roomUpdateDTO.id());
            throw new BadRequestAlertException("Invalid room id", "room", "id.invalid");
        }

        if (roomUpdateDTO.facilityId() == null) {
            LOG.warn("[UPDATE] Room rejected: facilityId is null payload={}", roomUpdateDTO);
            throw new BadRequestAlertException("Facility id is required", "room", "facility.required");
        }

        if (roomUpdateDTO.departmentId() == null) {
            LOG.warn("[UPDATE] Room rejected: departmentId is null payload={}", roomUpdateDTO);
            throw new BadRequestAlertException("Department id is required", "room", "department.required");
        }

        if (Boolean.TRUE.equals(roomUpdateDTO.appointable())) {

            if (roomUpdateDTO.defaultDurationMinutes() == null || roomUpdateDTO.defaultDurationMinutes() <= 0) {
                LOG.warn("[UPDATE] Invalid duration for appointable room payload={}", roomUpdateDTO);
                throw new BadRequestAlertException(
                        "Default duration minutes must be greater than zero when room is appointable.",
                        "room",
                        "defaultDurationMinutes.invalid"
                );
            }

            if (roomUpdateDTO.defaultBufferBeforeMinutes() == null || roomUpdateDTO.defaultBufferBeforeMinutes() < 0) {
                LOG.warn("[UPDATE] Invalid bufferBefore for appointable room payload={}", roomUpdateDTO);
                throw new BadRequestAlertException(
                        "Default buffer before minutes must be zero or greater when room is appointable.",
                        "room",
                        "defaultBufferBeforeMinutes.invalid"
                );
            }

            if (roomUpdateDTO.defaultBufferAfterMinutes() == null || roomUpdateDTO.defaultBufferAfterMinutes() < 0) {
                LOG.warn("[UPDATE] Invalid bufferAfter for appointable room payload={}", roomUpdateDTO);
                throw new BadRequestAlertException(
                        "Default buffer after minutes must be zero or greater when room is appointable.",
                        "room",
                        "defaultBufferAfterMinutes.invalid"
                );
            }
        }

        Room updatedRoom = roomService.update(id, roomUpdateDTO);
        return ResponseEntity.ok(updatedRoom);
    }

    @PutMapping("/room/{id}/activation-status/{active}")
    public ResponseEntity<Room> changeActivationStatus(
            @PathVariable("id") @NotNull Long roomId,
            @PathVariable("active") @NotNull Boolean active
    ) {
        LOG.debug("REST change activation status for Room id={} active={}", roomId, active);

        Room updatedRoom = roomService.changeActivationStatus(roomId, active);
        return ResponseEntity.ok(updatedRoom);
    }

    @PostMapping("/room/by-ids")
    public ResponseEntity<List<Room>> findAllByIds(
            @RequestBody @NotNull List<Long> ids
    ) {
        LOG.debug("REST find Rooms by ids={}", ids);

        if (ids == null || ids.isEmpty()) {
            throw new BadRequestAlertException(
                    "Ids list must not be empty",
                    "room",
                    "ids.empty"
            );
        }

        List<Room> rooms = roomService.findAllByIds(ids);
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/room/available/by-department/{departmentId}/gender/{gender}")
    public ResponseEntity<List<Room>> findAvailableRoomsByDepartmentAndGender(
            @PathVariable @NotNull Long departmentId,
            @PathVariable @NotNull Gender gender,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST find available Rooms by departmentId={} gender='{}' pageable={}",
                departmentId, gender, pageable);

        Page<Room> page = roomService.findAvailableRoomsByDepartmentAndGender(
                departmentId,
                gender,
                pageable
        );

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Room>> findAll(
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST find all Rooms pageable={}", pageable);

        Page<Room> page = roomService.findAll(pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/room/{id}")
    public ResponseEntity<Room> findById(
            @PathVariable("id") @NotNull Long roomId
    ) {
        LOG.debug("REST find Room by id={}", roomId);

        Room room = roomService.findById(roomId);
        return ResponseEntity.ok(room);
    }

    @GetMapping("/search/by-name/{name}")
    public ResponseEntity<List<Room>> findByName(
            @PathVariable @NotNull String name,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST find Rooms by name='{}' pageable={}", name, pageable);

        Page<Room> page = roomService.findByName(name, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/room/search/by-department/{departmentId}")
    public ResponseEntity<List<Room>> findByDepartmentId(
            @PathVariable @NotNull Long departmentId,
            @ParameterObject Pageable pageable
    ) {
        LOG.debug("REST find Rooms by departmentId={} pageable={}", departmentId, pageable);

        Page<Room> page = roomService.findByDepartmentId(departmentId, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page
        );

        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
}