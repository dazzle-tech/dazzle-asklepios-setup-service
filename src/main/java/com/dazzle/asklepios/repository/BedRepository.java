package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Bed;
import com.dazzle.asklepios.domain.enumeration.BedStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BedRepository extends JpaRepository<Bed, Long> {

    Page<Bed> findByRoom_Id(Long roomId, Pageable pageable);

    Page<Bed> findByRoom_IdAndIsActiveTrueAndStatus(
            Long roomId,
            BedStatus status,
            Pageable pageable
    );
    List<Bed> findAllByIdIn(List<Long> ids);

    Page<Bed> findByRoom_Department_IdAndIsActiveTrue(Long departmentId, Pageable pageable);

    long countByRoom_Department_IdAndIsActiveTrue(Long departmentId);

    long countByRoom_Department_IdAndIsActiveTrueAndStatus(
            Long departmentId,
            BedStatus status
    );
    boolean existsByRoom_IdAndStatus(Long roomId, BedStatus status);
    Page<Bed> findByRoom_IdAndIsActiveTrue(Long roomId, Pageable pageable);
}