package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.NphiesPayer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NphiesPayerRepository extends JpaRepository<NphiesPayer, Long> {

    @Override
    @EntityGraph(attributePaths = {"facility", "country", "city"})
    Page<NphiesPayer> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"facility", "country", "city"})
    Optional<NphiesPayer> findById(Long id);

    @EntityGraph(attributePaths = {"facility", "country", "city"})
    Page<NphiesPayer> findByNphiesIdContainingIgnoreCase(
            String nphiesId,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"facility", "country", "city"})
    Page<NphiesPayer> findByNameEnContainingIgnoreCase(
            String nameEn,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"facility", "country", "city"})
    Page<NphiesPayer> findByNameArContainingIgnoreCase(
            String nameAr,
            Pageable pageable
    );

    boolean existsByNphiesIdIgnoreCase(String nphiesId);

    boolean existsByNphiesIdIgnoreCaseAndIdNot(String nphiesId, Long id);
}
