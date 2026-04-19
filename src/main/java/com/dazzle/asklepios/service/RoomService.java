package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.Room;
import com.dazzle.asklepios.domain.enumeration.BedStatus;
import com.dazzle.asklepios.domain.enumeration.Gender;
import com.dazzle.asklepios.repository.BedRepository;
import com.dazzle.asklepios.repository.DepartmentsRepository;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.repository.RoomRepository;
import com.dazzle.asklepios.service.dto.room.RoomCreateDTO;
import com.dazzle.asklepios.service.dto.room.RoomUpdateDTO;
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
public class RoomService {

    private static final Logger LOG = LoggerFactory.getLogger(RoomService.class);

    private final RoomRepository roomRepository;
    private final FacilityRepository facilityRepository;
    private final DepartmentsRepository departmentRepository;
    private final BedRepository bedRepository;

    public RoomService(
            RoomRepository roomRepository,
            FacilityRepository facilityRepository,
            DepartmentsRepository departmentRepository, BedRepository bedRepository
    ) {
        this.roomRepository = roomRepository;
        this.facilityRepository = facilityRepository;
        this.departmentRepository = departmentRepository;
        this.bedRepository = bedRepository;
    }

    public Room create(RoomCreateDTO roomCreateDTO) {
        LOG.info("[CREATE] Request to create Room payload={}", roomCreateDTO);

        Facility facility = facilityRepository.findById(roomCreateDTO.facilityId())
                .orElseThrow(() -> new NotFoundAlertException(
                        "Facility not found with id " + roomCreateDTO.facilityId(),
                        "room",
                        "facility.notfound"
                ));

        Department department = departmentRepository.findById(roomCreateDTO.departmentId())
                .orElseThrow(() -> new NotFoundAlertException(
                        "Department not found with id " + roomCreateDTO.departmentId(),
                        "room",
                        "department.notfound"
                ));

        Room room = Room.builder()
                .facility(facility)
                .departmentType(roomCreateDTO.departmentType())
                .department(department)
                .name(roomCreateDTO.name())
                .type(roomCreateDTO.type())
                .floor(roomCreateDTO.floor())
                .isSpecificGender(Boolean.TRUE.equals(roomCreateDTO.isSpecificGender()))
                .gender(roomCreateDTO.gender())
                .appointable(roomCreateDTO.appointable())
                .parallelCapacityValue(roomCreateDTO.parallelCapacityValue())
                .defaultDurationMinutes(roomCreateDTO.defaultDurationMinutes())
                .defaultBufferBeforeMinutes(
                        roomCreateDTO.defaultBufferBeforeMinutes() != null
                                ? roomCreateDTO.defaultBufferBeforeMinutes()
                                : 0
                )
                .defaultBufferAfterMinutes(
                        roomCreateDTO.defaultBufferAfterMinutes() != null
                                ? roomCreateDTO.defaultBufferAfterMinutes()
                                : 0
                )
                .build();

        try {
            Room createdRoom = roomRepository.saveAndFlush(room);
            LOG.info("Successfully created room id={} name='{}'", createdRoom.getId(), createdRoom.getName());
            return createdRoom;

        } catch (DataIntegrityViolationException | JpaSystemException exception) {
            handleConstraintsOnCreateOrUpdate(exception);
            throw new BadRequestAlertException(
                    "Database constraint violated while saving room.",
                    "room",
                    "db.constraint"
            );
        }
    }

    public Room update(Long id, RoomUpdateDTO roomUpdateDTO) {
        LOG.info("[UPDATE] Request to update Room id={} payload={}", id, roomUpdateDTO);

        Room existingRoom = roomRepository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException(
                        "Room not found with id " + id,
                        "room",
                        "notfound"
                ));

        Facility facility = facilityRepository.findById(roomUpdateDTO.facilityId())
                .orElseThrow(() -> new NotFoundAlertException(
                        "Facility not found with id " + roomUpdateDTO.facilityId(),
                        "room",
                        "facility.notfound"
                ));

