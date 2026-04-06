package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.BedRoomService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BedRoomServiceRepository extends JpaRepository<BedRoomService, Long> {

    Page<BedRoomService> findByRoom_Id(Long roomId, Pageable pageable);
}