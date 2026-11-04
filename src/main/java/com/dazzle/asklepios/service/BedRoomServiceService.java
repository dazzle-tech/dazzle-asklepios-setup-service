package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Bed;
import com.dazzle.asklepios.domain.BedRoomService;
import com.dazzle.asklepios.domain.Room;
import com.dazzle.asklepios.domain.ServiceSetup;
import com.dazzle.asklepios.repository.BedRepository;
import com.dazzle.asklepios.repository.BedRoomServiceRepository;
import com.dazzle.asklepios.repository.RoomRepository;
import com.dazzle.asklepios.repository.ServiceRepository;
import com.dazzle.asklepios.service.dto.bedRoomService.BedRoomServiceCreateDTO;
import com.dazzle.asklepios.service.dto.bedRoomService.BedRoomServiceUpdateDTO;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;

@Service
@Transactional
public class BedRoomServiceService {

    private static final Logger LOG = LoggerFactory.getLogger(BedRoomServiceService.class);

    private final BedRoomServiceRepository bedRoomServiceRepository;
    private final RoomRepository roomRepository;
    private final ServiceRepository serviceRepository;
    private final BedRepository bedRepository;

    public BedRoomServiceService(
            BedRoomServiceRepository bedRoomServiceRepository,
            RoomRepository roomRepository,
            ServiceRepository serviceRepository,
            BedRepository bedRepository
    ) {
        this.bedRoomServiceRepository = bedRoomServiceRepository;
        this.roomRepository = roomRepository;
        this.serviceRepository = serviceRepository;
        this.bedRepository = bedRepository;
    }

    public BedRoomService create(BedRoomServiceCreateDTO createDTO) {
        LOG.info("[CREATE] Request to create BedRoomService payload={}", createDTO);

        Room room = roomRepository.findById(createDTO.roomId())
                .orElseThrow(() -> {
                    LOG.warn("[CREATE] Room not found id={}", createDTO.roomId());
                    return new NotFoundAlertException(
                            "Room not found with id " + createDTO.roomId(),
                            "bedRoomService",
                            "room.notfound"
                    );
                });

        ServiceSetup service = serviceRepository.findById(createDTO.serviceId())
                .orElseThrow(() -> {
                    LOG.warn("[CREATE] Service not found id={}", createDTO.serviceId());
                    return new NotFoundAlertException(
                            "Service not found with id " + createDTO.serviceId(),
                            "bedRoomService",
                            "service.notfound"
                    );
                });

        Bed bed = null;
        if (Boolean.TRUE.equals(createDTO.bedSpecific())) {
            if (createDTO.bedId() == null) {
                LOG.warn("[CREATE] bedId is null while bedSpecific=true payload={}", createDTO);
                throw new BadRequestAlertException(
                        "Bed id is required when bedSpecific is true",
                        "bedRoomService",
                        "bed.required"
                );
            }

            bed = bedRepository.findById(createDTO.bedId())
                    .orElseThrow(() -> {
                        LOG.warn("[CREATE] Bed not found id={}", createDTO.bedId());
                        return new NotFoundAlertException(
                                "Bed not found with id " + createDTO.bedId(),
                                "bedRoomService",
                                "bed.notfound"
                        );
                    });
        }

        BedRoomService entity = BedRoomService.builder()
                .room(room)
                .service(service)
                .bedSpecific(Boolean.TRUE.equals(createDTO.bedSpecific()))
                .bed(bed)
                .rule(createDTO.rule())
                .build();

        try {
            BedRoomService saved = bedRoomServiceRepository.saveAndFlush(entity);
            LOG.info("[CREATE] Successfully created BedRoomService id={}", saved.getId());
            return saved;

        } catch (DataIntegrityViolationException | JpaSystemException exception) {
            LOG.error("[CREATE] DB error while saving BedRoomService payload={}", createDTO, exception);
            handleConstraints(exception);
            throw new BadRequestAlertException("Database constraint violated.", "bedRoomService", "db.constraint");
        }
    }