        Department department = departmentRepository.findById(roomUpdateDTO.departmentId())
                .orElseThrow(() -> new NotFoundAlertException(
                        "Department not found with id " + roomUpdateDTO.departmentId(),
                        "room",
                        "department.notfound"
                ));

        existingRoom.setFacility(facility);
        existingRoom.setDepartmentType(roomUpdateDTO.departmentType());
        existingRoom.setDepartment(department);
        existingRoom.setName(roomUpdateDTO.name());
        existingRoom.setType(roomUpdateDTO.type());
        existingRoom.setFloor(roomUpdateDTO.floor());
        existingRoom.setIsSpecificGender(Boolean.TRUE.equals(roomUpdateDTO.isSpecificGender()));
        existingRoom.setGender(roomUpdateDTO.gender());
        existingRoom.setAppointable(roomUpdateDTO.appointable());
        existingRoom.setParallelCapacityValue(roomUpdateDTO.parallelCapacityValue());
        existingRoom.setDefaultDurationMinutes(roomUpdateDTO.defaultDurationMinutes());
        existingRoom.setDefaultBufferBeforeMinutes(
                roomUpdateDTO.defaultBufferBeforeMinutes() != null
                        ? roomUpdateDTO.defaultBufferBeforeMinutes()
                        : 0
        );
        existingRoom.setDefaultBufferAfterMinutes(
                roomUpdateDTO.defaultBufferAfterMinutes() != null
                        ? roomUpdateDTO.defaultBufferAfterMinutes()
                        : 0
        );

