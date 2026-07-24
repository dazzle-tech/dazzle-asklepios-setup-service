package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Room;
import com.dazzle.asklepios.domain.enumeration.Gender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    Page<Room> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Room> findByDepartment_Id(Long departmentId, Pageable pageable);

    Page<Room> findByDepartment_IdAndIsActiveTrueAndGenderIsNullOrDepartment_IdAndIsActiveTrueAndGender(
            Long departmentId1,
            Long departmentId2,
            Gender gender,
            Pageable pageable
    );

    Page<Room> findByDepartment_IdAndIsActiveTrueAndIsSpecificGenderFalse(
            Long departmentId,
            Pageable pageable
    );

    Page<Room> findByIsActiveTrueAndAppointableIsTrueAndDepartment_Id(
            Long departmentId,
            Pageable pageable
    );

    List<Room> findAllByIdIn(List<Long> ids);
}