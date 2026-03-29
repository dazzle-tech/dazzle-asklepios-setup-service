package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Bed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface BedRepository extends JpaRepository<Bed, Long> {

    Page<Bed> findByRoom_Id(Long roomId, Pageable pageable);

    Page<Bed> findByRoom_IdAndIsActiveTrue(Long roomId, Pageable pageable);
}