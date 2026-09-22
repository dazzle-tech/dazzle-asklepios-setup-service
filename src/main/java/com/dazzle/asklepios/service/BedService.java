package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Bed;
import com.dazzle.asklepios.domain.Room;
import com.dazzle.asklepios.domain.enumeration.BedStatus;
import com.dazzle.asklepios.repository.BedRepository;
import com.dazzle.asklepios.repository.RoomRepository;
import com.dazzle.asklepios.service.dto.bed.BedCreateDTO;
import com.dazzle.asklepios.service.dto.bed.BedUpdateDTO;
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

import java.util.List;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;

@Service
@Transactional
public class BedService {

    private static final Logger LOG = LoggerFactory.getLogger(BedService.class);

    private final BedRepository bedRepository;
    private final RoomRepository roomRepository;

    public BedService(
            BedRepository bedRepository,
            RoomRepository roomRepository
    ) {
        this.bedRepository = bedRepository;
        this.roomRepository = roomRepository;
    }

    public Bed create(BedCreateDTO bedCreateDTO) {
        LOG.info("[CREATE] Request to create Bed payload={}", bedCreateDTO);

        Room room = roomRepository.findById(bedCreateDTO.roomId())
                .orElseThrow(() -> new NotFoundAlertException(
                        "Room not found with id " + bedCreateDTO.roomId(),
                        "bed",
                        "room.notfound"
                ));

        Bed bed = Bed.builder()
                .room(room)
                .name(bedCreateDTO.name())
                .locationDetails(bedCreateDTO.locationDetails())
                .type(bedCreateDTO.type())
                .status(BedStatus.READY)
                .build();

        try {
            Bed createdBed = bedRepository.saveAndFlush(bed);
            LOG.info("Successfully created bed id={} name='{}'", createdBed.getId(), createdBed.getName());
            return createdBed;

        } catch (DataIntegrityViolationException | JpaSystemException exception) {
            handleConstraintsOnCreateOrUpdate(exception);
            throw new BadRequestAlertException(
                    "Database constraint violated while saving bed.",
                    "bed",
                    "db.constraint"
            );
        }
    }

    public Bed update(Long id, BedUpdateDTO bedUpdateDTO) {
        LOG.info("[UPDATE] Request to update Bed id={} payload={}", id, bedUpdateDTO);

        Bed existingBed = bedRepository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException(
                        "Bed not found with id " + id,
                        "bed",
                        "notfound"
                ));

        Room room = roomRepository.findById(bedUpdateDTO.roomId())
                .orElseThrow(() -> new NotFoundAlertException(
                        "Room not found with id " + bedUpdateDTO.roomId(),
                        "bed",
                        "room.notfound"
                ));

        existingBed.setRoom(room);
        existingBed.setName(bedUpdateDTO.name());
        existingBed.setLocationDetails(bedUpdateDTO.locationDetails());
        existingBed.setType(bedUpdateDTO.type());