    public BedRoomService update(Long id, BedRoomServiceUpdateDTO updateDTO) {
        LOG.info("[UPDATE] Request to update BedRoomService id={} payload={}", id, updateDTO);

        BedRoomService existing = bedRoomServiceRepository.findById(id)
                .orElseThrow(() -> {
                    LOG.warn("[UPDATE] BedRoomService not found id={}", id);
                    return new NotFoundAlertException(
                            "BedRoomService not found with id " + id,
                            "bedRoomService",
                            "notfound"
                    );
                });

        Room room = roomRepository.findById(updateDTO.roomId())
                .orElseThrow(() -> {
                    LOG.warn("[UPDATE] Room not found id={}", updateDTO.roomId());
                    return new NotFoundAlertException(
                            "Room not found",
                            "bedRoomService",
                            "room.notfound"
                    );
                });

        ServiceSetup service = serviceRepository.findById(updateDTO.serviceId())
                .orElseThrow(() -> {
                    LOG.warn("[UPDATE] Service not found id={}", updateDTO.serviceId());
                    return new NotFoundAlertException(
                            "Service not found",
                            "bedRoomService",
                            "service.notfound"
                    );
                });

        Bed bed = null;
        if (Boolean.TRUE.equals(updateDTO.bedSpecific())) {
            if (updateDTO.bedId() == null) {
                LOG.warn("[UPDATE] bedId is null while bedSpecific=true payload={}", updateDTO);
                throw new BadRequestAlertException(
                        "Bed is required",
                        "bedRoomService",
                        "bed.required"
                );
            }

            bed = bedRepository.findById(updateDTO.bedId())
                    .orElseThrow(() -> {
                        LOG.warn("[UPDATE] Bed not found id={}", updateDTO.bedId());
                        return new NotFoundAlertException(
                                "Bed not found",
                                "bedRoomService",
                                "bed.notfound"
                        );
                    });
        }

        existing.setRoom(room);
        existing.setService(service);
        existing.setBedSpecific(updateDTO.bedSpecific());
        existing.setBed(bed);
        existing.setRule(updateDTO.rule());

        try {
            BedRoomService updated = bedRoomServiceRepository.saveAndFlush(existing);
            LOG.info("[UPDATE] Successfully updated BedRoomService id={}", updated.getId());
            return updated;

        } catch (DataIntegrityViolationException | JpaSystemException exception) {
            LOG.error("[UPDATE] DB error while updating BedRoomService id={} payload={}", id, updateDTO, exception);
            handleConstraints(exception);
            throw new BadRequestAlertException("Database constraint violated.", "bedRoomService", "db.constraint");
        }
    }

    public BedRoomService changeActivationStatus(Long id, boolean active) {
        LOG.info("[CHANGE ACTIVATION STATUS] Request to set BedRoomService id={} active={}", id, active);

        BedRoomService entity = findById(id);
        entity.setIsActive(active);

        try {
            BedRoomService updated = bedRoomServiceRepository.saveAndFlush(entity);
            LOG.info(
                    "[CHANGE ACTIVATION STATUS] Successfully changed activation status for BedRoomService id={} active={}",
                    id,
                    updated.getIsActive()
            );
            return updated;

        } catch (DataIntegrityViolationException | JpaSystemException exception) {
            LOG.error("[CHANGE ACTIVATION STATUS] DB error while updating BedRoomService id={}", id, exception);
            handleConstraints(exception);
            throw new BadRequestAlertException(
                    "Database constraint violated.",
                    "bedRoomService",
                    "db.constraint"
            );
        }
    }

    @Transactional(readOnly = true)
    public BedRoomService findById(Long id) {
        LOG.debug("[FIND BY ID] Fetch BedRoomService id={}", id);

        return bedRoomServiceRepository.findById(id)
                .orElseThrow(() -> {
                    LOG.warn("[FIND BY ID] BedRoomService not found id={}", id);
                    return new NotFoundAlertException(
                            "BedRoomService not found with id " + id,
                            "bedRoomService",
                            "notfound"
                    );
                });
    }

    public Page<BedRoomService> findAllByRoom(Long roomId, Pageable pageable) {
        LOG.info("[GET ALL] Fetch BedRoomServices for roomId={} page={} size={}",
                roomId,
                pageable.getPageNumber(),
                pageable.getPageSize());

        Page<BedRoomService> result =
                bedRoomServiceRepository.findByRoom_Id(roomId, pageable);

        LOG.info("[GET ALL] Retrieved {} records for roomId={}",
                result.getTotalElements(),
                roomId);

        return result;
    }

    private void handleConstraints(RuntimeException exception) {
        String message = getRootCause(exception) != null
                ? getRootCause(exception).getMessage()
                : exception.getMessage();

        LOG.error("[DB ERROR] Raw message: {}", message, exception);

        String lower = message != null ? message.toLowerCase() : "";

        if (lower.contains("uk_bed_room_service")) {
            LOG.warn("[CONSTRAINT] Duplicate BedRoomService detected (room/bed/service combination)");
            throw new BadRequestAlertException(
                    "This service is already assigned to this room/bed.",
                    "bedRoomService",
                    "duplicate"
            );
        }

        LOG.error("[CONSTRAINT] Unhandled DB constraint violation: {}", lower);
    }
}