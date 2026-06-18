package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.NphiesPayer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NphiesPayerRepository extends JpaRepository<NphiesPayer, Long> {

    Page<NphiesPayer> findByNphiesIdContainingIgnoreCase(
            String nphiesId,
            Pageable pageable
    );

    Page<NphiesPayer> findByNameEnContainingIgnoreCase(
            String nameEn,
            Pageable pageable
    );

    Page<NphiesPayer> findByNameArContainingIgnoreCase(
            String nameAr,
            Pageable pageable
    );
}