        try {
            Bed updatedBed = bedRepository.saveAndFlush(existingBed);
            LOG.info("Successfully updated bed id={} name='{}'", updatedBed.getId(), updatedBed.getName());
            return updatedBed;

        } catch (DataIntegrityViolationException | JpaSystemException exception) {
            handleConstraintsOnCreateOrUpdate(exception);
            throw new BadRequestAlertException(
                    "Database constraint violated while updating bed.",
                    "bed",
                    "db.constraint"
            );
        }
    }

    public Bed changeActivationStatus(Long id, boolean active) {
        LOG.info("[CHANGE ACTIVATION STATUS] Request to set Bed id={} active={}", id, active);

        Bed existingBed = bedRepository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException(
                        "Bed not found with id " + id,
                        "bed",
                        "notfound"
                ));

        if (!active && existingBed.getStatus() == BedStatus.OCCUPIED) {
            LOG.warn("[CHANGE ACTIVATION STATUS] Cannot deactivate occupied bed id={}", id);

            throw new BadRequestAlertException(
                    "Cannot deactivate an occupied bed",
                    "bed",
                    "bed.occupied"
            );
        }

        existingBed.setIsActive(active);

        try {
            Bed updatedBed = bedRepository.saveAndFlush(existingBed);
            LOG.info(
                    "Successfully changed activation status for bed id={} name='{}' active={}",
                    updatedBed.getId(),
                    updatedBed.getName(),
                    updatedBed.getIsActive()
            );
            return updatedBed;

        } catch (DataIntegrityViolationException | JpaSystemException exception) {
            handleConstraintsOnCreateOrUpdate(exception);
            throw new BadRequestAlertException(
                    "Database constraint violated while updating bed activation status.",
                    "bed",
                    "db.constraint"
            );
        }
    }

    @Transactional(readOnly = true)
    public Bed findById(Long id) {
        LOG.debug("[FIND BY ID] Fetching Bed id={}", id);

        return bedRepository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException(
                        "Bed not found with id " + id,
                        "bed",
                        "notfound"
                ));
    }

    @Transactional(readOnly = true)
    public Page<Bed> findActiveByRoomIdAndStatusEmpty(Long roomId, Pageable pageable) {
        LOG.debug("[FIND EMPTY ACTIVE BY ROOM ID] roomId='{}' pageable={}", roomId, pageable);

        if (!roomRepository.existsById(roomId)) {
            throw new NotFoundAlertException(
                    "Room not found with id " + roomId,
                    "bed",
                    "room.notfound"
            );
        }

        Page<Bed> bedsPage = bedRepository.findByRoom_IdAndIsActiveTrueAndStatus(
                roomId,
                BedStatus.READY,
                pageable
        );

        LOG.debug(
                "[FIND EMPTY ACTIVE BY ROOM ID] Retrieved beds count={} pageNumber={} pageSize={}",
                bedsPage.getNumberOfElements(),
                bedsPage.getNumber(),
                bedsPage.getSize()
        );

        return bedsPage;
    }

    @Transactional(readOnly = true)
    public Page<Bed> findAllActiveByRoomId(Long roomId, Pageable pageable) {
        LOG.debug("[FIND ACTIVE BEDS BY ROOM ID] roomId='{}' pageable={}", roomId, pageable);

        if (!roomRepository.existsById(roomId)) {
            throw new NotFoundAlertException(
                    "Room not found with id " + roomId,
                    "bed",
                    "room.notfound"
            );
        }

        Page<Bed> bedsPage = bedRepository.findByRoom_IdAndIsActiveTrue(roomId, pageable);

        LOG.debug(
                "[FIND ACTIVE BEDS BY ROOM ID] Retrieved beds count={} pageNumber={} pageSize={} totalElements={} totalPages={}",
                bedsPage.getNumberOfElements(),
                bedsPage.getNumber(),
                bedsPage.getSize(),
                bedsPage.getTotalElements(),
                bedsPage.getTotalPages()
        );

        return bedsPage;
    }
    public Bed markAsOccupied(Long id) {
        LOG.info("[MARK AS OCCUPIED] Request to mark Bed as OCCUPIED id={}", id);

        Bed existingBed = bedRepository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException(
                        "Bed not found with id " + id,
                        "bed",
                        "notfound"
                ));

        if (existingBed.getStatus() != BedStatus.READY) {
            LOG.warn(
                    "[MARK AS OCCUPIED] Failed: Bed id={} currentStatus={} is not EMPTY",
                    existingBed.getId(),
                    existingBed.getStatus()
            );

            throw new BadRequestAlertException(
                    "Bed must be EMPTY to be marked as OCCUPIED",
                    "bed",
                    "invalid.status.transition"
            );
        }

        existingBed.setStatus(BedStatus.OCCUPIED);

        try {
            Bed updatedBed = bedRepository.saveAndFlush(existingBed);
            LOG.info("Successfully marked bed id={} as OCCUPIED", updatedBed.getId());
            return updatedBed;

        } catch (DataIntegrityViolationException | JpaSystemException exception) {
            handleConstraintsOnCreateOrUpdate(exception);
            throw new BadRequestAlertException(
                    "Database constraint violated while updating bed status.",
                    "bed",
                    "db.constraint"
            );
        }
    }

    public Bed markAsInCleaning(Long id) {
        LOG.info("[MARK AS IN_CLEANING] Request to mark Bed as IN_CLEANING id={}", id);

        Bed existingBed = bedRepository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException(
                        "Bed not found with id " + id,
                        "bed",
                        "notfound"
                ));

        if (existingBed.getStatus() == BedStatus.IN_CLEANING) {
            LOG.info("[MARK AS IN_CLEANING] Bed id={} already IN_CLEANING, skipping", id);
            return existingBed;
        }

        if (existingBed.getStatus() != BedStatus.OCCUPIED) {
            LOG.warn(
                    "[MARK AS IN_CLEANING] Failed: Bed id={} currentStatus={} is not OCCUPIED",
                    existingBed.getId(),
                    existingBed.getStatus()
            );

            throw new BadRequestAlertException(
                    "Bed must be OCCUPIED to be marked as IN_CLEANING",
                    "bed",
                    "invalid.status.transition"
            );
        }

        existingBed.setStatus(BedStatus.IN_CLEANING);

        try {
            Bed updatedBed = bedRepository.saveAndFlush(existingBed);
            LOG.info("Successfully marked bed id={} as IN_CLEANING", updatedBed.getId());
            return updatedBed;

        } catch (DataIntegrityViolationException | JpaSystemException exception) {
            handleConstraintsOnCreateOrUpdate(exception);
            throw new BadRequestAlertException(
                    "Database constraint violated while updating bed status.",
                    "bed",
                    "db.constraint"
            );
        }
    }

    public Bed markAsOutOfService(Long id) {
        LOG.info("[MARK AS OUT_OF_SERVICE] Request id={}", id);

        Bed bed = bedRepository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException(
                        "Bed not found with id " + id,
                        "bed",
                        "notfound"
                ));

        if (bed.getStatus() != BedStatus.READY &&
                bed.getStatus() != BedStatus.IN_CLEANING) {

            LOG.warn(
                    "[MARK AS OUT_OF_SERVICE] Failed id={} currentStatus={}",
                    bed.getId(),
                    bed.getStatus()
            );

            throw new BadRequestAlertException(
                    "Bed must be EMPTY or IN_CLEANING to be marked as OUT_OF_SERVICE",
                    "bed",
                    "invalid.status.transition"
            );
        }

        bed.setStatus(BedStatus.OUT_OF_SERVICE);

        Bed updated = bedRepository.saveAndFlush(bed);

        LOG.info("Successfully marked bed id={} as OUT_OF_SERVICE", updated.getId());

        return updated;
    }

    public Bed markAsReady(Long id) {
        LOG.info("[MARK AS READY] Request id={}", id);

        Bed bed = bedRepository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException(
                        "Bed not found with id " + id,
                        "bed",
                        "notfound"
                ));

        if (bed.getStatus() != BedStatus.IN_CLEANING &&
                bed.getStatus() != BedStatus.OUT_OF_SERVICE) {

            LOG.warn(
                    "[MARK AS READY] Failed id={} currentStatus={}",
                    bed.getId(),
                    bed.getStatus()
            );

            throw new BadRequestAlertException(
                    "Bed must be IN_CLEANING or OUT_OF_SERVICE to be marked as READY",
                    "bed",
                    "invalid.status.transition"
            );
        }

        bed.setStatus(BedStatus.READY);

        Bed updated = bedRepository.saveAndFlush(bed);

        LOG.info("Successfully marked bed id={} as READY", updated.getId());

        return updated;
    }

    @Transactional(readOnly = true)
    public Page<Bed> findByRoomId(Long roomId, Pageable pageable) {
        LOG.debug("[FIND BY ROOM ID] Searching beds by roomId='{}' pageable={}", roomId, pageable);
        return bedRepository.findByRoom_Id(roomId, pageable);
    }
    @Transactional(readOnly = true)
    public List<Bed> findAllByIds(List<Long> ids) {
        LOG.debug("[FIND ALL BY IDS] Fetching Beds ids={}", ids);

        List<Bed> beds = bedRepository.findAllByIdIn(ids);

        if (beds.isEmpty()) {
            throw new NotFoundAlertException(
                    "No beds found for ids " + ids,
                    "bed",
                    "list.notfound"
            );
        }

        return beds;
    }

    @Transactional(readOnly = true)
    public long countActiveBeds(Long departmentId) {
        LOG.debug("[COUNT ACTIVE BEDS] departmentId={}", departmentId);

        long count = bedRepository.countByRoom_Department_IdAndIsActiveTrue(departmentId);

        LOG.debug("[COUNT ACTIVE BEDS RESULT] departmentId={} count={}", departmentId, count);

        return count;
    }

    @Transactional(readOnly = true)
    public long countBedsByStatus(Long departmentId, BedStatus status) {
        LOG.debug("[COUNT BEDS BY STATUS] departmentId={} status={}", departmentId, status);

        long count = bedRepository.countByRoom_Department_IdAndIsActiveTrueAndStatus(
                departmentId,
                status
        );

        LOG.debug(
                "[COUNT BEDS BY STATUS RESULT] departmentId={} status={} count={}",
                departmentId,
                status,
                count
        );

        return count;
    }

    @Transactional(readOnly = true)
    public Page<Bed> findActiveByDepartmentId(Long departmentId, Pageable pageable) {
        LOG.debug("[FIND ACTIVE BY DEPARTMENT ID] Searching active beds by departmentId='{}' pageable={}", departmentId, pageable);
        return bedRepository.findByRoom_Department_IdAndIsActiveTrue(departmentId, pageable);
    }
    private void handleConstraintsOnCreateOrUpdate(RuntimeException exception) {
        Throwable rootCause = getRootCause(exception);
        String errorMessage = rootCause != null ? rootCause.getMessage() : exception.getMessage();

        LOG.error("DB ROOT CAUSE: {}", errorMessage, exception);

        String lowerCaseErrorMessage = errorMessage != null ? errorMessage.toLowerCase() : "";

        if (lowerCaseErrorMessage.contains("uk_bed_room_name_ci")
                || lowerCaseErrorMessage.contains("duplicate")
                || lowerCaseErrorMessage.contains("unique")) {
            throw new BadRequestAlertException(
                    "A bed with the same name already exists in this room.",
                    "bed",
                    "unique.bed.name.room"
            );
        }

        if (lowerCaseErrorMessage.contains("fk_bed_room")
                || lowerCaseErrorMessage.contains("room_id")) {
            throw new BadRequestAlertException(
                    "Invalid room reference.",
                    "bed",
                    "room.invalid"
            );
        }

        throw new BadRequestAlertException(
                "Database constraint violated while saving bed.",
                "bed",
                "db.constraint"
        );
    }
}