        try {
            Room updatedRoom = roomRepository.saveAndFlush(existingRoom);
            LOG.info("Successfully updated room id={} name='{}'", updatedRoom.getId(), updatedRoom.getName());
            return updatedRoom;

        } catch (DataIntegrityViolationException | JpaSystemException exception) {
            handleConstraintsOnCreateOrUpdate(exception);
            throw new BadRequestAlertException(
                    "Database constraint violated while updating room.",
                    "room",
                    "db.constraint"
            );
        }
    }

    public Room changeActivationStatus(Long id, boolean active) {
        LOG.info("[CHANGE ACTIVATION STATUS] Request to set Room id={} active={}", id, active);

        Room existingRoom = roomRepository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException(
                        "Room not found with id " + id,
                        "room",
                        "notfound"
                ));

        if (!active) {
            boolean hasOccupiedBeds = bedRepository.existsByRoom_IdAndStatus(id, BedStatus.OCCUPIED);

            if (hasOccupiedBeds) {
                throw new BadRequestAlertException(
                        "Cannot deactivate room because it has occupied beds.",
                        "room",
                        "room.has.occupied.beds"
                );
            }
        }

        existingRoom.setIsActive(active);

        try {
            Room updatedRoom = roomRepository.saveAndFlush(existingRoom);
            LOG.info(
                    "Successfully changed activation status for room id={} name='{}' active={}",
                    updatedRoom.getId(),
                    updatedRoom.getName(),
                    updatedRoom.getIsActive()
            );
            return updatedRoom;

        } catch (DataIntegrityViolationException | JpaSystemException exception) {
            handleConstraintsOnCreateOrUpdate(exception);
            throw new BadRequestAlertException(
                    "Database constraint violated while updating room activation status.",
                    "room",
                    "db.constraint"
            );
        }
    }

    @Transactional(readOnly = true)
    public Room findById(Long id) {
        LOG.debug("[FIND BY ID] Fetching Room id={}", id);

        return roomRepository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException(
                        "Room not found with id " + id,
                        "room",
                        "notfound"
                ));
    }

    @Transactional(readOnly = true)
    public Page<Room> findAll(Pageable pageable) {
        LOG.debug("[FIND ALL] Fetching all rooms pageable={}", pageable);

        Page<Room> roomsPage = roomRepository.findAll(pageable);

        LOG.debug(
                "[FIND ALL] Retrieved rooms count={} pageNumber={} pageSize={} totalElements={} totalPages={}",
                roomsPage.getNumberOfElements(),
                roomsPage.getNumber(),
                roomsPage.getSize(),
                roomsPage.getTotalElements(),
                roomsPage.getTotalPages()
        );

        return roomsPage;
    }

    @Transactional(readOnly = true)
    public Page<Room> findByName(String name, Pageable pageable) {
        LOG.debug("[FIND BY NAME] Searching rooms by name='{}' pageable={}", name, pageable);
        return roomRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Room> findByDepartmentId(Long departmentId, Pageable pageable) {
        LOG.debug("[FIND BY DEPARTMENT ID] Searching rooms by departmentId='{}' pageable={}", departmentId, pageable);
        return roomRepository.findByDepartment_Id(departmentId, pageable);
    }


    @Transactional(readOnly = true)
    public Page<Room> findAvailableRoomsByDepartmentAndGender(Long departmentId, Gender gender, Pageable pageable) {
        LOG.debug("[FIND AVAILABLE ROOMS] departmentId='{}' gender='{}' pageable={}",
                departmentId, gender, pageable);

        Page<Room> roomsPage =
                roomRepository.findByDepartment_IdAndIsActiveTrueAndGenderIsNullOrDepartment_IdAndIsActiveTrueAndGender(
                        departmentId,
                        departmentId,
                        gender,
                        pageable
                );

        LOG.debug(
                "[FIND AVAILABLE ROOMS] Retrieved rooms count={} pageNumber={} pageSize={} totalElements={} totalPages={}",
                roomsPage.getNumberOfElements(),
                roomsPage.getNumber(),
                roomsPage.getSize(),
                roomsPage.getTotalElements(),
                roomsPage.getTotalPages()
        );

        return roomsPage;
    }

    @Transactional(readOnly = true)
    public List<Room> findAllByIds(List<Long> ids) {
        LOG.debug("[FIND ALL BY IDS] Fetching Rooms ids={}", ids);

        List<Room> rooms = roomRepository.findAllByIdIn(ids);

        if (rooms.isEmpty()) {
            throw new NotFoundAlertException(
                    "No rooms found for ids " + ids,
                    "room",
                    "list.notfound"
            );
        }

        return rooms;
    }

    @Transactional(readOnly = true)
    public Page<Room> findActiveAndAppointableByDepartmentId(Long departmentId, Pageable pageable) {
        LOG.debug("[FIND findActiveAndAppointableByDepartmentId] Fetching rooms departmentId={}, pageable={}", departmentId, pageable);

        Page<Room> roomsPage = roomRepository.findByIsActiveTrueAndAppointableIsTrueAndDepartment_Id(departmentId, pageable);

        return roomsPage;
    }

    private void handleConstraintsOnCreateOrUpdate(RuntimeException exception) {
        Throwable rootCause = getRootCause(exception);
        String errorMessage = rootCause != null ? rootCause.getMessage() : exception.getMessage();

        LOG.error("DB ROOT CAUSE: {}", errorMessage, exception);

        String lowerCaseErrorMessage = errorMessage != null ? errorMessage.toLowerCase() : "";

        if (lowerCaseErrorMessage.contains("uk_room_name_department_ci")
                || lowerCaseErrorMessage.contains("duplicate")
                || lowerCaseErrorMessage.contains("unique")) {
            throw new BadRequestAlertException(
                    "A room with the same name already exists in this department.",
                    "room",
                    "unique.room.name.department"
            );
        }

        if (lowerCaseErrorMessage.contains("ck_room_gender_required")
                || lowerCaseErrorMessage.contains("gender")) {
            throw new BadRequestAlertException(
                    "Gender is required when the room is gender-specific.",
                    "room",
                    "gender.required"
            );
        }

        if (lowerCaseErrorMessage.contains("chk_room_appointable_requirements")
                || lowerCaseErrorMessage.contains("default_duration_minutes")
                || lowerCaseErrorMessage.contains("default_buffer_before_minutes")
                || lowerCaseErrorMessage.contains("default_buffer_after_minutes")) {
            throw new BadRequestAlertException(
                    "Appointable room requires valid duration and buffer values.",
                    "room",
                    "appointable.requirements.invalid"
            );
        }

        throw new BadRequestAlertException(
                "Database constraint violated while saving room.",
                "room",
                "db.constraint"
        );
    }
}