package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.NphiesPayer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NphiesPayerRepository extends JpaRepository<NphiesPayer, Long> {

    @Override
    @EntityGraph(attributePaths = {"facility", "country", "city"})
    Page<NphiesPayer> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"facility", "country", "city", "tpas"})
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

    Optional<NphiesPayer> findFirstByNphiesIdIgnoreCase(String nphiesId);

    boolean existsByNphiesIdIgnoreCase(String nphiesId);

    boolean existsByNphiesIdIgnoreCaseAndIdNot(String nphiesId, Long id);

    @EntityGraph(attributePaths = {"facility", "country", "city"})
    Page<NphiesPayer> findByIsActiveTrue(Pageable pageable);

    @EntityGraph(attributePaths = {"facility", "country", "city"})
    List<NphiesPayer> findByIsActiveTrue();

    @EntityGraph(attributePaths = {"facility", "country", "city"})
    Page<NphiesPayer> findByIsActiveTrueAndNameEnContainingIgnoreCase(
            String nameEn,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"facility", "country", "city"})
    @Query("""
            SELECT p FROM NphiesPayer p
            WHERE p.isActive = true
              AND (
                    :search IS NULL OR :search = ''
                    OR LOWER(p.nameEn) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(p.nameAr) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(p.nphiesId) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            """)
    Page<NphiesPayer> searchActive(@Param("search") String search, Pageable pageable);
}
